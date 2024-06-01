package playground.pages

import MyService
import androidx.compose.runtime.*
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.forms.TextInput
import com.varabyte.kobweb.streams.ApiStream
import kotlinx.coroutines.launch
import kotlinx.rpc.client.awaitFieldInitialization
import kotlinx.rpc.client.withService
import kotlinx.rpc.rpcClientConfig
import kotlinx.rpc.serialization.json
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import playground.ApiStreamRPCClient
import playground.components.layouts.PageLayout

@Page
@Composable
fun HomePage() {
    PageLayout("Welcome to Kobweb!") {
        val stream = remember {
            ApiStream("my-rpc-test")
        }
        val rpcClient = remember {
            ApiStreamRPCClient(stream, rpcClientConfig {
                serialization {
                    json()
                }
            })
        }
        val myService = remember { rpcClient.withService<MyService>() }
        var flowInitialized by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            launch {
                myService.awaitFieldInitialization { myFlow }
                flowInitialized = true
            }
            println(myService.sayHello("john", "james", 11))
            println("done")
        }
        val coroutineScope = rememberCoroutineScope()

        Text("Please enter your name")
        var name by remember { mutableStateOf("") }
        TextInput(name, onTextChange = { name = it })
        P()
        Text("Hello ${name.takeIf { it.isNotBlank() } ?: "World"}!")
        Button(onClick = {
            coroutineScope.launch {
                myService.sayHello(name, "smith", 11)
                    .also(::println)
            }
        }) {
            Text("say hello")
        }
        if (flowInitialized) {
            val serviceText by myService.myFlow.collectAsState()
            Text("Text from server: $serviceText")
        }
    }
}
