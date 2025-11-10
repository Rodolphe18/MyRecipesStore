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
        VideoFullScreen(youTubeUrl = youTubeId)
    }
}

@Composable
fun VideoFullScreen(
    youTubeUrl: String
) {
    val context = LocalContext.current
    val activity = context.findActivity()
    val videoId = youTubeUrl.substringAfter("v=").substringBefore("&")
    val viewGroup = remember { mutableStateOf<ViewGroup?>(null) }

    if (videoId.isBlank()) return

    val embedUrl = "https://www.youtube.com/embed/$videoId?autoplay=1&enablejsapi=1"

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = {
                WebView(it).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    settings.apply {
                        javaScriptEnabled = true
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        domStorageEnabled = true
                        mediaPlaybackRequiresUserGesture = false
                    }

                    webViewClient = WebViewClient()

                    webChromeClient = object : WebChromeClient() {
                        private var customView: View? = null
                        private var customViewCallback: CustomViewCallback? = null

                        override fun onShowCustomView(view: View, callback: CustomViewCallback) {
                            customView = view
                            customViewCallback = callback
                            viewGroup.value = activity?.window?.decorView as? ViewGroup
                            viewGroup.value?.apply {
                                addView(
                                    view,
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                            }
                        }

                        override fun onHideCustomView() {
                            viewGroup.value?.apply {
                                customView?.let { removeView(it) }
                            }
                            customView = null
                            customViewCallback?.onCustomViewHidden()
                        }
                    }

                    loadUrl(embedUrl)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
    LaunchedEffect(Unit) {
        ScreenCounter.increment()
    }
}


