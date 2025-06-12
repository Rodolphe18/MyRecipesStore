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

const val HOME_ROUTE = "home"
const val BASE_ROUTE = "base"

fun NavController.navigateToHomeScreen(navOptions: NavOptions? = null) {
    this.navigate(HOME_ROUTE, navOptions)
}

fun NavGraphBuilder.homeScreen(onRecipeClick: (List<String>,Int, String) -> Unit, onToggleFavorite: (LikeableRecipe, Boolean) -> Unit, onOpenSection: (String) -> Unit, sectionDestination: NavGraphBuilder.() -> Unit, recipeDetailDestination: NavGraphBuilder.() -> Unit, videoDestination: NavGraphBuilder.() -> Unit,onVideoButtonClick:(String)->Unit) {
    navigation(startDestination = HOME_ROUTE, route = BASE_ROUTE) {
        composable(route = HOME_ROUTE) {
            HomeRoute(onRecipeClick = { ids, index, title -> onRecipeClick(ids,index, title) }, onToggleFavorite= onToggleFavorite, onOpenSection = { onOpenSection(it) }, onVideoButtonClick = onVideoButtonClick)
        }
        sectionDestination()
        recipeDetailDestination()
        videoDestination()
    }
}

@Composable
fun HomeRoute(viewModel: HomeViewModel= hiltViewModel(), onRecipeClick: (List<String>,Int,String) -> Unit, onToggleFavorite:(LikeableRecipe, Boolean)->Unit, onOpenSection: (String) -> Unit, onVideoButtonClick:(String)->Unit) {
    val homeUiState by viewModel.homeUiState.collectAsStateWithLifecycle()
    HomeScreen(homeUiState = homeUiState, onOpenRecipe = onRecipeClick, onOpenSection = onOpenSection, onReload =  { viewModel.reload() }, onToggleFavorite = onToggleFavorite, onVideoButtonClick = onVideoButtonClick)
}