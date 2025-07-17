package com.francotte.myrecipesstore.ui.compose.composables

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass


val WindowWidthSizeClass.nbHomeColumns: Int
    get() = when (this) {
        WindowWidthSizeClass.Compact -> 1
        WindowWidthSizeClass.Medium -> 2
        else -> 2
    }

val WindowWidthSizeClass.nbCategoriesColumns: Int
    get() = when (this) {
        WindowWidthSizeClass.Compact -> 3
        WindowWidthSizeClass.Medium -> 4
        else -> 4
    }

val WindowWidthSizeClass.nbSectionColumns: Int
    get() = when (this) {
        WindowWidthSizeClass.Compact -> 2
        WindowWidthSizeClass.Medium -> 4
        else -> 4
    }



