package com.francotte.designsystem.component


import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.francotte.designsystem.theme.LightYellow

@Stable
fun whiteYellowVerticalGradient(): Brush {
    return Brush.verticalGradient(
        colors = listOf(
            LightYellow.copy(alpha = 0.1f),
            Color.White

        ),
        startY = Float.POSITIVE_INFINITY,
        endY = 0f
    )
}