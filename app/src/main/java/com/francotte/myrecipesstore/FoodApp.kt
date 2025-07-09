package com.francotte.myrecipesstore

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navOptions
import com.francotte.myrecipesstore.settings.SettingsDialog
import com.francotte.myrecipesstore.ui.compose.favorites.FAVORITE_ROUTE
import com.francotte.myrecipesstore.ui.compose.favorites.login.navigateToLoginScreen
import com.francotte.myrecipesstore.ui.compose.home.HOME_ROUTE
import com.francotte.myrecipesstore.ui.compose.home.HomeViewModel
import com.francotte.myrecipesstore.ui.navigation.BottomBar
import com.francotte.myrecipesstore.ui.navigation.NavHost
import com.francotte.myrecipesstore.ui.navigation.TopAppBar
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch


@SuppressLint("UnrememberedGetBackStackEntry")
@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun FoodApp(@ApplicationContext context: Context, appState: AppState) {
    val isAuthenticated by appState.authManager.isAuthenticated.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }
    var showSettingsDialog by rememberSaveable { mutableStateOf(false) }
    val currentBackStackEntry = appState.navController.currentBackStackEntryAsState().value
    val currentDestination = currentBackStackEntry?.destination?.route

    if (showSettingsDialog) {
        SettingsDialog(
            onDismiss = { showSettingsDialog = false },
            onLogout = {
                scope.launch {

                    appState.authManager.logout()

                    currentDestination?.let { route ->
                        if (route == FAVORITE_ROUTE) {
                            appState.navController.navigateToLoginScreen()
                        } else {
                            appState.navController.navigate(route) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                    showSettingsDialog = false
                }
            },
            onShareApp = {
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "Essaie cette app incroyable !")
                    putExtra(Intent.EXTRA_TITLE, context.resources.getString(R.string.app_name))
                    putExtra(Intent.EXTRA_SUBJECT, context.resources.getString(R.string.app_name))
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, null)
                context.startActivity(shareIntent)
                showSettingsDialog = false
            },
            onSubscribedClick = {}
        )
    }
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackBarHostState) },
        topBar = {
            val destination = appState.currentTopLevelDestination
            if (destination != null) {
                TopAppBar(
                    titleRes = destination.titleTextId,
                    actionIcon = Icons.Outlined.Settings,
                    actionIconContentDescription = "settings",
                    onActionClick = { showSettingsDialog = true },
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
                onAddRecipe = { title, ingredients, instructions, uris ->
                    scope.launch {
                        appState.favoriteManager.createRecipe(
                            title,
                            ingredients,
                            instructions,
                            uris
                        )
                    }
                },
                modifier = Modifier
                    .padding(padding)
                    .consumeWindowInsets(padding),
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

