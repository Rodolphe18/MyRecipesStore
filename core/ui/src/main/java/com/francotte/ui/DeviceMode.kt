package com.francotte.ui

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowWidthSizeClass

enum class DeviceMode {
    PhonePortrait,
    PhoneLandscape,
    TabletPortrait,
    TabletLandscape,
}

/**
 * Dérive le [DeviceMode] depuis `WindowSizeClass` (via [currentWindowAdaptiveInfo]) — seule source
 * d'adaptativité de l'app. On conserve les 4 cas historiques en combinant largeur + hauteur :
 * une fenêtre courte (hauteur Compact) correspond à un téléphone en paysage, une largeur Expanded
 * (avec fenêtre haute) à une tablette en paysage, etc.
 *
 * À appeler directement dans chaque écran qui en a besoin (pas de CompositionLocal global).
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun rememberDeviceMode(): DeviceMode {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val width = windowSizeClass.windowWidthSizeClass
    val height = windowSizeClass.windowHeightSizeClass
    return remember(width, height) {
        val compactWidth = width == WindowWidthSizeClass.COMPACT
        val expandedWidth = width == WindowWidthSizeClass.EXPANDED
        val compactHeight = height == WindowHeightSizeClass.COMPACT
        when {
            compactHeight -> DeviceMode.PhoneLandscape
            compactWidth -> DeviceMode.PhonePortrait
            expandedWidth -> DeviceMode.TabletLandscape
            else -> DeviceMode.TabletPortrait
        }
    }
}
