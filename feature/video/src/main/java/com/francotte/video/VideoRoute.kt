package com.francotte.video

import android.view.Window
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable

@Serializable
data class VideoRoute(val youTubeUrl: String)

fun NavController.navigateToVideoFullScreen(
    youTubeId: String,
    navOptions: NavOptionsBuilder.() -> Unit = {}
) {
    navigate(route = VideoRoute(youTubeId)) {
        navOptions()
    }
}


fun NavGraphBuilder.videoFullScreen(window: Window) {
    composable<VideoRoute> {
        val youTubeId = it.toRoute<VideoRoute>().youTubeUrl
        VideoFullScreen(youtubeUrl = youTubeId, window = window)
    }
}

fun NavDestination?.isFullscreen(): Boolean =
    this?.hasRoute<VideoRoute>() == true




