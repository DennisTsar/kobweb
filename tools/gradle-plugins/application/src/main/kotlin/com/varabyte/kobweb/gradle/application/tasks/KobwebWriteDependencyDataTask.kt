package com.varabyte.kobweb.gradle.application.tasks

import com.varabyte.kobweb.gradle.core.metadata.LibraryMetadata
import com.varabyte.kobweb.gradle.core.metadata.ModuleMetadata
import com.varabyte.kobweb.gradle.core.metadata.WorkerMetadata
import com.varabyte.kobweb.gradle.core.util.searchZipFor
import com.varabyte.kobweb.ksp.KOBWEB_METADATA_FRONTEND
import com.varabyte.kobweb.ksp.KOBWEB_METADATA_LIBRARY
import com.varabyte.kobweb.ksp.KOBWEB_METADATA_MODULE
import com.varabyte.kobweb.ksp.KOBWEB_METADATA_WORKER
import com.varabyte.kobweb.project.frontend.FrontendData
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

// NOTE: This task in meant as an internal API so it does not inherit from KobwebTask
// TODO: docs
// TODO: is jvm a separate task or what?
/**
 * Collect all app data from the current site and all library dependencies, writing the result to [libraryOutput].
 *
 * This is done so that multiple tasks can read the same values from a single, cached file. Those tasks should take
 * `appDataFile` as an input and then deserialize it in their execute method:
 *
 * ```
 * // Configuring the task
 * myAppDataUsingTask.configure {
 *   appDataFile.set(kobwebCacheAppDataTask.flatMap { it.appDataFile })
 * }
 *
 * // Inside the task
 * @get:InputFile
 * abstract val appDataFile: RegularFileProperty
 *
 * @TaskAction
 * fun execute() {
 *   val appData = Json.decodeFromString<AppData>(appDataFile.get().asFile.readText())
 *   // ...
 * }
 * ```
 */
abstract class KobwebWriteDependencyDataTask : DefaultTask() {
    init {
        description = "Search the project's dependencies and store all Kobweb metadata, " +
            "at which point it can be looked up by downstream tasks that need it."
    }

    @get:InputFiles
    abstract val jsCompileClasspath: ConfigurableFileCollection

    @get:OutputFile
    abstract val jsOutput: RegularFileProperty

//    @get:InputFiles
//    @get:Optional
//    abstract val jvmCompileClasspath: ConfigurableFileCollection

//    @get:OutputFile
//    @get:Optional
//    abstract val jvmOutput: RegularFileProperty

    @TaskAction
    fun execute() {
        val libraries = mutableListOf<JsLibraryData>()
        val workers = mutableListOf<JsWorkerData>()
        jsCompileClasspath.forEach { file ->
            val moduleMetadata = file.findDataInZip<ModuleMetadata>(KOBWEB_METADATA_MODULE)
                ?: return@forEach

            val libraryMetadata = file.findDataInZip<LibraryMetadata>(KOBWEB_METADATA_LIBRARY)

            if (libraryMetadata != null) {
                val frontendData = file.findDataInZip<FrontendData>(KOBWEB_METADATA_FRONTEND)
                libraries.add(
                    JsLibraryData(
                        moduleMetadata = moduleMetadata,
                        path = file.invariantSeparatorsPath,
                        libraryMetadata = libraryMetadata,
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
                    path = file.invariantSeparatorsPath,
                    workerMetadata = workerMetadata
                )
            )
        }
        jsOutput.get().asFile.writeText(Json.encodeToString(JsDependencyData(libraries, workers)))
    }

    private inline fun <reified T> File.findDataInZip(path: String): T? {
        var data: T? = null
        searchZipFor(path) { bytes ->
            data = Json.decodeFromString<T>(bytes.decodeToString())
        }
        return data
    }
}

interface KobwebDependencyData {
    val moduleMetadata: ModuleMetadata
    val path: String
    val file get() = File(path)
}

@Serializable
class JsLibraryData(
    override val moduleMetadata: ModuleMetadata,
    override val path: String,
    val libraryMetadata: LibraryMetadata,
    // a library with a js target maye have no js sources & thus no frontend data
    val frontendData: FrontendData?,
) : KobwebDependencyData

@Serializable
class JsWorkerData(
    override val moduleMetadata: ModuleMetadata,
    override val path: String,
    val workerMetadata: WorkerMetadata,
) : KobwebDependencyData

@Serializable
class JsDependencyData(
    val libraries: List<JsLibraryData>,
    val workers: List<JsWorkerData>,
)
