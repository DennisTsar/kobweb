package com.varabyte.kobweb.browser

import com.varabyte.kobweb.browser.http.AbortController
import com.varabyte.kobweb.browser.http.FetchDefaults
import com.varabyte.kobweb.browser.http.fetch
import com.varabyte.kobweb.browser.http.http
import com.varabyte.kobweb.navigation.BasePath
import kotlinx.browser.window
import org.w3c.dom.Window
import org.w3c.fetch.RequestRedirect

/**
 * A class which makes it easier to access a Kobweb API endpoint, instead of using [Window.fetch] directly.
 *
 * This class works by wrapping [Window.http] but specifically for Kobweb API calls (URLs which are prefixed with
 * "/api/").
 */
@Suppress("MemberVisibilityCanBePrivate") // It's an API...
class ApiFetcher(private val window: Window) {
    /**
     * If true, when using any of the "try" methods, log any errors, if they occur, to the console.
     *
     * This is a useful way to debug what happened because otherwise the exception will be silently swallowed.
     *
     * This value will be set to true if you are running on a debug build, but it will default to false otherwise.
     */
    var logOnError: Boolean by window.http::logOnError

    private fun toResource(apiPath: String): String {
        return BasePath.prependTo("/api/${apiPath.trimStart('/')}")
    }

    /**
     * Call DELETE on a target API path.
     *
     * See also [tryDelete], which will return null if the request fails for any reason.
     *
     * Note: you should NOT prepend your path with "api/", as that will be added automatically.
     */
    @Deprecated("DO NOT IGNORE. Please change to `deleteBytes` instead. This method will be modified soon in a backwards incompatible way, in order to support additional cases that the current form doesn't support.", replaceWith = ReplaceWith("deleteBytes(apiPath, headers, redirect, abortController)"))
    suspend fun delete(
        apiPath: String,
        headers: Map<String, Any>? = FetchDefaults.Headers,
        redirect: RequestRedirect? = FetchDefaults.Redirect,
        abortController: AbortController? = null,
    ): ByteArray = deleteBytes(apiPath, headers, redirect, abortController)

    /**
     * Call DELETE on a target API path, returning the response body as a raw array of bytes.
     *
     * See also [tryDelete], which will return null if the request fails for any reason.
     *
     * Note: you should NOT prepend your path with "api/", as that will be added automatically.
     */
    suspend fun deleteBytes(
        apiPath: String,
        headers: Map<String, Any>? = FetchDefaults.Headers,
        redirect: RequestRedirect? = FetchDefaults.Redirect,
        abortController: AbortController? = null,
    ): ByteArray = window.http.deleteBytes(toResource(apiPath), headers, redirect, abortController)

    /**
     * Like [delete], but returns null if the request failed for any reason.
     *
     * Additionally, if [logOnError] is set to true, any failure will be logged to the console. By default, this will
     * be true for debug builds and false for release builds.
     */
    @Deprecated("DO NOT IGNORE. Please change to `tryDeleteBytes` instead. This method will be modified soon in a backwards incompatible way, in order to support additional cases that the current form doesn't support.", replaceWith = ReplaceWith("tryDeleteBytes(apiPath, headers, redirect, abortController)"))
    suspend fun tryDelete(
        apiPath: String,
        headers: Map<String, Any>? = FetchDefaults.Headers,
        redirect: RequestRedirect? = FetchDefaults.Redirect,
        abortController: AbortController? = null,
    ): ByteArray? = tryDeleteBytes(apiPath, headers, redirect, abortController)

    /**
     * Like [delete], but returns null if the request failed for any reason.
     *
     * Additionally, if [logOnError] is set to true, any failure will be logged to the console. By default, this will
     * be true for debug builds and false for release builds.
     */
    suspend fun tryDeleteBytes(
        apiPath: String,
        headers: Map<String, Any>? = FetchDefaults.Headers,
        redirect: RequestRedirect? = FetchDefaults.Redirect,
        abortController: AbortController? = null,
    ): ByteArray? = window.http.tryDeleteBytes(toResource(apiPath), headers, redirect, abortController)

