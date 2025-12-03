package com.varabyte.kobweb.browser.http

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.w3c.fetch.RequestRedirect
import kotlin.collections.orEmpty

/**
 * Call GET on a target resource with [R] as the expected return type.
 *
 * See also [tryGet], which will return null if the request fails for any reason.
 *
 * Note: you should NOT prepend your path with "api/", as that will be added automatically.
 */
suspend inline fun <reified R> HttpFetcher.get(
    resource: String,
    headers: Map<String, Any>? = FetchDefaults.Headers,
    redirect: RequestRedirect? = FetchDefaults.Redirect,
    abortController: AbortController? = null,
    responseDeserializer: DeserializationStrategy<R> = serializer()
): R {
    return Json.decodeFromString(
        responseDeserializer,
        getBytes(resource, headers, redirect, abortController).decodeToString()
    )
}

/**
 * Like [get], but returns null if the request failed for any reason.
 *
 * Additionally, if [HttpFetcher.logOnError] is set to true, any failure will be logged to the console. By default, this will
 * be true for debug builds and false for release builds.
 */
suspend inline fun <reified R> HttpFetcher.tryGet(
    resource: String,
    headers: Map<String, Any>? = FetchDefaults.Headers,
    redirect: RequestRedirect? = FetchDefaults.Redirect,
    abortController: AbortController? = null,
    responseDeserializer: DeserializationStrategy<R> = serializer()
): R? {
    return tryGetBytes(resource, headers, redirect, abortController)
        ?.decodeToString()
        ?.let { Json.decodeFromString(responseDeserializer, it) }
}

/**
 * Call POST on a target resource with [R] as the expected return type.
 *
 * You can set [R] to `Unit` if this request doesn't expect a response body.
 *
 * See also [tryPost], which will return null if the request fails for any reason.
 *
 * Note: you should NOT prepend your path with "api/", as that will be added automatically.
 *
 * @param body The body to send with the request. Make sure your class is marked with @Serializable or provide a custom
 *  [bodySerializer].
 */
suspend inline fun <reified B, reified R> HttpFetcher.post(
    resource: String,
    body: B,
    headers: Map<String, Any>? = FetchDefaults.Headers,
    redirect: RequestRedirect? = FetchDefaults.Redirect,
    abortController: AbortController? = null,
    bodySerializer: SerializationStrategy<B> = serializer(),
    responseDeserializer: DeserializationStrategy<R> = serializer()
): R {
    val responseBytes = postBytes(
        resource,
        body,
        mapOf("Content-type" to "application/json") + headers.orEmpty(),
        redirect,
        abortController,
        bodySerializer
    )

    if (R::class == Unit::class) return Unit as R

    return Json.decodeFromString(responseDeserializer, responseBytes.decodeToString())
}

/**
 * A serialize-friendly version of [post] that expects a body but does not expect a serialized response.
 */
@Deprecated("DO NOT IGNORE. Please change to `postBytes` instead. This method will be modified soon in a backwards incompatible way, in order to support additional cases that the current form doesn't support.", replaceWith = ReplaceWith("postBytes(resource, body, headers, redirect, abortController, bodySerializer)"))
suspend inline fun <reified B> HttpFetcher.post(
    resource: String,
    body: B,
    headers: Map<String, Any>? = FetchDefaults.Headers,
    redirect: RequestRedirect? = FetchDefaults.Redirect,
    abortController: AbortController? = null,
    bodySerializer: SerializationStrategy<B> = serializer(),
): ByteArray = postBytes(resource, body, headers, redirect, abortController, bodySerializer)

/**
 * A serialize-friendly version of [post] that expects a body and returns its response as a raw byte array.
 */
suspend inline fun <reified B> HttpFetcher.postBytes(
    resource: String,
    body: B,
    headers: Map<String, Any>? = FetchDefaults.Headers,
    redirect: RequestRedirect? = FetchDefaults.Redirect,
    abortController: AbortController? = null,
    bodySerializer: SerializationStrategy<B> = serializer(),
): ByteArray {
    return postBytes(
        resource,
        mapOf("Content-type" to "application/json") + headers.orEmpty(),
        Json.encodeToString(bodySerializer, body).encodeToByteArray(),
        redirect,
        abortController
    )
}

/**
 * A serialize-friendly version of [post] that has no body but expects a serialized response.
 */
