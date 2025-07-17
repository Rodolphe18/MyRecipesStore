package com.francotte.myrecipesstore.ui.compose.user_recipes

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import androidx.compose.runtime.getValue
import com.francotte.myrecipesstore.network.model.Ingredient


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