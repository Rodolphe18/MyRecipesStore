package com.francotte.video

import android.view.Window
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun HideNavigationBar(window: Window) {
    val view = LocalView.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    DisposableEffect(window, view, lifecycle) {
        val controller = WindowCompat.getInsetsController(window, view)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) controller.hide(WindowInsetsCompat.Type.systemBars())
        }
        lifecycle.addObserver(observer)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        onDispose {
            lifecycle.removeObserver(observer)
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }
}