suspend inline fun <reified R> HttpFetcher.post(
    resource: String,
    headers: Map<String, Any>? = FetchDefaults.Headers,
    redirect: RequestRedirect? = FetchDefaults.Redirect,
    abortController: AbortController? = null,
    responseDeserializer: DeserializationStrategy<R> = serializer(),
): R {
    val responseBytes = postBytes(
        resource,
        headers,
        body = null,
        redirect,
        abortController
    )

    return Json.decodeFromString(responseDeserializer, responseBytes.decodeToString())
}

/**
 * Like [post], but returns null if the request failed for any reason.
 *
 * Additionally, if [HttpFetcher.logOnError] is set to true, any failure will be logged to the console. By default, this will
 * be true for debug builds and false for release builds.
 *
 * @param body The body to send with the request. Make sure your class is marked with @Serializable or provide a custom
 *  [bodySerializer].
 */
suspend inline fun <reified B, reified R> HttpFetcher.tryPost(
    resource: String,
    body: B,
    headers: Map<String, Any>? = FetchDefaults.Headers,
    redirect: RequestRedirect? = FetchDefaults.Redirect,
    abortController: AbortController? = null,
    bodySerializer: SerializationStrategy<B> = serializer(),
    responseDeserializer: DeserializationStrategy<R> = serializer()
): R? {
    val responseBytes = tryPostBytes(
        resource,
        body,
        mapOf("Content-type" to "application/json") + headers.orEmpty(),
        redirect,
        abortController,
        bodySerializer,
    )

    if (responseBytes == null) return null
    if (R::class == Unit::class) return Unit as R

    return Json.decodeFromString(responseDeserializer, responseBytes.decodeToString())
}

/**
 * A serialize-friendly version of [tryPost] that expects a body but does not expect a serialized response.
 */
@Deprecated("DO NOT IGNORE. Please change to `tryPostBytes` instead. This method will be modified soon in a backwards incompatible way, in order to support additional cases that the current form doesn't support.", replaceWith = ReplaceWith("tryPostBytes(resource, body, headers, redirect, abortController, bodySerializer)"))
suspend inline fun <reified B> HttpFetcher.tryPost(
    resource: String,
    body: B,
    headers: Map<String, Any>? = FetchDefaults.Headers,
    redirect: RequestRedirect? = FetchDefaults.Redirect,
    abortController: AbortController? = null,
    bodySerializer: SerializationStrategy<B> = serializer(),
): ByteArray? = tryPostBytes(resource, body, headers, redirect, abortController, bodySerializer)

/**
 * A serialize-friendly version of [tryPost] that expects a body and returns its response as a raw byte array.
 */
suspend inline fun <reified B> HttpFetcher.tryPostBytes(
    resource: String,
    body: B,
    headers: Map<String, Any>? = FetchDefaults.Headers,
    redirect: RequestRedirect? = FetchDefaults.Redirect,
    abortController: AbortController? = null,
    bodySerializer: SerializationStrategy<B> = serializer(),
): ByteArray? {
    return tryPostBytes(
        resource,
        headers,
        Json.encodeToString(bodySerializer, body).encodeToByteArray(),
        redirect,
        abortController,
    )
}

/**
 * A serialize-friendly version of [tryPost] that has no body but expects a serialized response.
 */
suspend inline fun <reified R> HttpFetcher.tryPost(
    resource: String,
    headers: Map<String, Any>? = FetchDefaults.Headers,
    redirect: RequestRedirect? = FetchDefaults.Redirect,
    abortController: AbortController? = null,
    responseDeserializer: DeserializationStrategy<R> = serializer(),
): R? {
    val responseBytes = tryPostBytes(
        resource,
        headers,
        body = null,
        redirect,
        abortController
    )

    if (responseBytes == null) return null

    return Json.decodeFromString(responseDeserializer, responseBytes.decodeToString())
}


/**
 * Call PUT on a target resource with [R] as the expected return type.
 *
 * You can set [R] to `Unit` if this request doesn't expect a response body.
 *
 * See also [tryPut], which will return null if the request fails for any reason.
 *
 * Note: you should NOT prepend your path with "api/", as that will be added automatically.
 *
 * @param body The body to send with the request. Make sure your class is marked with @Serializable or provide a custom
 *  [bodySerializer].
 */
suspend inline fun <reified B, reified R> HttpFetcher.put(
    resource: String,
    body: B,
    headers: Map<String, Any>? = FetchDefaults.Headers,
    redirect: RequestRedirect? = FetchDefaults.Redirect,
    abortController: AbortController? = null,
    bodySerializer: SerializationStrategy<B> = serializer(),
    responseDeserializer: DeserializationStrategy<R> = serializer()
): R {
    val responseBytes = putBytes(
        resource,
        body,
        mapOf("Content-type" to "application/json") + headers.orEmpty(),
        redirect,
        abortController,
        bodySerializer
    )

    if (R::class == Unit::class) return Unit as R

    return Json.decodeFromString(responseDeserializer, responseBytes.decodeToString())
}

