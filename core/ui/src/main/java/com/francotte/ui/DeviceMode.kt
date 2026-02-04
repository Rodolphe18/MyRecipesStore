package com.francotte.ui

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration

enum class DeviceMode {
    PhonePortrait,
    PhoneLandscape,
    TabletPortrait,
    TabletLandscape,
}

@Composable
fun rememberDeviceMode(): DeviceMode {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isTabletLike = configuration.smallestScreenWidthDp >= 600

    return when {
        !isTabletLike && !isLandscape -> DeviceMode.PhonePortrait
        !isTabletLike && isLandscape -> DeviceMode.PhoneLandscape
        isTabletLike && !isLandscape -> DeviceMode.TabletPortrait
        else -> DeviceMode.TabletLandscape
    }
}



@Immutable
data class AppLayoutInfo(
    val mode: DeviceMode,
)

@Composable
fun ProvideDeviceMode(content: @Composable () -> Unit) {
    val mode = rememberDeviceMode()
    val info = remember(mode) { AppLayoutInfo(mode = mode) }

    CompositionLocalProvider(LocalAppLayout provides info) {
        content()
    }
}
