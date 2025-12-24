package com.francotte.myrecipesstore.navigation

import android.content.Context
import android.content.Intent
import android.view.Window
import androidx.activity.compose.LocalActivity
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
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navOptions
import com.francotte.designsystem.component.TopAppBar
import com.francotte.favorites.FAVORITE_ROUTE
import com.francotte.inapp_rating.RatingBottomSheetHost
import com.francotte.login.navigateToLoginScreen
import com.francotte.myrecipesstore.ui.AppState
import com.francotte.profile.navigateToProfileScreen
import com.francotte.settings.SettingsBottomSheet
import com.francotte.settings.navigateToPremiumScreen
import com.francotte.ui.LocalAuthManager
import com.francotte.ui.LocalFavoriteManager
import com.francotte.ui.LocalInAppRatingManager
import com.francotte.video.isFullscreen
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseScaffold(
    @ApplicationContext context: Context,
    appState: AppState,
    windowSizeClass: WindowSizeClass,
    window: Window,
) {
    val localActivity = LocalActivity.current
    val localAuthManager = LocalAuthManager.current
    val localFavoriteManager = LocalFavoriteManager.current
    val localInAppRatingManager = LocalInAppRatingManager.current

    val isAuthenticated by localAuthManager.isAuthenticated.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }
    var showSettingsDialog by rememberSaveable { mutableStateOf(false) }

    val customRecipeHasBeenUpdated by localFavoriteManager
        .customRecipeHasBeenUpdatedSuccessfully
        .collectAsStateWithLifecycle()
    val currentRoute = appState.currentDestination?.route
    val backStackEntry by appState.navController.currentBackStackEntryAsState()
    val isFullscreen = backStackEntry?.destination.isFullscreen()

    LaunchedEffect(Unit) {
        localFavoriteManager.goToLoginScreenEvent.collect {
            appState.navController.navigateToLoginScreen()
        }
    }
    LaunchedEffect(Unit) {
        localActivity?.let {activity ->
            if (localInAppRatingManager.shouldTryToShowInAppReview()) {
                localInAppRatingManager.apply {
                    requestInAppReview(activity)
                    setHasBeenRatedOrNotAskAgainToTrue()
                }
            }
        }
    }
    if (showSettingsDialog) {
        SettingsBottomSheet(
            onDismiss = { showSettingsDialog = false },
            onOpenPrivacyPolicy = {
                showSettingsDialog = false
                val uri = "https://myrecipesstore18.com/privacy-policy.html".toUri()
                val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            },
            onLogout = {
                scope.launch {
                    localAuthManager.logout()
                    currentRoute?.let { route ->
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
            onPremiumClick = {
                showSettingsDialog = false
                appState.navController.navigateToPremiumScreen()
            },
            onShareApp = {
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "Try this app! it is incredible!")
                    putExtra(Intent.EXTRA_TITLE, localAuthManager.toString()) // ou ton string
                    putExtra(Intent.EXTRA_SUBJECT, localAuthManager.toString())
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, null)
                context.startActivity(shareIntent)
                showSettingsDialog = false
            },
            onDeleteClick = {
                scope.launch {
                    localAuthManager.deleteUser()
                    currentRoute?.let { route ->
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
            val destination = appState.currentTopLevelDestination
            if (!isFullscreen && destination != null) {
                val user by localAuthManager.user.collectAsStateWithLifecycle()
                var image by remember { mutableStateOf("") }
                LaunchedEffect(user?.image) { user?.image?.let { image = it } }

                TopAppBar(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(horizontal = 4.dp),
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
            if (!isFullscreen) {
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
        }
    ) { padding ->

        Box(
            Modifier
                .fillMaxSize()
                .let {
                    if (isFullscreen) it
                    else it.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                }
        ) {
            BaseNavHost(
                appState = appState,
                modifier = if (isFullscreen) Modifier else Modifier
                    .padding(padding)
                    .consumeWindowInsets(padding),
                window = window,
                windowSizeClass = windowSizeClass,
                isAuthenticated = isAuthenticated,
                customRecipeHasBeenUpdated = customRecipeHasBeenUpdated,
                onToggleFavorite = { recipe, _ ->
                    scope.launch { localFavoriteManager.toggleRecipeFavorite(recipe) }
                }
            )
            RatingBottomSheetHost(localInAppRatingManager)
            //  InAppUpdateHost(appUpdateManager = appState.appUpdateManager, snackBarHostState)
        }
    }

    LaunchedEffect(Unit) {
        localFavoriteManager.snackBarMessage.collect { snackBarHostState.showSnackbar(it) }
    }
    LaunchedEffect(Unit) {
        localAuthManager.snackBarMessage.collect { snackBarHostState.showSnackbar(it) }
    }
}
