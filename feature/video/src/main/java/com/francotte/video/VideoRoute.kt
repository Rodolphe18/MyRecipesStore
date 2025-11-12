package com.francotte.video

import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.francotte.common.ScreenCounter
import com.francotte.common.findActivity
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


fun NavGraphBuilder.videoFullScreen() {
    composable<VideoRoute> {
        val youTubeId = it.toRoute<VideoRoute>().youTubeUrl
        VideoFullScreen(youtubeUrl = youTubeId)
    }
}