    /**
     * Call GET on a target API path.
     *
     * See also [tryGet], which will return null if the request fails for any reason.
     *
     * Note: you should NOT prepend your path with "api/", as that will be added automatically.
     */
    @Deprecated("DO NOT IGNORE. Please change to `getBytes` instead. This method will be modified soon in a backwards incompatible way, in order to support additional cases that the current form doesn't support.", replaceWith = ReplaceWith("getBytes(apiPath, headers, redirect, abortController)"))
    suspend fun get(
        apiPath: String,
        headers: Map<String, Any>? = FetchDefaults.Headers,
        redirect: RequestRedirect? = FetchDefaults.Redirect,
        abortController: AbortController? = null,
    ): ByteArray = getBytes(apiPath, headers, redirect, abortController)

    /**
     * Call GET on a target API path, returning the response body as a raw array of bytes.
     *
     * See also [tryGet], which will return null if the request fails for any reason.
     *
     * Note: you should NOT prepend your path with "api/", as that will be added automatically.
     */
    suspend fun getBytes(
        apiPath: String,
        headers: Map<String, Any>? = FetchDefaults.Headers,
        redirect: RequestRedirect? = FetchDefaults.Redirect,
        abortController: AbortController? = null,
    ): ByteArray = window.http.getBytes(toResource(apiPath), headers, redirect, abortController)

    /**
     * Like [get], but returns null if the request failed for any reason.
     *
     * Additionally, if [logOnError] is set to true, any failure will be logged to the console. By default, this will
     * be true for debug builds and false for release builds.
     */
    @Deprecated("DO NOT IGNORE. Please change to `tryGetBytes` instead. This method will be modified soon in a backwards incompatible way, in order to support additional cases that the current form doesn't support.", replaceWith = ReplaceWith("tryGetBytes(apiPath, headers, redirect, abortController)"))
    suspend fun tryGet(
        apiPath: String,
        headers: Map<String, Any>? = FetchDefaults.Headers,
        redirect: RequestRedirect? = FetchDefaults.Redirect,
        abortController: AbortController? = null,
    ): ByteArray? = tryGetBytes(apiPath, headers, redirect, abortController)

    /**
     * Like [get], but returns null if the request failed for any reason.
     *
     * Additionally, if [logOnError] is set to true, any failure will be logged to the console. By default, this will
     * be true for debug builds and false for release builds.
     */
    suspend fun tryGetBytes(
        apiPath: String,
        headers: Map<String, Any>? = FetchDefaults.Headers,
        redirect: RequestRedirect? = FetchDefaults.Redirect,
        abortController: AbortController? = null,
    ): ByteArray? = window.http.tryGetBytes(toResource(apiPath), headers, redirect, abortController)

    /**
     * Call HEAD on a target API path.
     *
     * See also [tryHead], which will return null if the request fails for any reason.
     *
     * Note: you should NOT prepend your path with "api/", as that will be added automatically.
     */
    @Deprecated("DO NOT IGNORE. Please change to `headBytes` instead. This method will be modified soon in a backwards incompatible way, in order to support additional cases that the current form doesn't support.", replaceWith = ReplaceWith("headBytes(apiPath, headers, redirect, abortController)"))
    suspend fun head(
        apiPath: String,
        headers: Map<String, Any>? = FetchDefaults.Headers,
        redirect: RequestRedirect? = FetchDefaults.Redirect,
        abortController: AbortController? = null,
    ): ByteArray = headBytes(apiPath, headers, redirect, abortController)

    /**
     * Call HEAD on a target API path, returning the response body as a raw array of bytes.
     *
     * See also [tryHead], which will return null if the request fails for any reason.
     *
     * Note: you should NOT prepend your path with "api/", as that will be added automatically.
     */
    suspend fun headBytes(
        apiPath: String,
        headers: Map<String, Any>? = FetchDefaults.Headers,
        redirect: RequestRedirect? = FetchDefaults.Redirect,
        abortController: AbortController? = null,
    ): ByteArray = window.http.headBytes(toResource(apiPath), headers, redirect, abortController)

