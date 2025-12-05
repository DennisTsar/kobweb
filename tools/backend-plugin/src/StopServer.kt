import com.varabyte.kobweb.gradle.application.tasks.KobwebStopTask
import com.varabyte.kobweb.project.KobwebApplication
import org.jetbrains.amper.plugins.Input
import org.jetbrains.amper.plugins.ModuleSources
import org.jetbrains.amper.plugins.TaskAction

@TaskAction
fun stopServer(
    @Input projectRoot: ModuleSources,
) {
    KobwebStopTask.execute(
        kobwebApplication = KobwebApplication(projectRoot.from.modulePath),
    )
}
