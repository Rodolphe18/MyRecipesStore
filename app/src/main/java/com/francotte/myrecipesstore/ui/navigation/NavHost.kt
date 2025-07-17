package com.francotte.myrecipesstore.ui.navigation


import android.net.Uri
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.francotte.myrecipesstore.domain.model.LikeableRecipe
import com.francotte.myrecipesstore.network.model.Ingredient
import com.francotte.myrecipesstore.ui.compose.add_recipe.addRecipeScreen
import com.francotte.myrecipesstore.ui.compose.categories.categoriesScreen
import com.francotte.myrecipesstore.ui.compose.categories.category.categoryScreen
import com.francotte.myrecipesstore.ui.compose.categories.category.navigateToCategoryScreen
import com.francotte.myrecipesstore.ui.compose.detail.deepLinkRecipeScreen
import com.francotte.myrecipesstore.ui.compose.detail.detailRecipeScreen
import com.francotte.myrecipesstore.ui.compose.detail.navigateToDetailRecipeScreen
import com.francotte.myrecipesstore.ui.compose.favorites.favoritesScreen
import com.francotte.myrecipesstore.ui.compose.favorites.navigateToFavoriteScreen
import com.francotte.myrecipesstore.ui.compose.home.BASE_ROUTE
import com.francotte.myrecipesstore.ui.compose.home.homeScreen
import com.francotte.myrecipesstore.ui.compose.login.loginScreen
import com.francotte.myrecipesstore.ui.compose.login.navigateToLoginScreen
import com.francotte.myrecipesstore.ui.compose.profile.profileScreen
import com.francotte.myrecipesstore.ui.compose.register.navigateToRegisterScreen
import com.francotte.myrecipesstore.ui.compose.register.registerScreen
import com.francotte.myrecipesstore.ui.compose.reset.request_reset.navigateToRequestResetPasswordScreen
import com.francotte.myrecipesstore.ui.compose.reset.request_reset.requestResetPasswordScreen
import com.francotte.myrecipesstore.ui.compose.reset.reset.navigateToResetPasswordScreen
import com.francotte.myrecipesstore.ui.compose.reset.reset.resetPasswordScreen
import com.francotte.myrecipesstore.ui.compose.search.result_mode.navigateToSearchModeScreen
import com.francotte.myrecipesstore.ui.compose.search.result_mode.searchModeScreen
import com.francotte.myrecipesstore.ui.compose.search.result_recipes.navigateToSearchRecipesScreen
import com.francotte.myrecipesstore.ui.compose.search.result_recipes.searchRecipesScreen
import com.francotte.myrecipesstore.ui.compose.search.searchScreen
import com.francotte.myrecipesstore.ui.compose.section.navigateToSection
import com.francotte.myrecipesstore.ui.compose.section.sectionScreen
import com.francotte.myrecipesstore.ui.compose.user_recipes.customRecipeDetailScreen
import com.francotte.myrecipesstore.ui.compose.user_recipes.navigateToCustomRecipeDetailScreen
import com.francotte.myrecipesstore.ui.compose.video.navigateToVideoFullScreen
import com.francotte.myrecipesstore.ui.compose.video.videoFullScreen

@Composable
fun NavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    windowSizeClass: WindowSizeClass,
    startDestination: String = BASE_ROUTE,
    onToggleFavorite: (LikeableRecipe, Boolean) -> Unit,
    isAuthenticated: Boolean,
    customRecipeHasBeenUpdated: Boolean,
    onUpdate: (recipeId: String, title: String, ingredients: List<Ingredient>, instructions: String, image: Uri?) -> Unit,
    onSubmit: (title: String, ingredients: List<Ingredient>, instructions: String, image: Uri?) -> Unit,
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
        addRecipeScreen(isAuthenticated, navController::navigateToLoginScreen, onSubmit)
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
        customRecipeDetailScreen(navController::popBackStack, onUpdate)
        videoFullScreen()
        profileScreen(navController::popBackStack)
        requestResetPasswordScreen(onBackPressed = navController::popBackStack)
        resetPasswordScreen()
    }
}



