package com.francotte.video

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.francotte.feature.video.api.VideoNavKey
import kotlinx.serialization.Serializable



fun EntryProviderScope<NavKey>.videoEntry() {
    entry<VideoNavKey> {
        val youTubeUrl = it.youTubeUrl
        VideoFullScreen(youtubeUrl = youTubeUrl)
    }
}

