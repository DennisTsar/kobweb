package playground

import androidx.compose.runtime.*
import com.varabyte.kobweb.browser.api
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
import com.varabyte.kobweb.silk.theme.colors.loadFromLocalStorage
import com.varabyte.kobweb.silk.theme.colors.saveToLocalStorage
import com.varabyte.kobweb.silk.theme.colors.systemPreference
import com.varabyte.kobweb.streams.ApiStream
import com.varabyte.kobweb.streams.ApiStreamListener
import kotlinx.browser.window
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.rpc.krpc.KrpcConfig
import kotlinx.rpc.krpc.KrpcTransport
import kotlinx.rpc.krpc.KrpcTransportMessage
import kotlinx.rpc.krpc.client.KrpcClient
import org.jetbrains.compose.web.css.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val COLOR_MODE_STORAGE_KEY_NAME = "playground:app:colorMode"

@InitSilk
fun updateTheme(ctx: InitSilkContext) = ctx.config.apply {
    initialColorMode = ColorMode.loadFromLocalStorage(COLOR_MODE_STORAGE_KEY_NAME) ?: ColorMode.systemPreference
}

@InitSilk
fun registerGlobalStyles(ctx: InitSilkContext) = ctx.stylesheet.apply {
    registerStyle("html") {
        cssRule(CSSMediaQuery.MediaFeature("prefers-reduced-motion", StylePropertyValue("no-preference"))) {
            Modifier.scrollBehavior(ScrollBehavior.Smooth)
        }
    }

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
        LaunchedEffect(colorMode) { colorMode.saveToLocalStorage(COLOR_MODE_STORAGE_KEY_NAME) }

        Surface(SmoothColorStyle.toModifier().minHeight(100.vh)) {
            content()
        }
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

@OptIn(InternalCoroutinesApi::class, DelicateCoroutinesApi::class)
class ApiStreamTransport(private val webSocket: ApiStream) : KrpcTransport {
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

    override suspend fun send(message: KrpcTransportMessage) {
        when (message) {
            is KrpcTransportMessage.StringMessage -> {
                webSocket.send(message.value)
            }

            is KrpcTransportMessage.BinaryMessage -> {
                webSocket.send(message.value.toString()) // TODO: this is wrong
            }
        }
    }

    override suspend fun receive(): KrpcTransportMessage {
        return suspendCoroutine { continuation ->
            myStreamListener.onTextReceived = { messageEvent ->
                continuation.resume(KrpcTransportMessage.StringMessage(messageEvent))
            }
        }
    }
}

internal class ApiStreamRPCClient(
    webSocket: ApiStream,
    config: KrpcConfig.Client,
) : KrpcClient(config, ApiStreamTransport(webSocket))

internal class PostRPCClient(
    apiPath: String,
    config: KrpcConfig.Client,
) : KrpcClient(config, PostRequestTransport(apiPath))
