package com.francotte.myrecipesstore.navigation

import android.view.Window
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import com.francotte.add_recipe.addRecipeScreen
import com.francotte.categories.categoriesScreen
import com.francotte.categories.categoryScreen
import com.francotte.categories.navigateToCategoryScreen
import com.francotte.detail.deepLinkRecipeScreen
import com.francotte.detail.detailRecipeScreen
import com.francotte.detail.navigateToDetailRecipeScreen
import com.francotte.favorites.customRecipeDetailScreen
import com.francotte.favorites.favoritesScreen
import com.francotte.favorites.navigateToCustomRecipeDetailScreen
import com.francotte.favorites.navigateToFavoriteScreen
import com.francotte.home.HOME_ROUTE
import com.francotte.home.homeScreen
import com.francotte.login.loginScreen
import com.francotte.login.navigateToLoginScreen
import com.francotte.model.LikeableRecipe
import com.francotte.myrecipesstore.ui.AppState
import com.francotte.profile.profileScreen
import com.francotte.register.navigateToRegisterScreen
import com.francotte.register.registerScreen
import com.francotte.reset.navigateToRequestResetPasswordScreen
import com.francotte.reset.navigateToResetPasswordScreen
import com.francotte.reset.requestResetPasswordScreen
import com.francotte.reset.resetPasswordScreen
import com.francotte.search.result_mode.navigateToSearchModeScreen
import com.francotte.search.result_mode.searchModeScreen
import com.francotte.search.result_recipe.navigateToSearchRecipesScreen
import com.francotte.search.result_recipe.searchRecipesScreen
import com.francotte.search.searchScreen
import com.francotte.section.navigateToSection
import com.francotte.section.sectionScreen
import com.francotte.settings.premiumScreen
import com.francotte.video.navigateToVideoFullScreen
import com.francotte.video.videoFullScreen

@Composable
fun BaseNavHost(
    appState: AppState,
    modifier: Modifier = Modifier,
    windowSizeClass: WindowSizeClass,
    window: Window,
    onToggleFavorite: (LikeableRecipe, Boolean) -> Unit,
    isAuthenticated: Boolean,
    customRecipeHasBeenUpdated: Boolean,
) {
    val navController = appState.navController

    val alreadyNavigated = remember { mutableStateOf(false) }
    LaunchedEffect(appState.resetPasswordToken) {
        val token = appState.resetPasswordToken
        if (token != null && !alreadyNavigated.value) {
            alreadyNavigated.value = true
            navController.navigateToResetPasswordScreen(token)
        }
    }

    NavHost(
        navController = navController,
        startDestination = HOME_ROUTE,
        modifier = modifier,
    ) {
        homeScreen(
            onRecipeClick = navController::navigateToDetailRecipeScreen,
            onToggleFavorite = onToggleFavorite,
            onOpenSection = navController::navigateToSection,
            onVideoButtonClick = navController::navigateToVideoFullScreen,
            windowSizeClass = windowSizeClass
        )
        categoriesScreen(windowSizeClass, navController::navigateToCategoryScreen)
        categoryScreen(windowSizeClass, navController::popBackStack, navController::navigateToDetailRecipeScreen, onToggleFavorite)
        addRecipeScreen(isAuthenticated) { navController.navigateToLoginScreen() }
        searchModeScreen(navController::navigateToSearchRecipesScreen, navController::popBackStack)
        sectionScreen(navController::popBackStack,windowSizeClass, navController::navigateToDetailRecipeScreen, onToggleFavorite)
        searchRecipesScreen(windowSizeClass,navController::navigateToDetailRecipeScreen, onToggleFavorite, navController::popBackStack)
        searchScreen(onSearchModeSelected = navController::navigateToSearchModeScreen)
        loginScreen(
            onRegister = navController::navigateToRegisterScreen,
            navigateToFavoriteScreen = navController::navigateToFavoriteScreen,
            onOpenResetPassword = navController::navigateToRequestResetPasswordScreen,
        )
        registerScreen(navController::popBackStack, navController::navigateToFavoriteScreen)
        favoritesScreen(
            windowSizeClass,
            onToggleFavorite,
            navController::navigateToDetailRecipeScreen,
            navController::navigateToCustomRecipeDetailScreen,
            customRecipeHasBeenUpdated
        )
        deepLinkRecipeScreen(navController::popBackStack, onToggleFavorite)
        detailRecipeScreen(navController::popBackStack, onToggleFavorite)
        customRecipeDetailScreen(navController::popBackStack)
        videoFullScreen(window)
        profileScreen(navController::popBackStack)
        requestResetPasswordScreen(navController::popBackStack)
        resetPasswordScreen()
        premiumScreen(navController::popBackStack)
    }
}

