package com.varabyte.kobweb.api.io

import com.varabyte.kobweb.api.http.io.parseCharsetFromContentType
import com.varabyte.truthish.assertThat
import kotlin.test.Test

class CharsetTest {
    @Test
    fun charsetParsingTests() {
        assertThat("text/plain".parseCharsetFromContentType()).isEqualTo(Charsets.UTF_8)
        assertThat("text/plain".parseCharsetFromContentType(defaultCharset = Charsets.US_ASCII)).isEqualTo(Charsets.US_ASCII)

        assertThat("text/plain; charset=utf-16".parseCharsetFromContentType()).isEqualTo(Charsets.UTF_16)
        assertThat("text/plain; charset = utf-16".parseCharsetFromContentType()).isEqualTo(Charsets.UTF_16)
        assertThat("text/plain; charset=\"utf-16\"".parseCharsetFromContentType()).isEqualTo(Charsets.UTF_16)

        // Case-insensitive
        assertThat("text/plain; Charset=UTF-16".parseCharsetFromContentType()).isEqualTo(Charsets.UTF_16)
        assertThat("text/plain; CHARSET=\"UTF-16\"".parseCharsetFromContentType()).isEqualTo(Charsets.UTF_16)

        // Multiple parameters
        assertThat("multipart/form-data; boundary=xyz; charset=\"utf-32\"".parseCharsetFromContentType()).isEqualTo(Charsets.UTF_32)
        assertThat("multipart/form-data; charset=utf-32; boundary=xyz".parseCharsetFromContentType()).isEqualTo(Charsets.UTF_32)
        assertThat("multipart/form-data; key=value; charset=\"utf-32\"; boundary=xyz".parseCharsetFromContentType()).isEqualTo(Charsets.UTF_32)

        // Fallback for invalid cases
        assertThat("text/plain; charset=".parseCharsetFromContentType()).isEqualTo(Charsets.UTF_8)
        assertThat("text/plain; charset=invalid".parseCharsetFromContentType()).isEqualTo(Charsets.UTF_8)
    }
}