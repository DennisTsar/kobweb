package com.varabyte.kobweb.rpc

import com.varabyte.kobweb.browser.api
import kotlinx.browser.window
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.rpc.RemoteService
import kotlinx.rpc.krpc.KrpcConfig
import kotlinx.rpc.krpc.KrpcTransport
import kotlinx.rpc.krpc.KrpcTransportMessage
import kotlinx.rpc.krpc.client.KrpcClient
import kotlinx.rpc.withService
import kotlin.coroutines.CoroutineContext

internal class PostRpcClientTransport(private val apiPath: String) : KrpcTransport {
    private var response: CompletableDeferred<ByteArray> = CompletableDeferred()

    val transportJob = Job()

    override val coroutineContext: CoroutineContext = transportJob

    override suspend fun receive(): KrpcTransportMessage {
        val res = response.await().decodeToString()
        response = CompletableDeferred()
        return KrpcTransportMessage.StringMessage(res)
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    override suspend fun send(message: KrpcTransportMessage) {
        val body = when (message) {
            is KrpcTransportMessage.BinaryMessage -> message.value.asUByteArray().toByteArray()
            is KrpcTransportMessage.StringMessage -> message.value.encodeToByteArray()
        }
        // TODO: how to handle errors?
        val res = async { window.api.post(apiPath, body = body) }
        response.complete(res.await())
    }
}

class PostRpcClient(
    apiPath: String,
    config: KrpcConfig.Client,
) : KrpcClient(config, PostRpcClientTransport(apiPath))

// If we have one service per api route anyway, then we could remove the `apiPath` parameter in favor of generated code
// that automatically associates a service with a specific route
inline fun <reified T : RemoteService> createRpcService(apiPath: String, config: KrpcConfig.Client): T {
    return PostRpcClient(apiPath, config).withService<T>()
}
