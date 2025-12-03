package com.varabyte.kobweb.io

import kotlinx.coroutines.runBlocking
import java.io.ByteArrayOutputStream
import java.io.Closeable
import java.io.IOException
import java.io.InputStream

/**
 * A byte source represents a source of byte data that can only be consumed one time.
 */
// Dev note: We created this class because we needed a suspendable async byte stream concept so we could abstract over
// ktor's ByteReadChannel without exposing it to Kobweb users directly. We considered using kotlinx-io's `Source` class
// but that is blocking. We may revisit the decision to use this class if kotlinx-io ever exposes their own async
// classes. See also: https://github.com/Kotlin/kotlinx-io/issues/163
interface ByteSource : Closeable {
    companion object {
        fun empty(): ByteSource = RawByteSource(byteArrayOf())
    }

    /**
     * The length of this byte source, if fixed and known, or null otherwise.
     */
    val length: Long? get() = null

    /**
     * Transfer up to [length] bytes, writing them into the target [buffer] array at the specified [offset].
     *
     * Every time read is called, this byte source is partially consumed. It is NOT safe to call this method from
     * multiple threads.
     *
     * Depending on the implementation of this interface, the actual source of data may stall (e.g. if it is fetching
     * bytes from the network), at which point this method will suspend. If you need blocking behavior, you can of
     * course wrap the call in [runBlocking], but you can also consider converting it into an [InputStream] using
     * [toInputStream] if you prefer.
     *
     * It is up to the caller to ensure that [offset] and [length] are specified so that data won't attempt to write
     * outside the bounds of the target [buffer], or else the code will throw.
     *
     * @return the number of bytes read, or -1 to indicate EOF (end of data reached; channel closed; etc.)
     */
    suspend fun read(buffer: ByteArray, offset: Int = 0, length: Int = buffer.size - offset): Int
}

/**
 * Read all remaining bytes out of the receiving [ByteSource].
 *
 * @param limit If set and the byte source is larger than it, throws an [IOException].
 */
suspend fun ByteSource.readRemaining(limit: Int? = null): ByteArray {
    val result = ByteArrayOutputStream(limit ?: (1024 * 1024))
    val chunk = ByteArray(64 * 1024)
    var totalBytesRead = 0
    while (true) {
        val bytesRead = read(chunk)
        if (bytesRead < 0) break
        if (bytesRead > 0) {
            result.write(chunk, 0, bytesRead)
            totalBytesRead += bytesRead
            if (limit != null && totalBytesRead > limit)
                throw IOException("Byte source larger than specified limit of $limit bytes")
        }
    }
    return result.toByteArray()
}

/**
 * Convert this byte source into an [InputStream].
 */
fun ByteSource.toInputStream(): InputStream {
    val byteSource = this
    return object : InputStream() {
        private val singleByte = ByteArray(1)

        override fun read(): Int {
            val bytesRead = runBlocking { byteSource.read(singleByte) }
            return if (bytesRead < 0) -1 else singleByte[0].toInt() and 0xFF
        }

        // Override because our impl is more performant by delegating directly to `ByteSource`
        override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
            return runBlocking { byteSource.read(buffer, offset, length) }
        }

        override fun close() {
            byteSource.close()
        }
    }
}

fun InputStream.toByteSource(): ByteSource {
    val inputStream = this
    return object : ByteSource {
        override suspend fun read(buffer: ByteArray, offset: Int, length: Int): Int {
            return inputStream.read(buffer, offset, length)
        }

        override fun close() {
            inputStream.close()
        }
    }
}

/**
 * A [ByteSource] that wraps a simple [ByteArray] buffer.
 */
class RawByteSource(private val bytes: ByteArray) : ByteSource {
    @Volatile private var closed = false
    private var pos = 0

    override val length = bytes.size.toLong()

    override suspend fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        if (closed) return -1
        if (pos >= bytes.size) return -1
        val bytesToCopy = minOf(length, bytes.size - pos)
        bytes.copyInto(buffer, offset, startIndex = pos, endIndex = pos + bytesToCopy)
        pos += bytesToCopy
        return bytesToCopy
    }

    override fun close() {
        closed = true
    }
}