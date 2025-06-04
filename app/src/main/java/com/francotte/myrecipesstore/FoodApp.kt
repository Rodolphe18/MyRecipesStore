package com.francotte.myrecipesstore

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import com.francotte.myrecipesstore.model.AbstractRecipe
import com.francotte.myrecipesstore.settings.SettingsDialog
import com.francotte.myrecipesstore.ui.navigation.AppState
import com.francotte.myrecipesstore.ui.navigation.BottomBar
import com.francotte.myrecipesstore.ui.navigation.NavHost
import com.francotte.myrecipesstore.ui.navigation.TopAppBar


@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun FoodApp(
    onSettingsClick: () -> Unit,
    onToggleFavorite:(AbstractRecipe)->Unit,
    onSettingsDismissed: () -> Unit,
    showSettingsDialog: Boolean,
    appState: AppState
) {

    val snackbarHostState = remember { SnackbarHostState() }

    if (showSettingsDialog) {
        SettingsDialog(
            onDismiss = { onSettingsDismissed() },
        )
    }
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {

            val destination = appState.currentTopLevelDestination
            if (destination != null) {
                TopAppBar(
                    modifier = Modifier.zIndex(-1F),
                    titleRes = destination.titleTextId,
                    actionIcon = Icons.Filled.Settings,
                    actionIconContentDescription = "settings",
                    onActionClick = onSettingsClick,
                )
            }
        },
        bottomBar = {
            BottomBar(
                    destinations = appState.topLevelDestinations,
                    onNavigateToDestination = appState::navigateToTopLevelDestination,
                    currentDestination = appState.currentDestination
                )
        }
    ) { padding ->
        Row(
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Horizontal
                    )
                )
        ) {

            NavHost(
                onToggleFavorite = onToggleFavorite,
                navController = appState.navController,
                modifier = Modifier
                    .padding(padding)
                    .consumeWindowInsets(padding)
            )
        }
    }

}