    /**
     * Like [head], but returns null if the request failed for any reason.
     *
     * Additionally, if [logOnError] is set to true, any failure will be logged to the console. By default, this will
     * be true for debug builds and false for release builds.
     */
    @Deprecated("DO NOT IGNORE. Please change to `tryHeadBytes` instead. This method will be modified soon in a backwards incompatible way, in order to support additional cases that the current form doesn't support.", replaceWith = ReplaceWith("tryHeadBytes(apiPath, headers, redirect, abortController)"))
    suspend fun tryHead(
        apiPath: String,
        headers: Map<String, Any>? = FetchDefaults.Headers,
        redirect: RequestRedirect? = FetchDefaults.Redirect,
        abortController: AbortController? = null,
    ): ByteArray? = tryHeadBytes(apiPath, headers, redirect, abortController)

    /**
     * Like [head], but returns null if the request failed for any reason.
     *
     * Additionally, if [logOnError] is set to true, any failure will be logged to the console. By default, this will
     * be true for debug builds and false for release builds.
     */
    suspend fun tryHeadBytes(
        apiPath: String,
        headers: Map<String, Any>? = FetchDefaults.Headers,
        redirect: RequestRedirect? = FetchDefaults.Redirect,
        abortController: AbortController? = null,
    ): ByteArray? = window.http.tryHeadBytes(toResource(apiPath), headers, redirect, abortController)

    /**
     * Call OPTIONS on a target API path.
     *
     * See also [tryOptions], which will return null if the request fails for any reason.
     *
     * Note: you should NOT prepend your path with "api/", as that will be added automatically.
     */
    @Deprecated("DO NOT IGNORE. Please change to `optionsBytes` instead. This method will be modified soon in a backwards incompatible way, in order to support additional cases that the current form doesn't support.", replaceWith = ReplaceWith("optionsBytes(apiPath, headers, redirect, abortController)"))
    suspend fun options(
        apiPath: String,
        headers: Map<String, Any>? = FetchDefaults.Headers,
        redirect: RequestRedirect? = FetchDefaults.Redirect,
        abortController: AbortController? = null,
    ): ByteArray = optionsBytes(apiPath, headers, redirect, abortController)

    /**
     * Call OPTIONS on a target API path, returning the response body as a raw array of bytes.
     *
     * See also [tryOptions], which will return null if the request fails for any reason.
     *
     * Note: you should NOT prepend your path with "api/", as that will be added automatically.
     */
    suspend fun optionsBytes(
        apiPath: String,
        headers: Map<String, Any>? = FetchDefaults.Headers,
        redirect: RequestRedirect? = FetchDefaults.Redirect,
        abortController: AbortController? = null,
    ): ByteArray = window.http.optionsBytes(toResource(apiPath), headers, redirect, abortController)

    /**
     * Like [options], but returns null if the request failed for any reason.
     *
     * Additionally, if [logOnError] is set to true, any failure will be logged to the console. By default, this will
     * be true for debug builds and false for release builds.
     */
    @Deprecated("DO NOT IGNORE. Please change to `tryOptionsBytes` instead. This method will be modified soon in a backwards incompatible way, in order to support additional cases that the current form doesn't support.", replaceWith = ReplaceWith("tryOptionsBytes(apiPath, headers, redirect, abortController)"))
    suspend fun tryOptions(
        apiPath: String,
        headers: Map<String, Any>? = FetchDefaults.Headers,
        redirect: RequestRedirect? = FetchDefaults.Redirect,
        abortController: AbortController? = null,
    ): ByteArray? = tryOptionsBytes(apiPath, headers, redirect, abortController)

    /**
     * Like [options], but returns null if the request failed for any reason.
     *
     * Additionally, if [logOnError] is set to true, any failure will be logged to the console. By default, this will
     * be true for debug builds and false for release builds.
     */
    suspend fun tryOptionsBytes(
        apiPath: String,
        headers: Map<String, Any>? = FetchDefaults.Headers,
        redirect: RequestRedirect? = FetchDefaults.Redirect,
        abortController: AbortController? = null,
    ): ByteArray? = window.http.tryOptionsBytes(toResource(apiPath), headers, redirect, abortController)

    /**
     * Call PATCH on a target API path.
     *
     * See also [tryPatch], which will return null if the request fails for any reason.
     *
     * Note: you should NOT prepend your path with "api/", as that will be added automatically.
     */
    @Deprecated("DO NOT IGNORE. Please change to `patchBytes` instead. This method will be modified soon in a backwards incompatible way, in order to support additional cases that the current form doesn't support.", replaceWith = ReplaceWith("patchBytes(apiPath, headers, body, redirect, abortController)"))
    suspend fun patch(
        apiPath: String,
        headers: Map<String, Any>? = FetchDefaults.Headers,
        body: ByteArray? = null,
        redirect: RequestRedirect? = FetchDefaults.Redirect,
        abortController: AbortController? = null,
    ): ByteArray = patchBytes(apiPath, headers, body, redirect, abortController)

