package com.francotte.video

import androidx.lifecycle.ViewModel
import com.francotte.domain.YouTubeUrlParser
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel(assistedFactory = VideoViewModel.Factory::class)
class VideoViewModel @AssistedInject constructor(@Assisted youtubeUrl: String) : ViewModel() {

    val state: StateFlow<VideoState> =
        MutableStateFlow(VideoState(videoId = YouTubeUrlParser.extractVideoId(youtubeUrl)))

    @AssistedFactory
    interface Factory {
        fun create(youtubeUrl: String): VideoViewModel
    }
}

data class VideoState(
    val videoId: String = "",
)
