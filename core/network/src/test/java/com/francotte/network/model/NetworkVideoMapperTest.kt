package com.francotte.network.model

import com.francotte.model.VideoStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class NetworkVideoMapperTest {

    @Test
    fun `maps a known status and fields`() {
        val v = NetworkVideo("id1", "READY", "https://x/master.m3u8", 3.0).asExternalModel()
        assertEquals(VideoStatus.READY, v.status)
        assertEquals("https://x/master.m3u8", v.manifestUrl)
        assertEquals(3.0, v.durationSec!!, 0.001)
    }

    @Test
    fun `unknown status falls back to FAILED`() {
        val v = NetworkVideo("id1", "SOMETHING_ELSE", null, null).asExternalModel()
        assertEquals(VideoStatus.FAILED, v.status)
    }
}
