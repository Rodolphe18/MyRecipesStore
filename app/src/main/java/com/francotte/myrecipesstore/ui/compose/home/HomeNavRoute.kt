package com.francotte.myrecipesstore.ui.compose.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.francotte.myrecipesstore.domain.model.LikeableRecipe
import kotlinx.serialization.Serializable

@Serializable
object HomeNavigationRoute

@Serializable
object BaseRoute

fun NavController.navigateToHomeScreen(navOptions: NavOptions? = null) {
    this.navigate(HomeNavigationRoute, navOptions)
}

fun NavGraphBuilder.homeScreen(onRecipeClick: (String,String) -> Unit, onToggleFavorite: (LikeableRecipe, Boolean) -> Unit, onOpenSection: (String) -> Unit, sectionDestination: NavGraphBuilder.() -> Unit, recipeDetailDestination: NavGraphBuilder.() -> Unit) {
    navigation<BaseRoute>(startDestination = HomeNavigationRoute) {
        composable<HomeNavigationRoute> {
            HomeRoute(onRecipeClick = { onRecipeClick(it.recipe.idMeal, it.recipe.strMeal) }, onToggleFavorite= onToggleFavorite, onOpenSection = { onOpenSection(it) })
        }
        sectionDestination()
        recipeDetailDestination()
    }
}

@Composable
fun HomeRoute(viewModel: HomeViewModel= hiltViewModel(), onRecipeClick: (LikeableRecipe) -> Unit, onToggleFavorite:(LikeableRecipe, Boolean)->Unit, onOpenSection: (String) -> Unit) {
    val homeUiState by viewModel.homeUiState.collectAsStateWithLifecycle()
    HomeScreen(homeUiState = homeUiState, onOpenRecipe = onRecipeClick, onOpenSection = onOpenSection, onReload =  { viewModel.reload() }, onToggleFavorite = onToggleFavorite)
}