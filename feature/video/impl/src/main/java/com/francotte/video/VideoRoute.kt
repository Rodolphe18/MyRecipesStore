package com.francotte.video

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.francotte.common.counters.ScreenCounter
import com.francotte.feature.video.api.VideoNavKey


fun EntryProviderScope<NavKey>.videoEntry() {
    entry<VideoNavKey> { key ->
        VideoRoute(
            viewModel = hiltViewModel<VideoViewModel, VideoViewModel.Factory>(
                key = key.youTubeUrl,
            ) { factory ->
                factory.create(key.youTubeUrl)
            },
        )
    }
}


@Composable
fun VideoRoute(
    viewModel: VideoViewModel,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    VideoFullScreen(videoId = state.videoId)
    LaunchedEffect(Unit) {
        ScreenCounter.increment()
    }
}
