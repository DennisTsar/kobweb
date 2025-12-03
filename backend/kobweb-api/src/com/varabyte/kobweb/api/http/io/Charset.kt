package com.varabyte.kobweb.api.http.io

import java.nio.charset.Charset
import java.nio.charset.IllegalCharsetNameException
import java.nio.charset.UnsupportedCharsetException

// See if the charset was encoded inside the content type string, e.g. "plain/text; charset=utf-8"
internal fun String?.parseCharsetFromContentType(defaultCharset: Charset = Charsets.UTF_8): Charset {
    if (this == null) return defaultCharset

    val contentType = this // for readability
    // case-insensitive search for `charset=...` OR `charset="..."`, skipping over whitespace
    val regex = Regex("""(?i)\s*charset\s*=\s*(?:"([^"]+)"|([^;\s"]+))""")
    val contentTypeParams = contentType.substringAfter(';', missingDelimiterValue = "")
    val match = regex.find(contentTypeParams)
    val name = run {
        val quotedName = match?.groups?.get(1)?.value
        val unquotedName = match?.groups?.get(2)?.value?.trim()
        quotedName ?: unquotedName
    }

    return try {
        name?.let { Charset.forName(it) }
    } catch (_: IllegalCharsetNameException) {
        null
    } catch (_: UnsupportedCharsetException) {
        null
    } ?: defaultCharset
}