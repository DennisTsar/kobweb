import com.varabyte.kobweb.api.Api
import com.varabyte.kobweb.api.init.InitApi
import com.varabyte.kobweb.api.init.InitApiContext
import com.varabyte.kobweb.api.log.Logger
import com.varabyte.kobweb.api.stream.ApiStream
import com.varabyte.kobweb.api.stream.Stream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.rpc.RPCConfig
import kotlinx.rpc.RPCTransport
import kotlinx.rpc.RPCTransportMessage
import kotlinx.rpc.registerService
import kotlinx.rpc.rpcServerConfig
import kotlinx.rpc.serialization.json
import kotlinx.rpc.server.KRPCServer
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

class MyCustomStream() : ApiStream() {
    var onTextReceived: (String) -> Unit = {}
    lateinit var logger: Logger
    lateinit var stream: Stream
    override suspend fun onClientConnected(ctx: ClientConnectedContext) {
        logger = ctx.logger
        stream = ctx.stream
    }

    override suspend fun onTextReceived(ctx: TextReceivedContext) {
        ctx.logger.warn("received ${ctx.text}")
        this.onTextReceived(ctx.text)
    }

    override suspend fun onClientDisconnected(ctx: ClientDisconnectedContext) {}
}

@Api("my-rpc-test")
val x = MyCustomStream()

@OptIn(InternalCoroutinesApi::class, DelicateCoroutinesApi::class)
class WebSocketTransport(private val stream: MyCustomStream) : RPCTransport {
    // Transport job should always be cancelled and never closed
    private val transportJob = Job()

    override val coroutineContext: CoroutineContext = transportJob

    init {
        // TODO: Don't think there's a way to close an API stream do we have to handle this?
        // Close the socket when the transport job is cancelled manually
//        transportJob.invokeOnCompletion(onCancelling = true) { // this onCancelling is internal api idk why
//            webSocket.close()
//        }
    }

    override suspend fun send(message: RPCTransportMessage) {
        when (message) {
            is RPCTransportMessage.StringMessage -> {
                stream.logger.warn("sending back ${message.value}")
                stream.stream.send(message.value)
            }

            is RPCTransportMessage.BinaryMessage -> {
                stream.stream.send(message.value.toString()) // TODO: this is wrong
            }
        }
    }

    override suspend fun receive(): RPCTransportMessage {
        return suspendCoroutine { continuation ->
            stream.onTextReceived = { messageEvent ->
                stream.logger.warn("i also recieved $messageEvent")
                continuation.resume(RPCTransportMessage.StringMessage(messageEvent))
            }
        }
    }
}

internal class WsRPCServer(
    webSocket: MyCustomStream,
    config: RPCConfig.Server,
) : KRPCServer(config, WebSocketTransport(webSocket))

@InitApi
fun initTest1(ctx: InitApiContext) {
    WsRPCServer(x, rpcServerConfig {
        serialization {
            json()
        }
    }).registerService<MyService> {
        MyServiceImpl(it)
    }
}

class MyServiceImpl(override val coroutineContext: CoroutineContext) : MyService {
    private val _myFlow = MutableStateFlow("a")
    override val myFlow = _myFlow
    override suspend fun sayHello(firstName: String, lastName: String, age: Int): String {
        return "hello $firstName $lastName $age"
    }

    init {
        CoroutineScope(coroutineContext).launch {
            while (true) {
                delay(2.seconds)
                _myFlow.value = Random.nextInt().toString()
            }
        }
    }
}
