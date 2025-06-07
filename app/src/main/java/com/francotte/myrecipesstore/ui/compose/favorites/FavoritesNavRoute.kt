package com.francotte.myrecipesstore.ui.compose.favorites


import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.francotte.myrecipesstore.model.AbstractRecipe
import com.francotte.myrecipesstore.model.LikeableRecipe
import kotlinx.serialization.Serializable

@Serializable
object FavoritesNavigationRoute

fun NavController.navigateToFavoriteScreen(navOptions: NavOptions? = null) {
    this.navigate(FavoritesNavigationRoute, navOptions)
}

fun NavGraphBuilder.favoritesScreen(onToggleFavorite:(LikeableRecipe,Boolean)->Unit,onOpenRecipe: (String) -> Unit, recipeDetailDestination: NavGraphBuilder.() -> Unit) {
    composable<FavoritesNavigationRoute> {
        FavoriteRoute(onRecipeClick = { onOpenRecipe(it.idMeal) }, onToggleFavorite = onToggleFavorite)
    recipeDetailDestination()
}
}

@Composable
fun FavoriteRoute(viewModel: FavViewModel = hiltViewModel(), onRecipeClick: (AbstractRecipe) -> Unit, onToggleFavorite:(LikeableRecipe, Boolean)->Unit) {
    val favoriteUiState by viewModel.favoritesRecipesState.collectAsStateWithLifecycle()
    FavoritesScreen(
        favoriteUiState = favoriteUiState,
        onReload = {},
        onOpenRecipe = onRecipeClick,
        onToggleFavorite
    )
}


