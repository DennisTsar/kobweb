package com.varabyte.kobweb.rpc

import com.varabyte.kobweb.api.data.Data
import com.varabyte.kobweb.api.data.add
import com.varabyte.kobweb.api.data.get
import com.varabyte.kobweb.api.data.getValue
import com.varabyte.kobweb.api.env.Environment
import com.varabyte.kobweb.api.http.Request
import com.varabyte.kobweb.api.init.InitApiContext
import com.varabyte.kobweb.api.log.Logger
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.rpc.RemoteService
import kotlinx.rpc.krpc.KrpcConfig
import kotlinx.rpc.krpc.KrpcTransport
import kotlinx.rpc.krpc.KrpcTransportMessage
import kotlinx.rpc.krpc.server.KrpcServer
import kotlinx.rpc.registerService
import kotlin.coroutines.CoroutineContext

// TODO: figure out String vs ByteArray/BinaryMessage handling?
class PostRpcTransport(private val data: TransportHandler) : KrpcTransport {
    // TODO: is there a way to get a better context?
    override val coroutineContext: CoroutineContext = Job()

    override suspend fun receive(): KrpcTransportMessage {
        return KrpcTransportMessage.StringMessage(data.awaitRequest())
    }

    override suspend fun send(message: KrpcTransportMessage) {
        val body = when (message) {
            is KrpcTransportMessage.BinaryMessage -> message.value.decodeToString()
            is KrpcTransportMessage.StringMessage -> message.value
        }
        data.provideResponse(body)
    }
}

// Currently this requires one server per service since setting up a `KrpcTransport` for multiple services seems complicated
@PublishedApi
internal class PostRpcServer(
    data: ServiceDataHolder<*>,
    config: KrpcConfig.Server,
) : KrpcServer(config, PostRpcTransport(data.transportHandler))

inline fun <reified Service : RemoteService> registerService(
    ctx: InitApiContext,
    config: KrpcConfig.Server,
    noinline serviceFactory: (CoroutineContext) -> Service
) {
    check(ctx.data.get<ServiceDataHolder<Service>>() == null) {
        "Service already registered: ${Service::class.simpleName}"
    }
    val serviceDataHolder = ServiceDataHolder<Service>()
    ctx.data.add(serviceDataHolder)
    PostRpcServer(serviceDataHolder, config)
        .registerService(serviceFactory)
}

class ServiceDataHolder<@Suppress("unused") Service : RemoteService> {
    val transportHandler = TransportHandler()
    var req: Request? = null
}

class TransportHandler {
    private var request: CompletableDeferred<String> = CompletableDeferred()
    private var response: CompletableDeferred<String> = CompletableDeferred()

    suspend fun awaitRequest(): String {
        val res = request.await()
        request = CompletableDeferred()
        return res
    }

    suspend fun awaitResponse(): String = response.await()

    fun provideResponse(res: String) {
        response.complete(res)
    }

    fun provideRequest(res: String) {
        response = CompletableDeferred()
        request.complete(res)
    }
}

class RpcContext<Service : RemoteService>(
    val env: Environment,
    val data: Data,
    val logger: Logger,
) {
    val req: Request? get() = data.getValue<ServiceDataHolder<Service>>().req
    // There probably should be some way of modifying some aspects of the response here
    // Though not the body, since that's what get returned by the function itself
}
