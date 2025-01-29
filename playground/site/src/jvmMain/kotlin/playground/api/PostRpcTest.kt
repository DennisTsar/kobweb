package playground.api

import MyPostService
import com.varabyte.kobweb.api.Api
import com.varabyte.kobweb.api.ApiContext
import com.varabyte.kobweb.api.init.InitApi
import com.varabyte.kobweb.api.init.InitApiContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.rpc.krpc.KrpcConfig
import kotlinx.rpc.krpc.KrpcTransport
import kotlinx.rpc.krpc.KrpcTransportMessage
import kotlinx.rpc.krpc.rpcServerConfig
import kotlinx.rpc.krpc.serialization.json.json
import kotlinx.rpc.krpc.server.KrpcServer
import kotlinx.rpc.registerService
import kotlin.coroutines.CoroutineContext

@Api("krpc-test")
suspend fun mockRequest(ctx: ApiContext) {
    response = CompletableDeferred()
    request.complete(ctx.req.body!!.decodeToString())
    ctx.res.body = response.await().encodeToByteArray()
}

@InitApi
fun initPostRpcServer(ctx: InitApiContext) {
    PostRpcServer(rpcServerConfig {
        serialization {
            json()
        }
    }).registerService<MyPostService> {
        MyPostImpl(it)
    }
}

var request: CompletableDeferred<String> = CompletableDeferred()
var response: CompletableDeferred<String> = CompletableDeferred()

class MyPostImpl(override val coroutineContext: CoroutineContext) : MyPostService {
    override suspend fun sayHello(firstName: String, lastName: String, age: Int): String {
        return "Hello, $firstName $lastName (age $age) (Post Request)"
    }
}

class PostTransport : KrpcTransport {
    private val transportJob = Job()

    override val coroutineContext: CoroutineContext = transportJob

    override suspend fun receive(): KrpcTransportMessage {
        val response = request.await()
        request = CompletableDeferred()
        return KrpcTransportMessage.StringMessage(response)
    }

    override suspend fun send(message: KrpcTransportMessage) {
        val body = when (message) {
            is KrpcTransportMessage.BinaryMessage -> message.value.decodeToString()
            is KrpcTransportMessage.StringMessage -> message.value
        }
        response.complete(body)
    }
}

internal class PostRpcServer(
    config: KrpcConfig.Server,
) : KrpcServer(config, PostTransport())
