package com.francotte.myrecipesstore.ui.navigation

import android.net.Uri
import android.util.Log
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.francotte.myrecipesstore.domain.model.LikeableRecipe
import com.francotte.myrecipesstore.manager.FavoriteManager
import com.francotte.myrecipesstore.network.model.Ingredient
import com.francotte.myrecipesstore.ui.compose.add_recipe.addRecipeScreen
import com.francotte.myrecipesstore.ui.compose.categories.CATEGORIES_ROUTE
import com.francotte.myrecipesstore.ui.compose.categories.categoriesScreen
import com.francotte.myrecipesstore.ui.compose.categories.category.categoryScreen
import com.francotte.myrecipesstore.ui.compose.categories.category.navigateToCategoryScreen
import com.francotte.myrecipesstore.ui.compose.detail.deepLinkRecipeScreen
import com.francotte.myrecipesstore.ui.compose.detail.detailRecipeScreen
import com.francotte.myrecipesstore.ui.compose.detail.navigateToDetailRecipeScreen
import com.francotte.myrecipesstore.ui.compose.favorites.favoritesScreen
import com.francotte.myrecipesstore.ui.compose.favorites.login.loginScreen
import com.francotte.myrecipesstore.ui.compose.favorites.navigateToFavoriteScreen
import com.francotte.myrecipesstore.ui.compose.favorites.register.navigateToRegisterScreen
import com.francotte.myrecipesstore.ui.compose.favorites.register.registerScreen
import com.francotte.myrecipesstore.ui.compose.home.BASE_ROUTE
import com.francotte.myrecipesstore.ui.compose.home.homeScreen
import com.francotte.myrecipesstore.ui.compose.search.result_mode.navigateToSearchModeScreen
import com.francotte.myrecipesstore.ui.compose.search.result_mode.searchModeScreen
import com.francotte.myrecipesstore.ui.compose.search.result_recipes.navigateToSearchRecipesScreen
import com.francotte.myrecipesstore.ui.compose.search.result_recipes.searchRecipesScreen
import com.francotte.myrecipesstore.ui.compose.search.searchScreen
import com.francotte.myrecipesstore.ui.compose.section.navigateToSection
import com.francotte.myrecipesstore.ui.compose.section.sectionScreen
import com.francotte.myrecipesstore.ui.compose.video.navigateToVideoFullScreen
import com.francotte.myrecipesstore.ui.compose.video.videoFullScreen
import com.francotte.myrecipesstore.util.ScreenCounter

@Composable
fun NavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    windowSizeClass: WindowSizeClass,
    startDestination: String = BASE_ROUTE,
    onToggleFavorite: (LikeableRecipe, Boolean) -> Unit,
    onAddRecipe: (title: String, ingredients: List<Ingredient>, instructions: String, images: List<Uri>) -> Unit,
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
                    onToggleFavorite = onToggleFavorite
                ) {
                    detailRecipeScreen(onBackClick = navController::popBackStack, onToggleFavorite)
                }
            },
            recipeDetailDestination = {
                detailRecipeScreen(onBackClick = navController::popBackStack, onToggleFavorite)
            },
            onVideoButtonClick = navController::navigateToVideoFullScreen,
            videoDestination = {
                videoFullScreen()
            },
            windowSizeClass = windowSizeClass
        )
        categoriesScreen(
            windowSizeClass= windowSizeClass,
            onOpenCategory = navController::navigateToCategoryScreen,
            categoryDestination = {
                categoryScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onOpenRecipe = navController::navigateToDetailRecipeScreen,
                    onToggleFavorite = onToggleFavorite,
                    recipeDetailDestination = {
                        detailRecipeScreen(onBackClick = {
                            navController.popBackStack()
                        }, onToggleFavorite = onToggleFavorite)
                    },
                    windowSizeClass = windowSizeClass)
            }
        )
        addRecipeScreen(onAddRecipe)
        searchScreen(onSearchModeSelected = navController::navigateToSearchModeScreen) {
            searchModeScreen(
                onItemSelected = navController::navigateToSearchRecipesScreen,
                onBack = navController::popBackStack
            ) {
                searchRecipesScreen(
                    onOpenRecipe = navController::navigateToDetailRecipeScreen,
                    onToggleFavorite = onToggleFavorite,
                    onBack = navController::popBackStack,
                    detailRecipeDestination = {
                        detailRecipeScreen(
                            onBackClick = navController::popBackStack,
                            onToggleFavorite
                        )
                    },
                    windowSizeClass = windowSizeClass)
            }
        }
        loginScreen(
            onRegister = navController::navigateToRegisterScreen,
            navigateToFavoriteScreen = navController::navigateToFavoriteScreen,
            registerScreenDestination = {
                registerScreen(
                    onBackPressed = navController::popBackStack,
                    navigateToFavoriteScreen = navController::navigateToFavoriteScreen
                ) {
                    favoritesScreen(
                        onToggleFavorite = onToggleFavorite,
                        windowSizeClass = windowSizeClass,
                        onOpenRecipe = navController::navigateToDetailRecipeScreen,
                        recipeDetailDestination = {
                            detailRecipeScreen(
                                onBackClick = navController::popBackStack,
                                onToggleFavorite
                            )
                        })
                }
            },
            favoriteScreenDestination = {
                favoritesScreen(
                    onToggleFavorite = onToggleFavorite,
                    onOpenRecipe = navController::navigateToDetailRecipeScreen,
                    recipeDetailDestination = {
                        detailRecipeScreen(
                            onBackClick = navController::popBackStack,
                            onToggleFavorite
                        )
                    }, windowSizeClass = windowSizeClass)
            }
        )
        favoritesScreen(
            onToggleFavorite = onToggleFavorite,
            onOpenRecipe = navController::navigateToDetailRecipeScreen,
            recipeDetailDestination = {
                detailRecipeScreen(
                    onBackClick = navController::popBackStack,
                    onToggleFavorite
                )
            },
            windowSizeClass = windowSizeClass
        )
        deepLinkRecipeScreen(
            onBackClick = navController::popBackStack,
            onToggleFavorite = onToggleFavorite
        )
    }
}



