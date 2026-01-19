package com.francotte.video

import android.view.Window
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.francotte.feature.video.api.VideoNavKey
import com.francotte.navigation.Navigator
import kotlinx.serialization.Serializable



fun EntryProviderScope<NavKey>.videoEntry(window: Window) {
    entry<VideoNavKey> {
        val youTubeUrl = it.youTubeUrl
        VideoFullScreen(youtubeUrl = youTubeUrl, window = window)
    }
}

