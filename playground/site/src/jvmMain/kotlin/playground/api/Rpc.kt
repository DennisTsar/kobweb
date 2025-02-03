package playground.api

import com.varabyte.kobweb.api.Api
import com.varabyte.kobweb.api.ApiContext
import com.varabyte.kobweb.api.data.getValue
import com.varabyte.kobweb.api.http.HttpMethod
import com.varabyte.kobweb.api.http.setBodyText
import com.varabyte.kobweb.api.init.InitApi
import com.varabyte.kobweb.api.init.InitApiContext
import com.varabyte.kobweb.rpc.RpcContext
import com.varabyte.kobweb.rpc.ServiceDataHolder
import com.varabyte.kobweb.rpc.registerService
import kotlinx.rpc.krpc.rpcServerConfig
import kotlinx.rpc.krpc.serialization.json.json
import playground.MyPostService
import kotlin.coroutines.CoroutineContext

//@KobwebRpc("krpc-test")
class MyPostImpl(
    val ctx: RpcContext<MyPostService>,
    override val coroutineContext: CoroutineContext
) : MyPostService {
    override suspend fun sayHello(firstName: String, lastName: String, age: Int): String {
        return "Hello, $firstName $lastName (age $age) (Post Request) ${ctx.req!!.connection.origin} ${ctx.req!!.queryParams}"
    }
}

// The below should be automatically generated

@InitApi
fun initPostRpcServer(ctx: InitApiContext) {
    registerService<MyPostService>(ctx, rpcServerConfig {
        serialization {
            json()
        }
    }) {
        MyPostImpl(RpcContext(ctx.env, ctx.data, ctx.logger), it)
    }
}

@Api("krpc-test")
suspend fun mockRequest(ctx: ApiContext) {
    if (ctx.req.method != HttpMethod.POST) {
        return
    }
    val data = ctx.data.getValue<ServiceDataHolder<MyPostService>>()
    data.req = ctx.req
    // TODO: I don't think every request needs a body?
    data.transportHandler.provideRequest(ctx.req.body!!.decodeToString())
    ctx.res.setBodyText(data.transportHandler.awaitResponse())
}
