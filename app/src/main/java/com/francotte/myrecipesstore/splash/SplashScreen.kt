package com.francotte.myrecipesstore.splash

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.francotte.ui.R


@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.orange)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_recipe_launcher),
            contentDescription = "Logo",
            modifier = Modifier.size(250.dp)
        )
    }
}

@Composable
fun ApplyAdSystemBarsFix(isAdShowing: Boolean) {
    val activity = LocalActivity.current
    val window = activity?.window ?: return
    val view = LocalView.current

    // Sauvegarde pour restaurer
    val oldStatusColor = remember { window.statusBarColor }
    val oldNavColor = remember { window.navigationBarColor }

    DisposableEffect(isAdShowing) {
        val controller = WindowCompat.getInsetsController(window, view)

        if (isAdShowing) {
            // 1) ne plus dessiner derrière la status bar
            WindowCompat.setDecorFitsSystemWindows(window, true)

            // 2) barre du haut noire opaque
            window.statusBarColor = android.graphics.Color.BLACK
            controller.isAppearanceLightStatusBars = false
        } else {
            // restauration
            WindowCompat.setDecorFitsSystemWindows(window, false)
            window.statusBarColor = oldStatusColor
            window.navigationBarColor = oldNavColor
        }

        onDispose { }
    }
}