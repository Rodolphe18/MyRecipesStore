package com.francotte.myrecipesstore.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Immutable
data class FoodColors(
    val text: Color,
    val body: Color,
    val placeholderBackground: Color,
    val primary: Color,
    val background: Color,
    val switchTrack: Color,
    val elevatedButtonBackground: Color,
    val tooltipBackground: Color,
    val primaryGradient: Brush,
    val pushTextColor: Color,
    val categoryBackground: Color,
    val favoriteButtonBackground: Color,
    val favoriteButtonIcon: Color,
    val ratingBackground: Color,
    val colorScheme: ColorScheme
) {
    companion object {
        @Stable
        val NeutralBlack = Color(0x21, 0x21, 0x21)

        @Stable
        val NeutralBlack50 = NeutralBlack.copy(alpha = 0.5f)

        @Stable
        val NeutralBlack80 = NeutralBlack.copy(alpha = 0.8f)

        @Stable
        val NeutralDarkGrey = Color(0x3C, 0x3C, 0x3C)

        @Stable
        val NeutralMediumGrey = Color(0x6D, 0x6D, 0x6D)

        @Stable
        val NeutralSoftGrey = Color(0xDA, 0xDA, 0xDA)

        @Stable
        val NeutralLightGrey = Color(0xF2, 0xF2, 0xF2)

        @Stable
        val NeutralWhite = Color.White

        @Stable
        val LightYellow = Color(0xFA, 0xCF, 0x5C)

        @Stable
        val Orange = Color(0xF8, 0x54,0x1A)

        @Stable
        val LightOrange = Color(0xFF,0xAA,0x43)

        @Stable
        val MediumGrey = Color(0x88, 0x88,0x88)

        @Stable
        val RadialGradientCenterColor = (Color(0x88, 0x88,0x88))

    }
}

@Composable
private fun primaryGradient(): Brush {
    return Brush.verticalGradient(
        listOf(
            FoodColors.LightOrange,
            FoodColors.Orange
        )
    )
}

@Composable
fun lightFoodColors(): FoodColors {
    val primary = FoodColors.Orange
    return FoodColors(
        text = FoodColors.NeutralBlack,
        body = FoodColors.NeutralMediumGrey,
        placeholderBackground = FoodColors.NeutralLightGrey,
        primary = primary,
        background = FoodColors.NeutralWhite,
        switchTrack = FoodColors.NeutralSoftGrey,
        elevatedButtonBackground = FoodColors.NeutralWhite,
        primaryGradient = primaryGradient(),
        tooltipBackground = FoodColors.NeutralWhite,
        pushTextColor = FoodColors.NeutralMediumGrey,
        categoryBackground = FoodColors.NeutralWhite,
        favoriteButtonBackground = FoodColors.NeutralWhite,
        favoriteButtonIcon = FoodColors.NeutralSoftGrey,
        ratingBackground = FoodColors.NeutralWhite,
        colorScheme = lightColorScheme(
            primary = primary,
            onPrimary = FoodColors.NeutralWhite,
            surface = FoodColors.NeutralWhite,
            surfaceVariant = FoodColors.NeutralWhite,
            onSurface = FoodColors.NeutralBlack,
            onSurfaceVariant = FoodColors.NeutralBlack,
            inverseSurface = FoodColors.NeutralBlack,
            inverseOnSurface = FoodColors.NeutralWhite,
        )
    )
}

@Composable
fun darkFoodColors(): FoodColors {
    val primary = FoodColors.Orange
    return FoodColors(
        text = FoodColors.NeutralLightGrey,
        body = FoodColors.NeutralSoftGrey,
        placeholderBackground = FoodColors.NeutralMediumGrey,
        primary = primary,
        background = FoodColors.NeutralBlack,
        switchTrack = FoodColors.NeutralMediumGrey,
        elevatedButtonBackground = FoodColors.NeutralDarkGrey,
        primaryGradient = primaryGradient(),
        tooltipBackground = FoodColors.NeutralDarkGrey,
        pushTextColor = FoodColors.NeutralWhite,
        categoryBackground = FoodColors.NeutralDarkGrey,
        favoriteButtonBackground = FoodColors.NeutralDarkGrey,
        favoriteButtonIcon = FoodColors.NeutralMediumGrey,
        ratingBackground = FoodColors.NeutralDarkGrey,
        colorScheme = darkColorScheme(
            primary = primary,
            onPrimary = FoodColors.NeutralWhite,
            surface = FoodColors.NeutralBlack,
            surfaceVariant = FoodColors.NeutralBlack,
            onSurface = FoodColors.NeutralWhite,
            onSurfaceVariant = FoodColors.NeutralWhite,
            inverseSurface = FoodColors.NeutralWhite,
            inverseOnSurface = FoodColors.NeutralBlack,
        )
    )
}


