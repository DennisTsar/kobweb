import org.jetbrains.amper.plugins.*

import java.nio.file.Path
import java.util.*
import kotlin.io.path.*

enum class TaskMode {
    Fa,
    Mdi;
}

@Configurable
interface Settings {
    /** Which type of icons to generate */
    val mode: TaskMode
    val srcFile: Path
}

@OptIn(ExperimentalPathApi::class)
@TaskAction
fun generateSources(
    @Input srcFile: Path,
    mode: TaskMode,
    @Output generatedSourceDir: Path,
) {
    val (code, dstFile) = when (mode) {
        TaskMode.Fa -> generateSourcesFa(srcFile) to 
            generatedSourceDir.resolve("com/varabyte/kobweb/silk/components/icons/fa/FaIcons.kt")
        TaskMode.Mdi -> generateSourcesMdi(srcFile) to
            generatedSourceDir.resolve("com/varabyte/kobweb/silk/components/icons/mdi/MdIcons.kt")
    }

    dstFile.apply {
        createParentDirectories()
        writeText(code)
    }
}
