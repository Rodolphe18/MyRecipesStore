package com.francotte.feature.video.api

import androidx.navigation3.runtime.NavKey
import com.francotte.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
data class VideoNavKey(val youTubeUrl: String) : NavKey

fun Navigator.navigateToVideo(youTubeUrl: String) {
    navigate(VideoNavKey(youTubeUrl))
}
