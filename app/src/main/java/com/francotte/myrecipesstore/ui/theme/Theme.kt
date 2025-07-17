package com.francotte.myrecipesstore.ui.theme

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@VisibleForTesting
val LightColorScheme = lightColorScheme(
    primary = Orange,
    onPrimary = NeutralWhite,
    surface = NeutralWhite,
    surfaceVariant = NeutralWhite,
    onSurface = Color.DarkGray,
    onSurfaceVariant = NeutralBlack,
    inverseSurface = NeutralBlack,
    inverseOnSurface = NeutralWhite,
    background = NeutralWhite,
    tertiary = NeutralWhite,
    onTertiary = NeutralSoftGrey,
    secondary = NeutralBlack,
    onSecondary = NeutralMediumGrey,
    primaryContainer = Color.DarkGray
)


@VisibleForTesting
val DarkColorScheme = darkColorScheme(
    primary = Orange,
    onPrimary = NeutralWhite,
    surface = NeutralBlack,
    surfaceVariant = NeutralBlack,
    onSurface = NeutralLightGrey,
    onSurfaceVariant = NeutralWhite,
    inverseSurface = NeutralWhite,
    inverseOnSurface = NeutralBlack,
    background = NeutralBlack,
    tertiary = NeutralDarkGrey,
    onTertiary = NeutralMediumGrey,
    secondary = NeutralLightGrey,
    onSecondary = NeutralSoftGrey,
    primaryContainer = Color.LightGray
)


@Composable
fun FoodTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

