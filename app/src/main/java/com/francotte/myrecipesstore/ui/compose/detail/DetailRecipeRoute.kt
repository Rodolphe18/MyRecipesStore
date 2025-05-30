package com.francotte.myrecipesstore.ui.compose.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
data class DetailRecipeRoute(val idMeal: String)

fun NavController.navigateToDetailRecipeScreen(mealId: String, navOptions: NavOptionsBuilder.() -> Unit = {}) {
    navigate(route = DetailRecipeRoute(mealId)) {
        navOptions()
    }
}

fun NavGraphBuilder.detailRecipeScreen(onBackClick:() -> Unit) {
    composable<DetailRecipeRoute> {
        DetailRecipeRoute()
    }
}

@Composable
internal fun DetailRecipeRoute(viewModel: DetailRecipeViewModel= hiltViewModel()) {
    val mealState by viewModel.recipe.collectAsStateWithLifecycle()
    DetailRecipeScreen(mealState)
}