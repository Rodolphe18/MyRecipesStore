package com.francotte.common

import android.net.Uri
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VideoRecorderResultBusTest {
    @Test
    fun `emitted uri is received by a collector`() = runTest {
        val bus = VideoRecorderResultBus()
        val uri = mockk<Uri>()
        var received: Uri? = null
        val job = launch { bus.results.collect { received = it } }
        runCurrent()
        bus.emit(uri)
        runCurrent()
        assertEquals(uri, received)
        job.cancel()
    }
}
