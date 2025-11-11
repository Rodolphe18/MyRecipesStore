package com.francotte.myrecipesstore.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navOptions
import com.francotte.designsystem.component.TopAppBar
import com.francotte.favorites.FAVORITE_ROUTE
import com.francotte.login.navigateToLoginScreen
import com.francotte.myrecipesstore.R
import com.francotte.myrecipesstore.navigation.BottomBar
import com.francotte.myrecipesstore.navigation.NavHost
import com.francotte.profile.navigateToProfileScreen
import com.francotte.settings.SettingsDialog
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch


@SuppressLint("UnrememberedGetBackStackEntry")
@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun FoodApp(
    @ApplicationContext context: Context,
    appState: AppState,
    windowSizeClass: WindowSizeClass
) {
    val isAuthenticated by appState.authManager.isAuthenticated.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }
    var showSettingsDialog by rememberSaveable { mutableStateOf(false) }
    val currentBackStackEntry = appState.navController.currentBackStackEntryAsState().value
    val currentDestination = currentBackStackEntry?.destination?.route
    val customRecipeHasBeenUpdated by appState.favoriteManager.customRecipeHasBeenUpdatedSuccessfully.collectAsStateWithLifecycle()
    if (showSettingsDialog) {
        SettingsDialog(
            onDismiss = { showSettingsDialog = false },
            onLogout = {
                scope.launch {

                    appState.authManager.logout()

                    currentDestination?.let { route ->
                        if (route == FAVORITE_ROUTE) {
                            appState.navController.navigateToLoginScreen(navOptions {
                                popUpTo(0) { inclusive = true }
                            })
                        } else {
                            appState.navController.navigate(route) {
                                popUpTo(FAVORITE_ROUTE) { inclusive = true }
                            }
                        }
                    }
                    showSettingsDialog = false
                }
            },
            onShareApp = {
                val sendIntent = Intent().apply {
                    setAction(Intent.ACTION_SEND)
                    putExtra(Intent.EXTRA_TEXT, "Try this app! it is incredible!")
                    putExtra(Intent.EXTRA_TITLE, context.resources.getString(R.string.app_name))
                    putExtra(Intent.EXTRA_SUBJECT, context.resources.getString(R.string.app_name))
                    setType("text/plain")
                }
                val shareIntent = Intent.createChooser(sendIntent, null)
                context.startActivity(shareIntent)
                showSettingsDialog = false
            },
            onDeleteAccount = {
                scope.launch {
                    appState.authManager.deleteUser()
                    currentDestination?.let { route ->
                        if (route == FAVORITE_ROUTE) {
                            appState.navController.navigateToLoginScreen(navOptions {
                                popUpTo(0) { inclusive = true }
                            })
                        } else {
                            appState.navController.navigate(route) {
                                popUpTo(FAVORITE_ROUTE) { inclusive = true }
                            }
                        }
                    }
                    showSettingsDialog = false
                }
            }
        )
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackBarHostState) },
        topBar = {
            val user by appState.authManager.user.collectAsStateWithLifecycle()
            var image by remember { mutableStateOf("") }
            LaunchedEffect(user?.image) {
                user?.image?.let {
                    image = it
                }
            }
            val destination = appState.currentTopLevelDestination
            if (destination != null) {
                TopAppBar(
                    modifier = Modifier.statusBarsPadding().padding(horizontal = 4.dp),
                    profileImage = image,
                    titleRes = destination.titleTextId,
                    actionIcon = Icons.Outlined.Settings,
                    actionIconContentDescription = "settings",
                    onActionClick = { showSettingsDialog = true },
                    navigationIconEnabled = isAuthenticated,
                    navigationIcon = Icons.Filled.AccountCircle,
                    onNavigationClick = { appState.navController.navigateToProfileScreen() }
                )
            }
        },
        bottomBar = {
            BottomBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                destinations = appState.topLevelDestinations,
                onNavigateToDestination = appState::navigateToTopLevelDestination,
                currentDestination = appState.currentDestination,
                isAuthenticated = isAuthenticated
            )
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Horizontal
                    )
                )
        ) {
            NavHost(
                onToggleFavorite = { recipe, _ ->
                    scope.launch {
                        appState.favoriteManager.toggleRecipeFavorite(
                            recipe
                        )
                    }
                },
                navController = appState.navController,
                modifier = Modifier
                    .padding(padding)
                    .consumeWindowInsets(padding),
                windowSizeClass = windowSizeClass,
                isAuthenticated = isAuthenticated,
                onSubmit = { recipeTitle, ingredients, instructions, url ->
                    scope.launch {
                        appState.favoriteManager.createRecipe(
                            recipeTitle,
                            ingredients,
                            instructions,
                            url
                        )
                    }
                },
                onUpdate = { recipeId, recipeTitle, ingredients, instructions, url ->
                    scope.launch {
                        try {
                            appState.favoriteManager.updateRecipe(
                                recipeId,
                                recipeTitle,
                                ingredients,
                                instructions,
                                url
                            )
                        } catch (e: Exception) {
                            Log.d("debug_update2", e.message.toString())
                        }
                    }
                },
                customRecipeHasBeenUpdated = customRecipeHasBeenUpdated,
                resetPasswordToken = appState.resetPasswordToken
            )
            LaunchedEffect(Unit) {
                appState.favoriteManager.snackBarMessage.collect { message ->
                    snackBarHostState.showSnackbar(message)
                }
            }
            LaunchedEffect(Unit) {
                appState.authManager.snackBarMessage.collect { message ->
                    snackBarHostState.showSnackbar(message)
                }
            }
        }
    }
}

