package playground.pages

import androidx.compose.runtime.*
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.silk.components.forms.Input
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.Worker
import playground.components.layouts.PageLayout
import playground.worker.WorkerInParams
import playground.worker.WorkerOutParams

@Page
@Composable
fun WorkerPage() {
    PageLayout("Worker test") {
        var sum by remember { mutableStateOf(0) }
        val worker = remember { Worker("worker.js") }
        worker.onmessage = { e ->
            val outParams = Json.decodeFromString<WorkerOutParams>(e.data as String)
            sum = outParams.sum
            Unit
        }

        var a by remember { mutableStateOf<Int?>(0) }
        var b by remember { mutableStateOf<Int?>(0) }

        LaunchedEffect(a, b) {
            worker.postMessage(Json.encodeToString(WorkerInParams(a?:0, b?:0)))
        }

        Input(InputType.Number, value = a, onValueChanged = { a = it?.toInt() })
        Input(InputType.Number, value = b, onValueChanged = { b = it?.toInt() })

        P()
        Text("Sum from worker: $sum")
    }
}
