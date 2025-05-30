package com.francotte.myrecipesstore.ui.compose.section

import androidx.annotation.StringRes
import com.francotte.myrecipesstore.R

enum class SectionType(@StringRes val titleRes: Int) { LATEST_RECIPES(R.string.latest_recipes), TOP_RECIPES(R.string.top_recipes) }