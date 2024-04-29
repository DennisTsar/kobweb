package com.varabyte.kobweb.gradle.application.tasks

import com.varabyte.kobweb.gradle.core.metadata.LibraryMetadata
import com.varabyte.kobweb.gradle.core.metadata.ModuleMetadata
import com.varabyte.kobweb.gradle.core.metadata.WorkerMetadata
import com.varabyte.kobweb.ksp.KOBWEB_METADATA_FRONTEND
import com.varabyte.kobweb.ksp.KOBWEB_METADATA_LIBRARY
import com.varabyte.kobweb.ksp.KOBWEB_METADATA_MODULE
import com.varabyte.kobweb.ksp.KOBWEB_METADATA_WORKER
import com.varabyte.kobweb.project.frontend.FrontendData
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.file.ArchiveOperations
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.inject.Inject

// NOTE: This task in meant as an internal API so it does not inherit from KobwebTask
/**
 * Collect all JS metadata from kobweb dependencies, writing the results to [libraryOutput] and [workerOutput].
 *
 * This is done so that multiple tasks can read the same values from a single, cached file. Those tasks should take
 * the relevant output location as an input and then deserialize it in their execute method:
 *
 * ```
 * // Configuring the task
 * myMetadataUsingTask.configure {
 *   appDataFile.set(kobwebWriteJsDepDataTask.flatMap { it.libraryOutput })
 * }
 *
 * // Inside the task
 * @get:InputFile
 * abstract val libraryDataFile: RegularFileProperty
 *
 * @TaskAction
 * fun execute() {
 *   val libraryData = Json.decodeFromString<List<JsLibraryData>>(libraryDataFile.get().asFile.readText())
 *   // ...
 * }
 * ```
 */
@CacheableTask
abstract class KobwebWriteJsDependencyDataTask : DefaultTask() {
    init {
        description = "Search the project's dependencies and store all Kobweb metadata, " +
            "at which point it can be looked up by downstream tasks that need it."
    }

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val compileClasspath: ConfigurableFileCollection

    @get:OutputFile
    abstract val libraryOutput: RegularFileProperty

    @get:OutputFile
    abstract val workerOutput: RegularFileProperty

    @TaskAction
    fun execute() {
        val libraries = mutableListOf<JsLibraryData>()
        val workers = mutableListOf<JsWorkerData>()
        compileClasspath.forEach { file ->
            val moduleMetadata = file.findDataInZip<ModuleMetadata>(KOBWEB_METADATA_MODULE)
                ?: return@forEach

            val libraryMetadata = file.findDataInZip<LibraryMetadata>(KOBWEB_METADATA_LIBRARY)

            if (libraryMetadata != null) {
                val frontendData = file.findDataInZip<FrontendData>(KOBWEB_METADATA_FRONTEND)
                libraries.add(
                    JsLibraryData(
                        moduleMetadata = moduleMetadata,
                        filename = file.name,
                        metadata = libraryMetadata,
                        frontendData = frontendData
                    )
                )
                return@forEach
            }

            val workerMetadata = file.findDataInZip<WorkerMetadata>(KOBWEB_METADATA_WORKER)
                ?: error("Found a module with no library or worker metadata: $file")
            workers.add(
                JsWorkerData(
                    moduleMetadata = moduleMetadata,
                    filename = file.name,
                    metadata = workerMetadata
                )
            )
        }
        libraryOutput.get().asFile.writeText(Json.encodeToString(libraries))
        workerOutput.get().asFile.writeText(Json.encodeToString(workers))
    }

    @get:Inject
    abstract val archiveOperations: ArchiveOperations

    @get:Inject
    abstract val objectFactory: ObjectFactory

    private inline fun <reified T> File.findDataInZip(path: String): T? {
        var data: T? = null
        // TODO: should we catch exceptions?
        val fileTree = if (isDirectory) {
            objectFactory.fileTree().from(this)
        } else {
            archiveOperations.zipTree(this)
        }
        fileTree
            .matching { include(path) }
            .visit { if (!isDirectory) data = Json.decodeFromString<T>(this.file.readText()) }

        return data
    }
}

interface KobwebDependencyData {
    val moduleMetadata: ModuleMetadata

    // TODO: do we want to save full path? path relative to something? just name?
    val filename: String

    // TODO: should this exist? currently it exists so that "file.nameWithoutExtension" can be used
    val file get() = File(filename)
}

@Serializable
class JsLibraryData(
    override val moduleMetadata: ModuleMetadata,
    override val filename: String,
    // TODO: var name should be "libraryMetadata" or just "metadata"?
    val metadata: LibraryMetadata,
    // a library with a js target maye have no js sources & thus no frontend data
    val frontendData: FrontendData?,
) : KobwebDependencyData

@Serializable
class JsWorkerData(
    override val moduleMetadata: ModuleMetadata,
    override val filename: String,
    val metadata: WorkerMetadata,
) : KobwebDependencyData
