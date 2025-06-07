package com.francotte.myrecipesstore.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable


@Composable
fun FoodTheme(content: @Composable () -> Unit) {
    val foodColors = if (isSystemInDarkTheme()) darkFoodColors() else lightFoodColors()
    MaterialTheme(colorScheme = foodColors.colorScheme) { content() }
}

