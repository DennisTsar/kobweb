package playground.pages

import MyPostService
import MyService
import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.forms.TextInput
import com.varabyte.kobweb.silk.components.layout.HorizontalDivider
import com.varabyte.kobweb.streams.ApiStream
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.rpc.krpc.rpcClientConfig
import kotlinx.rpc.krpc.serialization.json.json
import kotlinx.rpc.krpc.streamScoped
import kotlinx.rpc.withService
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import playground.ApiStreamRPCClient
import playground.PostRPCClient
import playground.components.layouts.PageLayout

@Page
@Composable
fun HomePage() {
    PageLayout("Welcome to Kobweb!") {
        val stream = remember { ApiStream("my-rpc-test") }
        val rpcClientWs = remember {
            ApiStreamRPCClient(stream, rpcClientConfig {
                serialization {
                    json()
                }
            })
        }
        val rpcServiceWs = remember { rpcClientWs.withService<MyService>() }

        val rpcClientPost = remember {
            PostRPCClient("/krpc-test", rpcClientConfig {
                serialization {
                    json()
                }
            })
        }
        val rpcServicePost = remember { rpcClientPost.withService<MyPostService>() }

        val flow = remember { MutableStateFlow("no initialized") }
        LaunchedEffect(Unit) {
            streamScoped {
                rpcServiceWs.keyFlow()
                    .collect {
                        flow.value = it
                    }
            }
        }

        val coroutineScope = rememberCoroutineScope()

        Text("Please enter your name")
        var name by remember { mutableStateOf("") }
        var serverHello by remember { mutableStateOf("") }
        TextInput(name, onTextChange = { name = it })
        Row(Modifier.gap(0.5.cssRem), verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = {
                coroutineScope.launch {
                    serverHello = rpcServiceWs.sayHello(name, "Smith", 11)
                }
            }) { Text("Say Hello (Websocket)") }
            Button(onClick = {
                coroutineScope.launch {
                    serverHello = rpcServicePost.sayHello(name, "Smith", 11)
                }
            }) { Text("Say Hello (Post Request)") }
        }
        Text("Server says: $serverHello")
        P()
        HorizontalDivider(Modifier.width(200.px))
        val key by flow.collectAsState()
        Text("Text from server: $key")
    }
}
