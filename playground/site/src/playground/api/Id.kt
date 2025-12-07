package playground.api

import com.varabyte.kobweb.api.Api
import com.varabyte.kobweb.api.ApiContext
import com.varabyte.kobweb.api.http.Body
import com.varabyte.kobweb.api.http.HttpMethod
import com.varabyte.kobweb.api.http.text
import java.util.*

@Api
fun generateId(ctx: ApiContext) {
    if (ctx.req.method != HttpMethod.GET) return
    ctx.res.body = Body.text("yolo234")//Body.text(UUID.randomUUID().toString())
}
