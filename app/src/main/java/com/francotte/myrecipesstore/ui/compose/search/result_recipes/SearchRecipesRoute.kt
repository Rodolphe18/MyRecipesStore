package com.francotte.myrecipesstore.ui.compose.search.result_recipes

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import com.francotte.myrecipesstore.domain.model.LikeableRecipe
import com.francotte.myrecipesstore.ui.compose.search.SearchMode
import kotlinx.serialization.Serializable

@Serializable
data class SearchRecipesNavRoute(val item:String, val mode:SearchMode)

fun NavController.navigateToSearchRecipesScreen(item:String,mode: SearchMode, navOptions: NavOptionsBuilder.() -> Unit={}) {
    navigate(route = SearchRecipesNavRoute(item,mode)) {
        navOptions()
    }
}

fun NavGraphBuilder.searchRecipesScreen(windowSizeClass: WindowSizeClass,onOpenRecipe:(List<String>, Int, String) -> Unit, onToggleFavorite:(LikeableRecipe, Boolean) -> Unit, onBack:()->Unit, detailRecipeDestination: NavGraphBuilder.() -> Unit) {
    composable<SearchRecipesNavRoute> {
        SearchRecipesRoute(windowSizeClass = windowSizeClass,onOpenRecipe=onOpenRecipe, onToggleFavorite = onToggleFavorite, onBack = onBack)
    }
    detailRecipeDestination()
}

@Composable
fun SearchRecipesRoute(viewModel: SearchRecipesViewModel = hiltViewModel(),windowSizeClass: WindowSizeClass, onOpenRecipe:(List<String>, Int, String) -> Unit, onToggleFavorite:(LikeableRecipe, Boolean) -> Unit, onBack:()->Unit) {
    val uiState by viewModel.searchRecipesUiState.collectAsStateWithLifecycle()
    val title = viewModel.item
    SearchRecipesScreen(searchRecipesUiState = uiState,windowSizeClass = windowSizeClass, title= title,onReload = {}, onOpenRecipe = onOpenRecipe, onToggleFavorite = onToggleFavorite, onBack = onBack)
}