/**
 * A serialize-friendly version of [put] that expects a body but does not expect a serialized response.
 */
@Deprecated("DO NOT IGNORE. Please change to `putBytes` instead. This method will be modified soon in a backwards incompatible way, in order to support additional cases that the current form doesn't support.", replaceWith = ReplaceWith("putBytes(resource, body, headers, redirect, abortController, bodySerializer)"))
suspend inline fun <reified B> HttpFetcher.put(
    resource: String,
    body: B,
    headers: Map<String, Any>? = FetchDefaults.Headers,
    redirect: RequestRedirect? = FetchDefaults.Redirect,
    abortController: AbortController? = null,
    bodySerializer: SerializationStrategy<B> = serializer(),
): ByteArray = putBytes(resource, body, headers, redirect, abortController, bodySerializer)

/**
 * A serialize-friendly version of [put] that expects a body and returns its response as a raw byte array.
 */
suspend inline fun <reified B> HttpFetcher.putBytes(
    resource: String,
    body: B,
    headers: Map<String, Any>? = FetchDefaults.Headers,
    redirect: RequestRedirect? = FetchDefaults.Redirect,
    abortController: AbortController? = null,
    bodySerializer: SerializationStrategy<B> = serializer(),
): ByteArray {
    return putBytes(
        resource,
        mapOf("Content-type" to "application/json") + headers.orEmpty(),
        Json.encodeToString(bodySerializer, body).encodeToByteArray(),
        redirect,
        abortController
    )
}

/**
 * A serialize-friendly version of [put] that has no body but expects a serialized response.
 */
suspend inline fun <reified R> HttpFetcher.put(
    resource: String,
    headers: Map<String, Any>? = FetchDefaults.Headers,
    redirect: RequestRedirect? = FetchDefaults.Redirect,
    abortController: AbortController? = null,
    responseDeserializer: DeserializationStrategy<R> = serializer(),
): R {
    val responseBytes = putBytes(
        resource,
        headers,
        body = null,
        redirect,
        abortController
    )

    return Json.decodeFromString(responseDeserializer, responseBytes.decodeToString())
}

/**
 * Like [put], but returns null if the request failed for any reason.
 *
 * Additionally, if [HttpFetcher.logOnError] is set to true, any failure will be logged to the console. By default, this will
 * be true for debug builds and false for release builds.
 *
 * @param body The body to send with the request. Make sure your class is marked with @Serializable or provide a custom
 *  [bodySerializer].
 */
suspend inline fun <reified B, reified R> HttpFetcher.tryPut(
    resource: String,
    body: B,
    headers: Map<String, Any>? = FetchDefaults.Headers,
    redirect: RequestRedirect? = FetchDefaults.Redirect,
    abortController: AbortController? = null,
    bodySerializer: SerializationStrategy<B> = serializer(),
    responseDeserializer: DeserializationStrategy<R> = serializer()
): R? {
    val responseBytes = tryPutBytes(
        resource,
        body,
        mapOf("Content-type" to "application/json") + headers.orEmpty(),
        redirect,
        abortController,
        bodySerializer,
    )

    if (responseBytes == null) return null
    if (R::class == Unit::class) return Unit as R

    return Json.decodeFromString(responseDeserializer, responseBytes.decodeToString())
}

/**
 * A serialize-friendly version of [tryPut] that expects a body but does not expect a serialized response.
 */
@Deprecated("DO NOT IGNORE. Please change to `tryPutBytes` instead. This method will be modified soon in a backwards incompatible way, in order to support additional cases that the current form doesn't support.", replaceWith = ReplaceWith("tryPutBytes(resource, body, headers, redirect, abortController, bodySerializer)"))
suspend inline fun <reified B> HttpFetcher.tryPut(
    resource: String,
    body: B,
    headers: Map<String, Any>? = FetchDefaults.Headers,
    redirect: RequestRedirect? = FetchDefaults.Redirect,
    abortController: AbortController? = null,
    bodySerializer: SerializationStrategy<B> = serializer(),
): ByteArray? = tryPutBytes(resource, body, headers, redirect, abortController, bodySerializer)

/**
 * A serialize-friendly version of [tryPut] that expects a body and returns its response as a raw byte array.
 */
