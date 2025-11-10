package com.francotte.favorites

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import com.francotte.network.model.Ingredient
import kotlinx.serialization.Serializable


@Serializable
data class CustomRecipeDetailRoute(val id: String)

fun NavController.navigateToCustomRecipeDetailScreen(id:String, navOptions: NavOptionsBuilder.() -> Unit = {}) {
    this.navigate(CustomRecipeDetailRoute(id), navOptions)
}

fun NavGraphBuilder.customRecipeDetailScreen(
    onBackClick:() -> Unit,
    onSubmit: (recipeId: String, title: String, ingredients: List<Ingredient>, instructions: String, image: Uri?) -> Unit
) {
    composable<CustomRecipeDetailRoute> {
       CustomRecipeDetailRoute(onBackCLick = onBackClick, onSubmit = onSubmit)
    }
}

@Composable
internal fun CustomRecipeDetailRoute(viewModel: CustomRecipeDetailViewModel= hiltViewModel(), onBackCLick:() -> Unit, onSubmit: (recipeId: String, title: String, ingredients: List<Ingredient>, instructions: String, image: Uri?) -> Unit) {
    val customRecipe by viewModel.recipe.collectAsStateWithLifecycle()
    CustomRecipeDetailScreen(viewModel = viewModel, customRecipe = customRecipe, onBackCLick= onBackCLick, onSubmit= onSubmit)
}