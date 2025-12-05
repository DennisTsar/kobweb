import org.jetbrains.amper.plugins.Classpath
import org.jetbrains.amper.plugins.CompilationArtifact
import org.jetbrains.amper.plugins.Configurable
import org.jetbrains.amper.plugins.ModuleSources

@Configurable
interface Settings {
    val serverJar: CompilationArtifact
    val siteModuleResources: ModuleSources // we separately need the module path and its resources - this gives us both
    val siteClasspath: Classpath
}