suspend inline fun <reified B> HttpFetcher.tryPutBytes(
    resource: String,
    body: B,
    headers: Map<String, Any>? = FetchDefaults.Headers,
    redirect: RequestRedirect? = FetchDefaults.Redirect,
    abortController: AbortController? = null,
    bodySerializer: SerializationStrategy<B> = serializer(),
): ByteArray? {
    return tryPutBytes(
        resource,
        headers,
        Json.encodeToString(bodySerializer, body).encodeToByteArray(),
        redirect,
        abortController,
    )
}

/**
 * A serialize-friendly version of [tryPut] that has no body but expects a serialized response.
 */
suspend inline fun <reified R> HttpFetcher.tryPut(
    resource: String,
    headers: Map<String, Any>? = FetchDefaults.Headers,
    redirect: RequestRedirect? = FetchDefaults.Redirect,
    abortController: AbortController? = null,
    responseDeserializer: DeserializationStrategy<R> = serializer(),
): R? {
    val responseBytes = tryPutBytes(
        resource,
        headers,
        body = null,
        redirect,
        abortController
    )

    if (responseBytes == null) return null

    return Json.decodeFromString(responseDeserializer, responseBytes.decodeToString())
}

/**
 * Call PATCH on a target resource with [R] as the expected return type.
 *
 * You can set [R] to `Unit` if this request doesn't expect a response body.
 *
 * See also [tryPatch], which will return null if the request fails for any reason.
 *
 * Note: you should NOT prepend your path with "api/", as that will be added automatically.
 *
 * @param body The body to send with the request. Make sure your class is marked with @Serializable or provide a custom
 *  [bodySerializer].
 */
suspend inline fun <reified B, reified R> HttpFetcher.patch(
    resource: String,
    body: B,
    headers: Map<String, Any>? = FetchDefaults.Headers,
    redirect: RequestRedirect? = FetchDefaults.Redirect,
    abortController: AbortController? = null,
    bodySerializer: SerializationStrategy<B> = serializer(),
    responseDeserializer: DeserializationStrategy<R> = serializer()
): R {
    val responseBytes = patchBytes(
        resource,
        body,
        mapOf("Content-type" to "application/json") + headers.orEmpty(),
        redirect,
        abortController,
        bodySerializer
    )

    if (R::class == Unit::class) return Unit as R

    return Json.decodeFromString(responseDeserializer, responseBytes.decodeToString())
}

/**
 * A serialize-friendly version of [patch] that expects a body but does not expect a serialized response.
 */
@Deprecated("DO NOT IGNORE. Please change to `patchBytes` instead. This method will be modified soon in a backwards incompatible way, in order to support additional cases that the current form doesn't support.", replaceWith = ReplaceWith("patchBytes(resource, body, headers, redirect, abortController, bodySerializer)"))
suspend inline fun <reified B> HttpFetcher.patch(
    resource: String,
    body: B,
    headers: Map<String, Any>? = FetchDefaults.Headers,
    redirect: RequestRedirect? = FetchDefaults.Redirect,
    abortController: AbortController? = null,
    bodySerializer: SerializationStrategy<B> = serializer(),
): ByteArray = patchBytes(resource, body, headers, redirect, abortController, bodySerializer)

/**
 * A serialize-friendly version of [patch] that expects a body and returns its response as a raw byte array.
 */
suspend inline fun <reified B> HttpFetcher.patchBytes(
    resource: String,
    body: B,
    headers: Map<String, Any>? = FetchDefaults.Headers,
    redirect: RequestRedirect? = FetchDefaults.Redirect,
    abortController: AbortController? = null,
    bodySerializer: SerializationStrategy<B> = serializer(),
): ByteArray {
    return patchBytes(
        resource,
        mapOf("Content-type" to "application/json") + headers.orEmpty(),
        Json.encodeToString(bodySerializer, body).encodeToByteArray(),
        redirect,
        abortController
    )
}

/**
 * A serialize-friendly version of [patch] that has no body but expects a serialized response.
 */
suspend inline fun <reified R> HttpFetcher.patch(
    resource: String,
    headers: Map<String, Any>? = FetchDefaults.Headers,
    redirect: RequestRedirect? = FetchDefaults.Redirect,
    abortController: AbortController? = null,
    responseDeserializer: DeserializationStrategy<R> = serializer(),
): R {
    val responseBytes = patchBytes(
        resource,
        headers,
        body = null,
        redirect,
        abortController
    )

    return Json.decodeFromString(responseDeserializer, responseBytes.decodeToString())
}

/**
 * Like [patch], but returns null if the request failed for any reason.
 *
 * Additionally, if [HttpFetcher.logOnError] is set to true, any failure will be logged to the console. By default, this will
 * be true for debug builds and false for release builds.
 *
 * @param body The body to send with the request. Make sure your class is marked with @Serializable or provide a custom
 *  [bodySerializer].
 */
