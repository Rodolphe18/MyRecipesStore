package com.francotte.myrecipesstore.ui.compose.categories.category

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
data class CategoryNavigationRoute(val category:String)

fun NavController.navigateToCategoryScreen(category: String, navOptions: NavOptionsBuilder.() -> Unit={}) {
    navigate(route = CategoryNavigationRoute(category)) {
        navOptions()
    }
}

fun NavGraphBuilder.categoryScreen(onBackClick: () -> Unit, onOpenRecipe: (String,String) -> Unit, onToggleFavorite:(LikeableRecipe, Boolean) -> Unit, recipeDetailDestination: NavGraphBuilder.() -> Unit) {
    composable<CategoryNavigationRoute> {
        CategoryRoute(onOpenRecipe =  { onOpenRecipe(it.recipe.idMeal,it.recipe.strMeal) }, onToggleFavorite = onToggleFavorite, onBack= onBackClick)
    }
    recipeDetailDestination()
}

@Composable
fun CategoryRoute(viewModel: CategoryViewModel = hiltViewModel(), onOpenRecipe: (LikeableRecipe) -> Unit, onToggleFavorite:(LikeableRecipe, Boolean)->Unit, onBack:()->Unit) {
    val uiState by viewModel.categoryUiState.collectAsStateWithLifecycle()
    val title = viewModel.category

    CategoryScreen(categoryUiState = uiState, title = title, onReload =  { viewModel.reload() }, onOpenRecipe = onOpenRecipe, onToggleFavorite = onToggleFavorite, onBack)

}