    /**
     * Call PATCH on a target API path, returning the response body as a raw array of bytes.
     *
     * If a request body is provided, it is also specified as a raw array of bytes.
     *
     * See also [tryPatch], which will return null if the request fails for any reason.
     *
     * Note: you should NOT prepend your path with "api/", as that will be added automatically.
     */
    suspend fun patchBytes(
        apiPath: String,
        headers: Map<String, Any>? = FetchDefaults.Headers,
        body: ByteArray? = null,
        redirect: RequestRedirect? = FetchDefaults.Redirect,
        abortController: AbortController? = null,
    ): ByteArray = window.http.patchBytes(toResource(apiPath), headers, body, redirect, abortController)

    /**
     * Like [patch], but returns null if the request failed for any reason.
     *
     * Additionally, if [logOnError] is set to true, any failure will be logged to the console. By default, this will
     * be true for debug builds and false for release builds.
     */
    @Deprecated("DO NOT IGNORE. Please change to `tryPatchBytes` instead. This method will be modified soon in a backwards incompatible way, in order to support additional cases that the current form doesn't support.", replaceWith = ReplaceWith("tryPatchBytes(apiPath, headers, body, redirect, abortController)"))
    suspend fun tryPatch(
        apiPath: String,
        headers: Map<String, Any>? = FetchDefaults.Headers,
        body: ByteArray? = null,
        redirect: RequestRedirect? = FetchDefaults.Redirect,
        abortController: AbortController? = null,
    ): ByteArray? = tryPatchBytes(apiPath, headers, body, redirect, abortController)

    /**
     * Like [patch], but returns null if the request failed for any reason.
     *
     * Additionally, if [logOnError] is set to true, any failure will be logged to the console. By default, this will
     * be true for debug builds and false for release builds.
     */
    suspend fun tryPatchBytes(
        apiPath: String,
        headers: Map<String, Any>? = FetchDefaults.Headers,
        body: ByteArray? = null,
        redirect: RequestRedirect? = FetchDefaults.Redirect,
        abortController: AbortController? = null,
    ): ByteArray? = window.http.tryPatchBytes(toResource(apiPath), headers, body, redirect, abortController)

    /**
     * Call POST on a target API path.
     *
     * See also [tryPost], which will return null if the request fails for any reason.
     *
     * Note: you should NOT prepend your path with "api/", as that will be added automatically.
     */
    @Deprecated("DO NOT IGNORE. Please change to `postBytes` instead. This method will be modified soon in a backwards incompatible way, in order to support additional cases that the current form doesn't support.", replaceWith = ReplaceWith("postBytes(apiPath, headers, body, redirect, abortController)"))
    suspend fun post(
        apiPath: String,
        headers: Map<String, Any>? = FetchDefaults.Headers,
        body: ByteArray? = null,
        redirect: RequestRedirect? = FetchDefaults.Redirect,
        abortController: AbortController? = null,
    ): ByteArray = postBytes(apiPath, headers, body, redirect, abortController)

    /**
     * Call POST on a target API path, returning the response body as a raw array of bytes.
     *
     * If a request body is provided, it is also specified as a raw array of bytes.
     *
     * See also [tryPost], which will return null if the request fails for any reason.
     *
     * Note: you should NOT prepend your path with "api/", as that will be added automatically.
     */
    suspend fun postBytes(
        apiPath: String,
        headers: Map<String, Any>? = FetchDefaults.Headers,
        body: ByteArray? = null,
        redirect: RequestRedirect? = FetchDefaults.Redirect,
        abortController: AbortController? = null,
    ): ByteArray = window.http.postBytes(toResource(apiPath), headers, body, redirect, abortController)

