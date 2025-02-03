package playground.pages

import androidx.compose.runtime.*
import com.varabyte.kobweb.browser.api
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.forms.TextInput
import com.varabyte.kobweb.silk.components.layout.HorizontalDivider
import kotlinx.browser.window
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.rpc.krpc.KrpcConfig
import kotlinx.rpc.krpc.KrpcTransport
import kotlinx.rpc.krpc.KrpcTransportMessage
import kotlinx.rpc.krpc.client.KrpcClient
import kotlinx.rpc.krpc.rpcClientConfig
import kotlinx.rpc.krpc.serialization.json.json
import kotlinx.rpc.withService
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import playground.MyPostService
import playground.components.layouts.PageLayout
import kotlin.coroutines.CoroutineContext

@Page
@Composable
fun HomePage() {
    PageLayout("Welcome to Kobweb!") {
        val rpcClientPost = remember {
            PostRPCClient("/krpc-test", rpcClientConfig {
                serialization {
                    this.json()
                }
            })
        }
        val rpcServicePost = remember { rpcClientPost.withService<MyPostService>() }
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


class PostRequestTransport(private val apiPath: String) : KrpcTransport {
    var result: CompletableDeferred<ByteArray> = CompletableDeferred()

    val transportJob = Job()

    override val coroutineContext: CoroutineContext = transportJob

    override suspend fun receive(): KrpcTransportMessage {
        val x = result.await().decodeToString()
        result = CompletableDeferred()
        return KrpcTransportMessage.StringMessage(x)
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    override suspend fun send(message: KrpcTransportMessage) {
        val body = when (message) {
            is KrpcTransportMessage.BinaryMessage -> message.value.asUByteArray().toByteArray()
            is KrpcTransportMessage.StringMessage -> message.value.encodeToByteArray()
        }
        val res = async { window.api.tryPost(apiPath, body = body) }
        result.complete(res.await()!!)
    }
}

internal class PostRPCClient(
    apiPath: String,
    config: KrpcConfig.Client,
) : KrpcClient(config, PostRequestTransport(apiPath))
