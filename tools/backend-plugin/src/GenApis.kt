import com.varabyte.kobweb.gradle.application.templates.createApisFactoryImpl
import com.varabyte.kobweb.project.backend.AppBackendData
import kotlinx.serialization.json.Json
import org.jetbrains.amper.plugins.Input
import org.jetbrains.amper.plugins.Output
import org.jetbrains.amper.plugins.TaskAction
import java.nio.file.Path
import kotlin.io.path.createParentDirectories
import kotlin.io.path.readText
import kotlin.io.path.writeText

@TaskAction
fun genApis(
    @Input appBackendData: Path,
    @Output outputDir: Path,
) {
    val appBackendData = Json.decodeFromString<AppBackendData>(appBackendData.readText())
    outputDir.resolve("ApisFactoryImpl.kt").apply {
        createParentDirectories()
        writeText(createApisFactoryImpl(appBackendData))
    }
}
