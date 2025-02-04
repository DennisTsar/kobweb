package playground.pages

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.rpc.createRpcService
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.forms.TextInput
import com.varabyte.kobweb.silk.components.layout.HorizontalDivider
import kotlinx.coroutines.launch
import kotlinx.rpc.krpc.rpcClientConfig
import kotlinx.rpc.krpc.serialization.json.json
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import playground.MyPostService
import playground.components.layouts.PageLayout

@Page
@Composable
fun HomePage() {
    PageLayout("Welcome to Kobweb!") {
        val rpcServicePost = remember {
            createRpcService<MyPostService>("/krpc-test", rpcClientConfig {
                serialization {
                    this.json()
                }
            })
        }
        val coroutineScope = rememberCoroutineScope()

        Text("Please enter your name")
        var name by remember { mutableStateOf("") }
        var serverHello by remember { mutableStateOf("") }
        TextInput(name, onTextChange = { name = it })
        Row(Modifier.gap(0.5.cssRem), verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = {
                coroutineScope.launch {
                    serverHello = rpcServicePost.sayHello(name, "Smith", 11)
                }
            }) { Text("Say Hello (Post Request)") }
        }
        Text("Server says: $serverHello")
        P()
        HorizontalDivider(Modifier.width(200.px))

    }
}
