package com.francotte.screenshot_testing

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
object RobolectricTestWindowSizes {

    fun compactPhone(): WindowSizeClass =
        WindowSizeClass.calculateFromSize(DpSize(width = 360.dp, height = 640.dp))

    fun landscapePhone(): WindowSizeClass =
        WindowSizeClass.calculateFromSize(DpSize(width = 840.dp, height = 480.dp)) // souvent Medium
}