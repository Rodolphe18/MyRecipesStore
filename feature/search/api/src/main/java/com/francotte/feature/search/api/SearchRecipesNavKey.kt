package com.francotte.feature.search.api

import androidx.navigation3.runtime.NavKey
import com.francotte.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
data class SearchRecipesNavKey(val item: String, val mode: SearchMode) : NavKey

fun Navigator.navigateToSearchRecipes(item: String, mode: SearchMode) {
    navigate(SearchRecipesNavKey(item,mode))
}
