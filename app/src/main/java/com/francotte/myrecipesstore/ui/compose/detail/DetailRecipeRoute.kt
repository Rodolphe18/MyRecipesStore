package com.francotte.myrecipesstore.ui.compose.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import com.francotte.myrecipesstore.domain.model.LikeableRecipe
import kotlinx.serialization.Serializable

@Serializable
data class DetailRecipeRoute(val recipeId: String, val recipeTitle:String)

fun NavController.navigateToDetailRecipeScreen(mealId: String,recipeTitle:String, navOptions: NavOptionsBuilder.() -> Unit = {}) {
    navigate(route = DetailRecipeRoute(mealId, recipeTitle)) {
        navOptions()
    }
}

fun NavGraphBuilder.detailRecipeScreen(onBackClick:() -> Unit,onToggleFavorite:(LikeableRecipe, Boolean)->Unit) {
    composable<DetailRecipeRoute> {
        DetailRecipeRoute(onBackClick = onBackClick, onToggleFavorite = onToggleFavorite)
    }
}

@Composable
internal fun DetailRecipeRoute(viewModel: DetailRecipeViewModel= hiltViewModel(), onBackClick:() -> Unit,onToggleFavorite:(LikeableRecipe, Boolean)->Unit) {
    val mealState by viewModel.recipe.collectAsStateWithLifecycle()
    DetailRecipeScreen(mealState,onToggleFavorite, viewModel.recipeName, onBackClick)
}