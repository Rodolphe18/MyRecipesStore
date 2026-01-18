package com.francotte.api

import androidx.navigation3.runtime.NavKey
import com.francotte.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
data class CustomRecipeNavKey(val recipeId: String?) : NavKey

fun Navigator.navigateToCustomRecipe(id:String?) {
    navigate(CustomRecipeNavKey(id))
}