    /**
     * Like [post], but returns null if the request failed for any reason.
     *
     * Additionally, if [logOnError] is set to true, any failure will be logged to the console. By default, this will
     * be true for debug builds and false for release builds.
     */
    @Deprecated("DO NOT IGNORE. Please change to `tryPostBytes` instead. This method will be modified soon in a backwards incompatible way, in order to support additional cases that the current form doesn't support.", replaceWith = ReplaceWith("tryPostBytes(apiPath, headers, body, redirect, abortController)"))
    suspend fun tryPost(
        apiPath: String,
        headers: Map<String, Any>? = FetchDefaults.Headers,
        body: ByteArray? = null,
        redirect: RequestRedirect? = FetchDefaults.Redirect,
        abortController: AbortController? = null,
    ): ByteArray? = tryPostBytes(apiPath, headers, body, redirect, abortController)

    /**
     * Like [post], but returns null if the request failed for any reason.
     *
     * Additionally, if [logOnError] is set to true, any failure will be logged to the console. By default, this will
     * be true for debug builds and false for release builds.
     */
    suspend fun tryPostBytes(
        apiPath: String,
        headers: Map<String, Any>? = FetchDefaults.Headers,
        body: ByteArray? = null,
        redirect: RequestRedirect? = FetchDefaults.Redirect,
        abortController: AbortController? = null,
    ): ByteArray? = window.http.tryPostBytes(toResource(apiPath), headers, body, redirect, abortController)

    /**
     * Call PUT on a target API path.
     *
     * See also [tryPut], which will return null if the request fails for any reason.
     *
     * Note: you should NOT prepend your path with "api/", as that will be added automatically.
     */
    @Deprecated("DO NOT IGNORE. Please change to `putBytes` instead. This method will be modified soon in a backwards incompatible way, in order to support additional cases that the current form doesn't support.", replaceWith = ReplaceWith("putBytes(apiPath, headers, body, redirect, abortController)"))
    suspend fun put(
        apiPath: String,
        headers: Map<String, Any>? = FetchDefaults.Headers,
        body: ByteArray? = null,
        redirect: RequestRedirect? = FetchDefaults.Redirect,
        abortController: AbortController? = null,
    ): ByteArray = putBytes(apiPath, headers, body, redirect, abortController)

    /**
     * Call PUT on a target API path, returning the response body as a raw array of bytes.
     *
     * If a request body is provided, it is also specified as a raw array of bytes.
     *
     * See also [tryPut], which will return null if the request fails for any reason.
     *
     * Note: you should NOT prepend your path with "api/", as that will be added automatically.
     */
    suspend fun putBytes(
        apiPath: String,
        headers: Map<String, Any>? = FetchDefaults.Headers,
        body: ByteArray? = null,
        redirect: RequestRedirect? = FetchDefaults.Redirect,
        abortController: AbortController? = null,
    ): ByteArray = window.http.putBytes(toResource(apiPath), headers, body, redirect, abortController)

    /**
     * Like [put], but returns null if the request failed for any reason.
     *
     * Additionally, if [logOnError] is set to true, any failure will be logged to the console. By default, this will
     * be true for debug builds and false for release builds.
     */
    @Deprecated("DO NOT IGNORE. Please change to `tryPutBytes` instead. This method will be modified soon in a backwards incompatible way, in order to support additional cases that the current form doesn't support.", replaceWith = ReplaceWith("tryPutBytes(apiPath, headers, body, redirect, abortController)"))
    suspend fun tryPut(
        apiPath: String,
        headers: Map<String, Any>? = FetchDefaults.Headers,
        body: ByteArray? = null,
        redirect: RequestRedirect? = FetchDefaults.Redirect,
        abortController: AbortController? = null,
    ): ByteArray? = tryPutBytes(apiPath, headers, body, redirect, abortController)

    /**
     * Like [put], but returns null if the request failed for any reason.
     *
     * Additionally, if [logOnError] is set to true, any failure will be logged to the console. By default, this will
     * be true for debug builds and false for release builds.
     */
    suspend fun tryPutBytes(
        apiPath: String,
        headers: Map<String, Any>? = FetchDefaults.Headers,
        body: ByteArray? = null,
        redirect: RequestRedirect? = FetchDefaults.Redirect,
        abortController: AbortController? = null,
    ): ByteArray? = window.http.tryPutBytes(toResource(apiPath), headers, body, redirect, abortController)
}

@Suppress("unused") // We tie our class to the "Window" class on purpose, so it can be used instead of `fetch`
val Window.api by lazy { ApiFetcher(window) }
