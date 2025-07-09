package com.francotte.myrecipesstore.ui.compose.favorites


import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.francotte.myrecipesstore.domain.model.LikeableRecipe
import com.francotte.myrecipesstore.network.model.CustomRecipe
import com.francotte.myrecipesstore.util.ScreenCounter
import kotlinx.serialization.Serializable

const val FAVORITE_ROUTE = "favorite_route"

fun NavController.navigateToFavoriteScreen(navOptions: NavOptions? = null) {
    this.navigate(FAVORITE_ROUTE, navOptions)
}

fun NavGraphBuilder.favoritesScreen(onToggleFavorite:(LikeableRecipe, Boolean)->Unit, onOpenRecipe: (List<String>,Int,String) -> Unit, recipeDetailDestination: NavGraphBuilder.() -> Unit) {
    composable(route = FAVORITE_ROUTE) {
        FavoriteRoute(onRecipeClick = { ids, index, title -> onOpenRecipe(ids,index,title) }, onToggleFavorite = onToggleFavorite)
    recipeDetailDestination()
}
}

@Composable
fun FavoriteRoute(viewModel: FavViewModel = hiltViewModel(), onRecipeClick: (List<String>,Int,String) -> Unit, onToggleFavorite:(LikeableRecipe, Boolean)->Unit) {
    val favoriteUiState by viewModel.favoritesRecipesState.collectAsStateWithLifecycle()
    val searchText by viewModel.searchText.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onSearchTextChange("")
    }
    FavoritesScreen(
        favoriteUiState = favoriteUiState,
        searchText = searchText,
        onSearchTextChanged = viewModel::onSearchTextChange,
        onReload = {},
        onOpenRecipe = onRecipeClick,
        onToggleFavorite = onToggleFavorite
    )
    ScreenCounter.increment()
}


