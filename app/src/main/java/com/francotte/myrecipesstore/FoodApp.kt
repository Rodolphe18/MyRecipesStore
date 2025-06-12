package com.francotte.myrecipesstore

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.francotte.myrecipesstore.domain.model.LikeableRecipe
import com.francotte.myrecipesstore.settings.SettingsDialog
import com.francotte.myrecipesstore.ui.compose.add_recipe.AddRecipeScreen
import com.francotte.myrecipesstore.ui.compose.user_recipes.UserRecipesScreen
import com.francotte.myrecipesstore.ui.navigation.BottomBar
import com.francotte.myrecipesstore.ui.navigation.NavHost
import com.francotte.myrecipesstore.ui.navigation.TopAppBar
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun FoodApp(
    @ApplicationContext context: Context,
    onSettingsClick: () -> Unit,
    onToggleFavorite: (LikeableRecipe, Boolean) -> Unit,
    onSettingsDismissed: () -> Unit,
    onNavigationClick: () -> Unit,
    showSettingsDialog: Boolean,
    showAddRecipe: Boolean,
    appState: AppState
) {
    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }
    val userRecipes by appState.favoriteManager.getUserRecipes().collectAsStateWithLifecycle()

    if (showSettingsDialog) {
        SettingsDialog(
            onDismiss = { onSettingsDismissed() },
            onLogout = { scope.launch { appState.authManager.logout() } },
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
            }
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
                    actionIcon = Icons.Filled.Settings,
                    navigationIcon = Icons.Filled.Add,
                    navigationIconEnabled = true,
                    actionIconContentDescription = "settings",
                    onActionClick = onSettingsClick,
                    onNavigationClick = onNavigationClick
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
        if (showAddRecipe) {
//            AddRecipeScreen(
//                modifier = Modifier.padding(
//                    top = padding.calculateTopPadding() + 12.dp,
//                    bottom = padding.calculateBottomPadding()
//                ), onSubmit = { title, ingredients, instructions, images ->
//                    scope.launch {
//                        appState.favoriteManager.createRecipe(
//                            title,
//                            ingredients,
//                            instructions,
//                            images
//                        )
//                    }
//                })
          UserRecipesScreen(modifier = Modifier.padding(top = padding.calculateTopPadding()),userRecipes)
        } else {
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
                LaunchedEffect(Unit) {
                    appState.favoriteManager.snackbarMessage.collect { message ->
                        snackBarHostState.showSnackbar(message)
                    }
                }
            }
        }
    }

}
