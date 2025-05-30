package com.francotte.myrecipesstore.ui.compose.categories.category

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import com.francotte.myrecipesstore.model.AbstractRecipe
import kotlinx.serialization.Serializable

@Serializable
data class CategoryNavigationRoute(val category:String)

fun NavController.navigateToCategoryScreen(category: String, navOptions: NavOptionsBuilder.() -> Unit={}) {
    navigate(route = CategoryNavigationRoute(category)) {
        navOptions()
    }
}

fun NavGraphBuilder.categoryScreen(onOpenRecipe: (String) -> Unit, onToggleFavorite:(AbstractRecipe) -> Unit, recipeDetailDestination: NavGraphBuilder.() -> Unit) {
    composable<CategoryNavigationRoute> {
        CategoryRoute(onOpenRecipe =  { onOpenRecipe(it.idMeal) }, onToggleFavorite = onToggleFavorite)
    }
    recipeDetailDestination()
}

@Composable
fun CategoryRoute(viewModel: CategoryViewModel = hiltViewModel(), onOpenRecipe: (AbstractRecipe) -> Unit, onToggleFavorite:(AbstractRecipe)->Unit) {
    val uiState by viewModel.categoryUiState.collectAsStateWithLifecycle()

    CategoryScreen(categoryUiState = uiState, onReload =  { viewModel.reload() }, onOpenRecipe = onOpenRecipe, onToggleFavorite = onToggleFavorite)

}