package com.francotte.myrecipesstore.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.francotte.myrecipesstore.domain.model.LikeableRecipe
import com.francotte.myrecipesstore.ui.compose.categories.categoriesScreen
import com.francotte.myrecipesstore.ui.compose.categories.category.categoryScreen
import com.francotte.myrecipesstore.ui.compose.categories.category.navigateToCategoryScreen
import com.francotte.myrecipesstore.ui.compose.detail.detailRecipeScreen
import com.francotte.myrecipesstore.ui.compose.detail.navigateToDetailRecipeScreen
import com.francotte.myrecipesstore.ui.compose.favorites.favoritesScreen
import com.francotte.myrecipesstore.ui.compose.favorites.login.loginScreen
import com.francotte.myrecipesstore.ui.compose.favorites.navigateToFavoriteScreen
import com.francotte.myrecipesstore.ui.compose.home.BaseRoute
import com.francotte.myrecipesstore.ui.compose.home.homeScreen
import com.francotte.myrecipesstore.ui.compose.section.navigateToSection
import com.francotte.myrecipesstore.ui.compose.section.sectionScreen

@Composable
fun NavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: Any = BaseRoute,
    onToggleFavorite:(LikeableRecipe, Boolean) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        homeScreen(
            onRecipeClick = navController::navigateToDetailRecipeScreen,
            onToggleFavorite = onToggleFavorite,
            onOpenSection = navController::navigateToSection,
            sectionDestination = {
                sectionScreen(
                    onBackClick = navController::popBackStack,
                    onRecipeClick = navController::navigateToDetailRecipeScreen,
                    onToggleFavorite = onToggleFavorite) {
                    detailRecipeScreen(onBackClick = navController::popBackStack,onToggleFavorite)
                }
            },
            recipeDetailDestination = {
                detailRecipeScreen(onBackClick = navController::popBackStack,onToggleFavorite)
            }
        )
        categoriesScreen(
            onOpenCategory = navController::navigateToCategoryScreen,
            categoryDestination = {
                categoryScreen(
                    onBackClick = navController::popBackStack,
                    onOpenRecipe = navController::navigateToDetailRecipeScreen,
                    onToggleFavorite = onToggleFavorite,
                    recipeDetailDestination = { detailRecipeScreen(onBackClick = navController::popBackStack,onToggleFavorite) })
            }
        )
        loginScreen(
            navController::popBackStack,
            navController::navigateToFavoriteScreen,
            {
                favoritesScreen(
                    onToggleFavorite = onToggleFavorite,
                    onOpenRecipe = navController::navigateToDetailRecipeScreen,
                    recipeDetailDestination = { detailRecipeScreen(onBackClick = navController::popBackStack,onToggleFavorite) })
            })
        favoritesScreen(
            onToggleFavorite = onToggleFavorite,
            onOpenRecipe = navController::navigateToDetailRecipeScreen,
            recipeDetailDestination = { detailRecipeScreen(onBackClick = navController::popBackStack,onToggleFavorite) })
    }
}


