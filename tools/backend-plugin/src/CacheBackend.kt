import com.varabyte.kobweb.gradle.core.util.searchZipFor
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
import org.jetbrains.amper.plugins.SourcesKind
import org.jetbrains.amper.plugins.TaskAction
import java.nio.file.Path
import kotlin.io.path.createParentDirectories
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

@TaskAction
fun cacheBackend(
    @Input siteClasspath: Classpath,
    @Input siteResources: ModuleSources,
    @Output outputFile: Path,
) {
    // Uncomment this line for a fun surprise (crash). Spoiler:
    // ERROR: Task ':site-bootstrap:cacheBackend@backend-plugin' failed: java.lang.ClassCastException: class org.jetbrains.amper.frontend.plugins.generated.ShadowSourcesKind cannot be cast to class org.jetbrains.amper.plugins.SourcesKind (org.jetbrains.amper.frontend.plugins.generated.ShadowSourcesKind is in unnamed module of loader 'app'; org.jetbrains.amper.plugins.SourcesKind is in unnamed module of loader ':site-bootstrap:cacheBackend@backend-plugin' @350b6788)                                                                                                                  
    //        at jdk.proxy5/jdk.proxy5.$Proxy61.getKind(Unknown Source)
    //        at CacheBackendKt.cacheBackend(CacheBackend.kt:26)
//    check(siteResources.kind == SourcesKind.Resources) { "`siteResources` must be resources" }

    val appBackendMetadataFile = siteResources.sourceDirectories.firstOrNull { it.endsWith("ksp") }
        ?.resolve(KOBWEB_APP_METADATA_BACKEND)
        ?.takeIf { it.exists() }

    // metadataFile will normally be found but might not exist for a project which JUST enabled the jvm backend OR
    // which itself defines no API endpoints but depends on library artifacts that do
    val appBackendData = appBackendMetadataFile?.readText()
        ?.let { Json.decodeFromString<AppBackendData>(it) } ?: AppBackendData()

    val mergedBackendData = buildList {
        add(appBackendData.backendData)
        // TODO: we don't need to look through the `site` jar itself
        siteClasspath.resolvedFiles.forEach { file ->
            file.toFile().searchZipFor(KOBWEB_METADATA_BACKEND) { bytes ->
                add(Json.decodeFromString<BackendData>(bytes.decodeToString()))
            }
        }
    }.merge(throwError = { error(it) })

    outputFile.apply {
        createParentDirectories()
        writeText(Json.encodeToString(AppBackendData(appBackendData.apiInterceptorMethod, mergedBackendData)))
    }
}
