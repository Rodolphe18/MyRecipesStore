package com.francotte.myrecipesstore.navigation


import android.view.Window
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
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
import com.francotte.home.BASE_ROUTE
import com.francotte.home.homeScreen
import com.francotte.login.loginScreen
import com.francotte.login.navigateToLoginScreen
import com.francotte.model.LikeableRecipe
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
fun NavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    windowSizeClass: WindowSizeClass,
    window: Window,
    startDestination: String = BASE_ROUTE,
    onToggleFavorite: (LikeableRecipe, Boolean) -> Unit,
    isAuthenticated: Boolean,
    customRecipeHasBeenUpdated: Boolean,
    resetPasswordToken: String? = null
) {

    val alreadyNavigated = remember { mutableStateOf(false) }

    LaunchedEffect(resetPasswordToken) {
        if (resetPasswordToken != null && !alreadyNavigated.value) {
            alreadyNavigated.value = true
            navController.navigateToResetPasswordScreen(resetPasswordToken)
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        homeScreen(
            onRecipeClick = navController::navigateToDetailRecipeScreen,
            onToggleFavorite = onToggleFavorite,
            onOpenSection = navController::navigateToSection,
            onVideoButtonClick = navController::navigateToVideoFullScreen,
            windowSizeClass = windowSizeClass
        )
        categoriesScreen(
            windowSizeClass = windowSizeClass,
            onOpenCategory = navController::navigateToCategoryScreen,
        )
        categoryScreen(windowSizeClass,navController::popBackStack,navController::navigateToDetailRecipeScreen,onToggleFavorite)
        addRecipeScreen(isAuthenticated, { navController.navigateToLoginScreen() })
        searchModeScreen(
            onItemSelected = navController::navigateToSearchRecipesScreen,
            onBack = navController::popBackStack
        )
        sectionScreen(
            onBackClick = navController::popBackStack,
            onRecipeClick = navController::navigateToDetailRecipeScreen,
            onToggleFavorite = onToggleFavorite,
            windowSizeClass = windowSizeClass
        )
        searchRecipesScreen(
            onOpenRecipe = navController::navigateToDetailRecipeScreen,
            onToggleFavorite = onToggleFavorite,
            onBack = navController::popBackStack,
            windowSizeClass = windowSizeClass
        )
        searchScreen(onSearchModeSelected = navController::navigateToSearchModeScreen)
        loginScreen(
            onRegister = navController::navigateToRegisterScreen,
            navigateToFavoriteScreen = navController::navigateToFavoriteScreen,
            onOpenResetPassword = navController::navigateToRequestResetPasswordScreen,
        )
        registerScreen(
            onBackPressed = navController::popBackStack,
            navigateToFavoriteScreen = navController::navigateToFavoriteScreen
        )
        favoritesScreen(
            windowSizeClass,
            onToggleFavorite,
            navController::navigateToDetailRecipeScreen,
            navController::navigateToCustomRecipeDetailScreen,
            customRecipeHasBeenUpdated
        )
        deepLinkRecipeScreen(
            onBackClick = navController::popBackStack,
            onToggleFavorite = onToggleFavorite
        )
        detailRecipeScreen(onBackClick = navController::popBackStack, onToggleFavorite)
        customRecipeDetailScreen(navController::popBackStack)
        videoFullScreen(window)
        profileScreen(navController::popBackStack)
        requestResetPasswordScreen(onBackPressed = navController::popBackStack)
        resetPasswordScreen()
        premiumScreen(navController::popBackStack)
    }
}



