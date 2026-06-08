package com.francotte.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import com.francotte.ads.BannerAdProvider

val LocalShouldShowBanners = compositionLocalOf { true }

val LocalBannerProvider =
    staticCompositionLocalOf<BannerAdProvider> {
        error("BannerAdProvider not provided")
    }

val LocalSnackbarHostState = staticCompositionLocalOf<SnackbarHostState> {
    error("SnackbarHostState state should be initialized at runtime")
}
