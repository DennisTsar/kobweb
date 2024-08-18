package playground.components.layouts

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.*
import com.varabyte.kobweb.compose.dom.ref
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.defer.deferRender
import kotlinx.browser.document
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.Text
import playground.components.sections.NavHeader

val myVeryImportantNode = document.createElement("div").also {
    it.id = "veryImportant"
}

@Composable
fun PageLayout(title: String, content: @Composable () -> Unit) {
    LaunchedEffect(title) {
        document.title = title
    }

    Column(
        modifier = Modifier.fillMaxSize().textAlign(TextAlign.Center),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        NavHeader()
        H1 { Text(title) }
        Box(
            ref = ref {
                it.appendChild(myVeryImportantNode)
            }
        )
        // For demo purposes, don't show this on the Echo page
        if (title != "Echo test") {
            deferRender(myVeryImportantNode.id) {
                var count by remember { mutableStateOf(0) }
                Button(
                    onClick = { count++ },
                ) {
                    Text("Counter: $count")
                }
            }
        }
        content()
    }
}
