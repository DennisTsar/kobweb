package playground

import androidx.compose.runtime.*
import com.varabyte.kobweb.browser.storage.getItem
import com.varabyte.kobweb.browser.storage.setItem
import com.varabyte.kobweb.browser.storage.createStorageKey
import com.varabyte.kobweb.compose.css.*
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Color
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.App
import com.varabyte.kobweb.silk.SilkApp
import com.varabyte.kobweb.silk.components.layout.Surface
import com.varabyte.kobweb.silk.init.InitSilk
import com.varabyte.kobweb.silk.init.InitSilkContext
import com.varabyte.kobweb.silk.init.registerStyleBase
import com.varabyte.kobweb.silk.style.common.SmoothColorStyle
import com.varabyte.kobweb.silk.style.toModifier
import com.varabyte.kobweb.silk.theme.colors.ColorMode
import com.varabyte.kobweb.streams.ApiStream
import com.varabyte.kobweb.streams.ApiStreamListener
import kotlinx.browser.localStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.rpc.RPCConfig
import kotlinx.rpc.RPCTransport
import kotlinx.rpc.RPCTransportMessage
import kotlinx.rpc.client.KRPCClient
import org.jetbrains.compose.web.css.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private val COLOR_MODE_KEY =
    ColorMode.entries.createStorageKey("playground:app:colorMode", defaultValue = ColorMode.DARK)

@InitSilk
fun updateTheme(ctx: InitSilkContext) = ctx.config.apply {
    initialColorMode = localStorage.getItem(COLOR_MODE_KEY)!!
}

@InitSilk
fun registerGlobalStyles(ctx: InitSilkContext) = ctx.stylesheet.apply {
    registerStyleBase("body") {
        Modifier
            .fontFamily(
                "-apple-system", "BlinkMacSystemFont", "Segoe UI", "Roboto", "Oxygen", "Ubuntu",
                "Cantarell", "Fira Sans", "Droid Sans", "Helvetica Neue", "sans-serif"
            )
            .lineHeight(1.4)
    }

    registerStyleBase("blockquote") {
        Modifier
            .borderLeft(width = 5.px, style = LineStyle.Solid, color = Color.rgb(0x0c0c0c))
            .margin(topBottom = 1.5.em, leftRight = 10.px)
            .padding(topBottom = 0.5.em, leftRight = 10.px)
            .textAlign(TextAlign.Left)
    }

    registerStyleBase("table, th, td") {
        Modifier.border(1.px, LineStyle.Solid, Colors.LightGray)
    }

    registerStyleBase("table") {
        Modifier.borderCollapse(BorderCollapse.Collapse)
    }

    registerStyleBase("#md-inline-demo") {
        Modifier.color(Colors.OrangeRed)
    }
}

@App
@Composable
fun AppEntry(content: @Composable () -> Unit) {
    SilkApp {
        val colorMode = ColorMode.current
        LaunchedEffect(colorMode) { localStorage.setItem(COLOR_MODE_KEY, colorMode) }

        Surface(SmoothColorStyle.toModifier().minHeight(100.vh)) {
            content()
        }
    }
}

//@OptIn(InternalCoroutinesApi::class, DelicateCoroutinesApi::class)
//class WebSocketTransport(private val webSocket: WebSocket) : RPCTransport {
//    // Transport job should always be cancelled and never closed
//    private val transportJob = Job()
//
//    override val coroutineContext: CoroutineContext = transportJob
//
//    init {
//        // Close the socket when the transport job is cancelled manually
//        transportJob.invokeOnCompletion(onCancelling = true) { // this onCancelling is internal api idk why
//            webSocket.close()
//        }
//    }
//
//    override suspend fun send(message: RPCTransportMessage) {
//        when (message) {
//            is RPCTransportMessage.StringMessage -> {
//                webSocket.send(message.value)
//            }
//
//            is RPCTransportMessage.BinaryMessage -> {
//                webSocket.send(message.value.unsafeCast<Int8Array>())
//            }
//        }
//    }
//
//    override suspend fun receive(): RPCTransportMessage {
//        return suspendCoroutine { continuation ->
//            webSocket.onmessage = { messageEvent ->
//                val message = when (messageEvent.type) {
//                    "text" -> {
//                        RPCTransportMessage.StringMessage(messageEvent.data.unsafeCast<String>())
//                    }
//
//                    "blob" -> {
//                        // TODO: this cast is probably unsafe
//                        RPCTransportMessage.BinaryMessage(messageEvent.data.unsafeCast<ByteArray>())
//                    }
//
//                    else -> {
//                        error("Unsupported websocket message type: ${messageEvent.type}. Expected \"text\" or \"binary\"")
//                    }
//                }
//                continuation.resume(message)
//            }
//        }
//    }
//}
//
//internal class WsRPCClient(
//    webSocket: WebSocket,
//    config: RPCConfig.Client,
//) : KRPCClient(config, WebSocketTransport(webSocket))

@OptIn(InternalCoroutinesApi::class, DelicateCoroutinesApi::class)
class ApiStreamTransport(private val webSocket: ApiStream) : RPCTransport {
    // Transport job should always be cancelled and never closed
    private val transportJob = Job()

    override val coroutineContext: CoroutineContext = transportJob

    class MyStreamListener() : ApiStreamListener {
        var onTextReceived: (String) -> Unit = {}
        override fun onTextReceived(ctx: ApiStreamListener.TextReceivedContext) {
            onTextReceived(ctx.text)
        }
    }

    var myStreamListener = MyStreamListener()

    init {
        // Close the socket when the transport job is cancelled manually
        transportJob.invokeOnCompletion(onCancelling = true) { // this onCancelling is internal api idk why
            webSocket.disconnect()
        }
        CoroutineScope(coroutineContext).launch {
            webSocket.connect(myStreamListener)
        }
    }

    override suspend fun send(message: RPCTransportMessage) {
        when (message) {
            is RPCTransportMessage.StringMessage -> {
                webSocket.send(message.value)
            }

            is RPCTransportMessage.BinaryMessage -> {
                webSocket.send(message.value.toString()) // TODO: this is wrong
            }
        }
    }

    override suspend fun receive(): RPCTransportMessage {
        return suspendCoroutine { continuation ->
            myStreamListener.onTextReceived = { messageEvent ->
                continuation.resume(RPCTransportMessage.StringMessage(messageEvent))
            }
        }
    }
}

internal class ApiStreamRPCClient(
    webSocket: ApiStream,
    config: RPCConfig.Client,
) : KRPCClient(config, ApiStreamTransport(webSocket))
