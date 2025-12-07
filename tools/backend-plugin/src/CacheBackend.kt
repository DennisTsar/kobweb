import com.varabyte.kobweb.ksp.KOBWEB_APP_METADATA_BACKEND
import com.varabyte.kobweb.ksp.KOBWEB_METADATA_BACKEND
import com.varabyte.kobweb.project.backend.AppBackendData
import com.varabyte.kobweb.project.backend.BackendData
import com.varabyte.kobweb.project.backend.merge
import kotlinx.serialization.json.Json
import org.jetbrains.amper.plugins.Classpath
import org.jetbrains.amper.plugins.Input
import org.jetbrains.amper.plugins.ModuleSources
import org.jetbrains.amper.plugins.Output
import org.jetbrains.amper.plugins.TaskAction
import java.nio.file.Path
import kotlin.io.path.createParentDirectories
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

@TaskAction
fun cacheBackend(
    @Input appResources: ModuleSources?, // TODO: nullable/null-default to skip site stuff
    @Input otherResources: List<ModuleSources>,
    @Output outputFile: Path,
) {
    val appBackendMetadataFile = appResources?.sourceDirectories?.firstOrNull { it.endsWith("ksp") }
        ?.resolve(KOBWEB_APP_METADATA_BACKEND)
        ?.takeIf { it.exists() }
    // metadataFile will normally be found but might not exist for a project which JUST enabled the jvm backend OR
    // which itself defines no API endpoints but depends on library artifacts that do
    val appBackendData = appBackendMetadataFile?.readText()
        ?.let { Json.decodeFromString<AppBackendData>(it) } ?: AppBackendData()

    val mergedBackendData = buildList {
        add(appBackendData.backendData)
        otherResources.forEach { sources ->
            sources.sourceDirectories.filter { it.endsWith("ksp") }.forEach { path ->
                val backendFile = path.resolve(KOBWEB_METADATA_BACKEND).takeIf { it.exists() } ?: return@forEach
                add(Json.decodeFromString<BackendData>(backendFile.readText()))
            }
        }
    }.merge(throwError = { error(it) })

    outputFile.apply {
        createParentDirectories()
        writeText(Json.encodeToString(AppBackendData(appBackendData.apiInterceptorMethod, mergedBackendData)))
    }
}
