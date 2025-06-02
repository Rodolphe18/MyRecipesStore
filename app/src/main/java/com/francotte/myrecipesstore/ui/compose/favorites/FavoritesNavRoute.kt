package com.francotte.myrecipesstore.ui.compose.favorites


import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.francotte.myrecipesstore.model.AbstractRecipe
import kotlinx.serialization.Serializable

@Serializable
object FavoritesNavigationRoute

fun NavController.navigateToFavoriteScreen(navOptions: NavOptions? = null) {
    this.navigate(FavoritesNavigationRoute, navOptions)
}

fun NavGraphBuilder.favoritesScreen(onToggleFavorite:(AbstractRecipe)->Unit,onOpenRecipe: (String) -> Unit, recipeDetailDestination: NavGraphBuilder.() -> Unit) {
    composable<FavoritesNavigationRoute> {
//        FavoritesRoute(onOpenRecipe =  {
//            onOpenRecipe(it.idMeal)
//        }, onToggleFavorite = onToggleFavorite)
    }
    recipeDetailDestination()
}

//@Composable
//fun FavoritesRoute(viewModel: FavoritesViewModel = hiltViewModel(), onOpenRecipe: (AbstractRecipe) -> Unit, onToggleFavorite: (AbstractRecipe) -> Unit) {
//    val homeUiState by viewModel
//   FavoritesScreen(favoritesUiState = homeUiState, onOpenCategory = onOpenRecipe, onReload =  { viewModel.reload() })
//}