suspend inline fun <reified B, reified R> HttpFetcher.tryPatch(
    resource: String,
    body: B,
    headers: Map<String, Any>? = FetchDefaults.Headers,
    redirect: RequestRedirect? = FetchDefaults.Redirect,
    abortController: AbortController? = null,
    bodySerializer: SerializationStrategy<B> = serializer(),
    responseDeserializer: DeserializationStrategy<R> = serializer()
): R? {
    val responseBytes = tryPatchBytes(
        resource,
        body,
        mapOf("Content-type" to "application/json") + headers.orEmpty(),
        redirect,
        abortController,
        bodySerializer,
    )

    if (responseBytes == null) return null
    if (R::class == Unit::class) return Unit as R

    return Json.decodeFromString(responseDeserializer, responseBytes.decodeToString())
}

/**
 * A serialize-friendly version of [tryPatch] that expects a body but does not expect a serialized response.
 */
@Deprecated("DO NOT IGNORE. Please change to `tryPatchBytes` instead. This method will be modified soon in a backwards incompatible way, in order to support additional cases that the current form doesn't support.", replaceWith = ReplaceWith("tryPatchBytes(resource, body, headers, redirect, abortController, bodySerializer)"))
suspend inline fun <reified B> HttpFetcher.tryPatch(
    resource: String,
    body: B,
    headers: Map<String, Any>? = FetchDefaults.Headers,
    redirect: RequestRedirect? = FetchDefaults.Redirect,
    abortController: AbortController? = null,
    bodySerializer: SerializationStrategy<B> = serializer(),
): ByteArray? = tryPatchBytes(resource, body, headers, redirect, abortController, bodySerializer)

/**
 * A serialize-friendly version of [tryPatch] that expects a body and returns its response as a raw byte array.
 */
suspend inline fun <reified B> HttpFetcher.tryPatchBytes(
    resource: String,
    body: B,
    headers: Map<String, Any>? = FetchDefaults.Headers,
    redirect: RequestRedirect? = FetchDefaults.Redirect,
    abortController: AbortController? = null,
    bodySerializer: SerializationStrategy<B> = serializer(),
): ByteArray? {
    return tryPatchBytes(
        resource,
        headers,
        Json.encodeToString(bodySerializer, body).encodeToByteArray(),
        redirect,
        abortController,
    )
}

/**
 * A serialize-friendly version of [tryPatch] that has no body but expects a serialized response.
 */
suspend inline fun <reified R> HttpFetcher.tryPatch(
    resource: String,
    headers: Map<String, Any>? = FetchDefaults.Headers,
    redirect: RequestRedirect? = FetchDefaults.Redirect,
    abortController: AbortController? = null,
    responseDeserializer: DeserializationStrategy<R> = serializer(),
): R? {
    val responseBytes = tryPatchBytes(
        resource,
        headers,
        body = null,
        redirect,
        abortController
    )

    if (responseBytes == null) return null

    return Json.decodeFromString(responseDeserializer, responseBytes.decodeToString())
}

/**
 * Call DELETE on a target resource with [R] as the expected return type.
 *
 * You can set [R] to `Unit` if this request doesn't expect a response body.
 *
 * See also [tryDelete], which will return null if the request fails for any reason.
 *
 * Note: you should NOT prepend your path with "api/", as that will be added automatically.
 */
suspend inline fun <reified R> HttpFetcher.delete(
    resource: String,
    headers: Map<String, Any>? = FetchDefaults.Headers,
    redirect: RequestRedirect? = FetchDefaults.Redirect,
    abortController: AbortController? = null,
    responseDeserializer: DeserializationStrategy<R> = serializer(),
): R {
    val responseBytes = deleteBytes(
        resource,
        headers,
        redirect,
        abortController
    )

    return Json.decodeFromString(responseDeserializer, responseBytes.decodeToString())
}

/**
 * A serialize-friendly version of [tryDelete].
 */
suspend inline fun <reified R> HttpFetcher.tryDelete(
    resource: String,
    headers: Map<String, Any>? = FetchDefaults.Headers,
    redirect: RequestRedirect? = FetchDefaults.Redirect,
    abortController: AbortController? = null,
    responseDeserializer: DeserializationStrategy<R> = serializer(),
): R? {
    val responseBytes = tryDeleteBytes(
        resource,
        headers,
        redirect,
        abortController
    )

    if (responseBytes == null) return null

    return Json.decodeFromString(responseDeserializer, responseBytes.decodeToString())
}

