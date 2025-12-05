import com.varabyte.kobweb.gradle.application.tasks.KobwebStartTask
import com.varabyte.kobweb.project.KobwebApplication
import com.varabyte.kobweb.server.api.ServerEnvironment
import com.varabyte.kobweb.server.api.SiteLayout
import org.jetbrains.amper.plugins.CompilationArtifact
import org.jetbrains.amper.plugins.Input
import org.jetbrains.amper.plugins.ModuleSources
import org.jetbrains.amper.plugins.TaskAction
import java.nio.file.Path

@TaskAction
fun startServer(
    @Input projectRoot: ModuleSources,
    @Input serverJar: CompilationArtifact, // TODO: or just Path?
    @Input fatJarPath: Path, // just for task dependency
    reuseServer: Boolean,
    // Amper doesn't seem to support enums from dependencies as inputs
    env: String,
    siteLayout: String,
) {
    KobwebStartTask.execute(
        kobwebApplication = KobwebApplication(projectRoot.from.modulePath),
        reuseServer = reuseServer,
        env = ServerEnvironment.valueOf(env),
        siteLayout = SiteLayout.valueOf(siteLayout),
        // HACK: using the standard jar from the build doesn't work, so we use the "executable" jar instead, which works.
        // However, the plugin api only exposes the standard jar (${module.jar}) and doesn't let use hook up the task
        // dependency, so you have to manually run: `./amper package -m server` for this to be available
        serverJar = serverJar.artifact.resolve("../../_server_executableJarJvm/server-jvm-executable.jar"),
        remoteDebuggingEnabled = false,
        remoteDebuggingPort = 0,
    )
}
