package playground.pages

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
import kotlinx.coroutines.launch
import kotlinx.rpc.client.awaitFieldInitialization
import kotlinx.rpc.client.withService
import kotlinx.rpc.rpcClientConfig
import kotlinx.rpc.serialization.json
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import playground.ApiStreamRPCClient
import playground.components.layouts.PageLayout

@Page
@Composable
fun HomePage() {
    PageLayout("Welcome to Kobweb!") {
        val stream = remember { ApiStream("my-rpc-test") }
        val rpcClient = remember {
            ApiStreamRPCClient(stream, rpcClientConfig {
                serialization {
                    json()
                }
            })
        }
        val rpcService = remember { rpcClient.withService<MyService>() }
        var flowInitialized by remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()
        LaunchedEffect(Unit) {
            launch {
                rpcService.awaitFieldInitialization { keyFlow }
                flowInitialized = true
            }
        }

        Text("Please enter your name")
        var name by remember { mutableStateOf("") }
        var serverHello by remember { mutableStateOf("") }
        Row(Modifier.gap(0.5.cssRem), verticalAlignment = Alignment.CenterVertically) {
            TextInput(name, onTextChange = { name = it })
            Button(onClick = {
                coroutineScope.launch {
                    serverHello = rpcService.sayHello(name, "Smith", 11)
                }
            }) { Text("Say Hello") }
        }
        Text("Server says: $serverHello")
        P()
        HorizontalDivider(Modifier.width(200.px))
        if (flowInitialized) {
            val key by rpcService.keyFlow.collectAsState()
            Text("Text from server: $key")
        }
    }
}
