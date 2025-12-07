package playground.api

import com.varabyte.kobweb.api.Api
import com.varabyte.kobweb.api.ApiContext
import com.varabyte.kobweb.api.http.Body
import com.varabyte.kobweb.api.http.text

@Api
fun hello(ctx: ApiContext) {
    ctx.res.body = Body.text("hello world")
}