package com.varabyte.kobweb.io

import com.varabyte.truthish.assertThat
import com.varabyte.truthish.assertThrows
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class ByteSourceTest {
    @Test
    fun canReadByteSourceInChunks() = runTest {
        val buffer = ByteArray(4)
        val source = RawByteSource(byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12))

        // Full copy (fill buffer)
        assertThat(source.read(buffer)).isEqualTo(4)
        assertThat(buffer).containsAllIn(1, 2, 3, 4).inOrder()

        // Partial copy (reduced length)
        assertThat(source.read(buffer, length = 2)).isEqualTo(2)
        assertThat(buffer).containsAllIn(5, 6, 3, 4).inOrder()

        // Partial copy (from offset)
        assertThat(source.read(buffer, offset = 2)).isEqualTo(2)
        assertThat(buffer).containsAllIn(5, 6, 7, 8).inOrder()

        // Partial copy (offset and length)
        assertThat(source.read(buffer, offset = 1, length = 2)).isEqualTo(2)
        assertThat(buffer).containsAllIn(5, 9, 10, 8).inOrder()

        // No-op copy
        assertThat(source.read(buffer, length = 0)).isEqualTo(0)
        assertThat(buffer).containsAllIn(5, 9, 10, 8).inOrder()

        // Partial read (length > num elements remaining)
        assertThat(source.read(buffer)).isEqualTo(2)
        assertThat(buffer).containsAllIn(11, 12, 10, 8).inOrder()

        // Data source is empty
        assertThat(source.read(buffer)).isEqualTo(-1)
        assertThat(source.read(buffer)).isEqualTo(-1)
    }

    @Test
    fun emptyContentImmediatelyReturnsEof() = runTest {
        val buffer = ByteArray(4)
        val emptySource = ByteSource.empty()
        assertThat(emptySource.read(buffer)).isEqualTo(-1)
        assertThat(buffer).isEqualTo(ByteArray(4))
    }

    @Test
    fun canReadRemainingBytesFromAByteSource() = runTest {
        val source = RawByteSource("123 Hello world".toByteArray())
        val temp = ByteArray(4)
        assertThat(source.read(temp)).isEqualTo(4)
        assertThat(source.readRemaining()).isEqualTo("Hello world".toByteArray())
    }

    @Test
    fun canConvertByteSourceIntoInputStream() = runTest {
        val source = RawByteSource(byteArrayOf(1, 2, 3, 4, 5))
        val inputStream = source.toInputStream()

        assertThat(inputStream.read()).isEqualTo(1)
        assertThat(inputStream.read()).isEqualTo(2)

        val temp = ByteArray(100)
        assertThat(inputStream.read(temp)).isEqualTo(3)
        assertThat(temp).containsAllIn(3, 4, 5).inOrder()

        assertThat(inputStream.read()).isEqualTo(-1)
    }

    @Test
    fun testOutOfBoundsExceptions() = runTest {
        val smallArray = ByteArray(4)
        val source = RawByteSource(byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))

        assertThrows<IndexOutOfBoundsException> { source.read(smallArray, offset = -1) }
        assertThrows<IndexOutOfBoundsException> { source.read(smallArray, offset = 999) }
        assertThrows<IndexOutOfBoundsException> { source.read(smallArray, length = -1) }
        assertThrows<IndexOutOfBoundsException> { source.read(smallArray, length = 999) }

        // After recovering, you can still read data as normal
        assertThat(source.read(smallArray)).isEqualTo(4)
        assertThat(smallArray).containsAllIn(1, 2, 3, 4).inOrder()
    }
}