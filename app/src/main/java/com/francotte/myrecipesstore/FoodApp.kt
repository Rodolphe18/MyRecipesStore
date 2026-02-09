package com.francotte.myrecipesstore

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.Window
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.francotte.add_recipe.addRecipeEntry
import com.francotte.api.navigateToProfile
import com.francotte.api.navigateToResetPassword
import com.francotte.categories.categoriesEntry
import com.francotte.categories.categoryEntry
import com.francotte.designsystem.component.FoodSnackbarHost
import com.francotte.designsystem.component.HideBottomSystemBar
import com.francotte.designsystem.component.TopAppBar
import com.francotte.detail.detailRecipeEntry
import com.francotte.favorites.customRecipeEntry
import com.francotte.favorites.favoritesEntry
import com.francotte.feature.home.api.HomeNavKey
import com.francotte.feature.login.api.navigateToLogin
import com.francotte.feature.settings.api.navigateToPremium
import com.francotte.feature.video.api.VideoNavKey
import com.francotte.home.homeEntry
import com.francotte.inapp_rating.InAppRatingManager
import com.francotte.login.loginEntry
import com.francotte.model.LikeableRecipe
import com.francotte.myrecipesstore.deeplink.DeepLinkBus
import com.francotte.myrecipesstore.deeplink.toNavKeyOrNull
import com.francotte.myrecipesstore.navigation.AppNavigationRail
import com.francotte.myrecipesstore.navigation.BottomBar
import com.francotte.myrecipesstore.navigation.TOP_LEVEL_NAV_ITEMS
import com.francotte.myrecipesstore.navigation.useNavigationRail
import com.francotte.myrecipesstore.splash.SplashNavKey
import com.francotte.myrecipesstore.splash.splashEntry
import com.francotte.myrecipesstore.ui.AppState
import com.francotte.navigation.Navigator
import com.francotte.navigation.toEntries
import com.francotte.profile.profileEntry
import com.francotte.register.registerEntry
import com.francotte.reset.requestResetEntry
import com.francotte.reset.resetPasswordEntry
import com.francotte.search.result_mode.searchModeEntry
import com.francotte.search.result_recipe.searchRecipesEntry
import com.francotte.search.searchEntry
import com.francotte.section.sectionEntry
import com.francotte.settings.SettingsBottomSheet
import com.francotte.settings.premiumEntry
import com.francotte.ui.LocalAppLayout
import com.francotte.ui.LocalAuthManager
import com.francotte.ui.LocalFavoriteManager
import com.francotte.ui.LocalInAppRatingManager
import com.francotte.video.videoEntry
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodApp(
    @ApplicationContext context: Context,
    appState: AppState,
    onToggleFavorite: (LikeableRecipe) -> Unit,
    window: Window,
) {
    val mode = LocalAppLayout.current.mode
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

    val useRail = mode.useNavigationRail()

    val topBarScrollBehavior =
        if (useRail) TopAppBarDefaults.enterAlwaysScrollBehavior()
        else null

    val navigator = remember { Navigator(appState.navigationState) }

    val pendingDeepLink = rememberSaveable { mutableStateOf<NavKey?>(null) }

    val entryProvider = entryProvider {
        splashEntry(navigator,window) {
            pendingDeepLink.value?.also { pendingDeepLink.value = null } ?: HomeNavKey
        }
        homeEntry(navigator, onToggleFavorite)
        categoriesEntry(navigator)
        categoryEntry(navigator, onToggleFavorite)
        addRecipeEntry(navigator, isAuthenticated)
        searchModeEntry(navigator)
        sectionEntry(navigator, onToggleFavorite)
        searchRecipesEntry(navigator, onToggleFavorite)
        searchEntry(navigator)
        loginEntry(navigator)
        registerEntry(navigator)
        favoritesEntry(navigator, onToggleFavorite, customRecipeHasBeenUpdated)
        premiumEntry(navigator)
        //  deepLinkRecipeScreen(navController::popBackStack, onToggleFavorite)
        detailRecipeEntry(navigator, onToggleFavorite)
        customRecipeEntry(navigator)
        videoEntry(window)
        profileEntry(navigator)
        resetPasswordEntry(navigator)
        requestResetEntry(navigator)
    }

    val entries = appState.navigationState.toEntries(entryProvider)


    LaunchedEffect(Unit) {
        localFavoriteManager.goToLoginScreenEvent.collect {
            navigator.navigateToLogin()
        }
    }
    LaunchedEffect(Unit) {
        showInAppReview(localActivity, localInAppRatingManager)
    }

    LaunchedEffect(Unit) {
        DeepLinkBus.intents.collect { intent ->
            val uri = intent.data ?: return@collect
            val key = uri.toNavKeyOrNull() ?: return@collect
            pendingDeepLink.value = key
        }
    }


    if (showSettingsDialog) {
        SettingsBottomSheet(
            onDismiss = { showSettingsDialog = false },
            onOpenPrivacyPolicy = {
                showSettingsDialog = false
                openPrivacyPolicy(context)
            },
            onLogout = {
                scope.launch {
                    localAuthManager.logout()
                    navigator.navigateToLogin()
                    showSettingsDialog = false
                }
            },
            onPremiumClick = {
                showSettingsDialog = false
                navigator.navigateToPremium()
            },
            onShareApp = {
                shareApp(context)
                showSettingsDialog = false
            },
            onDeleteClick = {
                scope.launch {
                    localAuthManager.deleteUser()
                    navigator.navigateToLogin()
                    showSettingsDialog = false
                }
            },
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { FoodSnackbarHost(snackBarHostState) },
        bottomBar = {
            val showChrome =
                appState.navigationState.currentKey != SplashNavKey &&
                    appState.navigationState.currentKey !is VideoNavKey

            if (showChrome && !useRail) {
                BottomBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp),
                    navigationState = appState.navigationState,
                    onNavigateToDestination = { navKey -> navigator.navigate(navKey) },
                    isAuthenticated = isAuthenticated,
                )
            }
        },
    ) { padding ->
        val showChrome = appState.navigationState.currentKey != SplashNavKey &&
                appState.navigationState.currentKey !is VideoNavKey
        Row(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
        ) {

            if (showChrome && useRail) {
                HideBottomSystemBar(window = window)
                AppNavigationRail(
                    modifier = Modifier
                        .fillMaxHeight(),
                    navigationState = appState.navigationState,
                    onNavigateToDestination = { navKey -> navigator.navigate(navKey) },
                    isAuthenticated = isAuthenticated,
                )
            }
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .then(
                        if (topBarScrollBehavior != null) Modifier.nestedScroll(topBarScrollBehavior.nestedScrollConnection)
                        else Modifier
                    )
            ) {
                var shouldShowTopAppBar = false

                if (appState.navigationState.currentKey in appState.navigationState.topLevelKeys) {
                    shouldShowTopAppBar = true

                    val destination =
                        TOP_LEVEL_NAV_ITEMS[appState.navigationState.currentTopLevelKey]
                            ?: error("Top level nav item not found for ${appState.navigationState.currentTopLevelKey}")

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
                        onNavigationClick = navigator::navigateToProfile,
                        scrollBehavior = topBarScrollBehavior
                    )
                }

                Box(
                    modifier = Modifier.consumeWindowInsets(
                        if (shouldShowTopAppBar) {
                            WindowInsets.safeDrawing.only(WindowInsetsSides.Top)
                        } else {
                            WindowInsets(0, 0, 0, 0)
                        },
                    )
                ) {
                    val alreadyNavigated = remember { mutableStateOf(false) }
                    LaunchedEffect(appState.resetPasswordToken) {
                        val token = appState.resetPasswordToken
                        if (token != null && !alreadyNavigated.value) {
                            alreadyNavigated.value = true
                            navigator.navigateToResetPassword(token)
                        }
                    }

                    NavDisplay(
                        entries = entries,
                        onBack = navigator::goBack,
                    )
                }

                LaunchedEffect(Unit) {
                    localFavoriteManager.snackBarMessage.collect { snackBarHostState.showSnackbar(it) }
                }
                LaunchedEffect(Unit) {
                    localAuthManager.snackBarMessage.collect { snackBarHostState.showSnackbar(it) }
                }
            }
        }
    }
}

private fun shareApp(context: Context) {
    val sendIntent =
        Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "Try this app! it is incredible!")
            putExtra(Intent.EXTRA_TITLE, "My recipes Store")
            putExtra(Intent.EXTRA_SUBJECT, "Food recipes")
            type = "text/plain"
        }
    val shareIntent = Intent.createChooser(sendIntent, null)
    context.startActivity(shareIntent)
}

private suspend fun showInAppReview(
    activity: Activity?,
    inAppRatingManager: InAppRatingManager,
) {
    activity?.let { activity ->
        if (inAppRatingManager.shouldTryToShowInAppReview()) {
            inAppRatingManager.apply {
                requestInAppReview(activity)
                setHasBeenRatedOrNotAskAgainToTrue()
            }
        }
    }
}

private fun openPrivacyPolicy(context: Context) {
    val uri = "https://myrecipesstore18.com/privacy-policy.html".toUri()
    val intent =
        Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    context.startActivity(intent)
}


