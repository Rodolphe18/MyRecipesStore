package com.francotte.myrecipesstore

import android.view.Window
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
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
import com.francotte.feature.login.api.navigateToLoginOnLogout
import com.francotte.feature.settings.api.navigateToPremium
import com.francotte.feature.video.api.VideoNavKey
import com.francotte.home.homeEntry
import com.francotte.login.loginEntry
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
import com.francotte.ui.LocalShouldShowBanners
import com.francotte.ui.LocalSnackbarHostState
import com.francotte.video.videoEntry
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodApp(appState: AppState, window: Window) {

    val mode = LocalAppLayout.current.mode
    val mainViewModel: MainViewModel = hiltViewModel()

    val isAuthenticated by mainViewModel.isAuthenticated.collectAsStateWithLifecycle()
    val shouldShowBanners by mainViewModel.shouldShowBanners.collectAsStateWithLifecycle()
    val userImage by mainViewModel.userImage.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snackBarHostState = LocalSnackbarHostState.current
    var showSettingsDialog by rememberSaveable { mutableStateOf(false) }

    val useRail = mode.useNavigationRail()

    val topBarScrollBehavior = if (useRail) TopAppBarDefaults.pinnedScrollBehavior() else null

    val navigator = remember { Navigator(appState.navigationState) }

    val pendingDeepLink = rememberSaveable { mutableStateOf<NavKey?>(null) }

    val entryProvider = entryProvider {
        splashEntry(navigator,window) {
            pendingDeepLink.value?.also { pendingDeepLink.value = null } ?: HomeNavKey
        }
        homeEntry(navigator, mainViewModel::toggleFavorite)
        categoriesEntry(navigator)
        categoryEntry(navigator, mainViewModel::toggleFavorite)
        addRecipeEntry(navigator)
        searchModeEntry(navigator)
        sectionEntry(navigator, mainViewModel::toggleFavorite)
        searchRecipesEntry(navigator, mainViewModel::toggleFavorite)
        searchEntry(navigator, mainViewModel::toggleFavorite)
        loginEntry(navigator)
        registerEntry(navigator)
        favoritesEntry(navigator, mainViewModel::toggleFavorite)
        premiumEntry(navigator)
        detailRecipeEntry(navigator, mainViewModel::toggleFavorite)
        customRecipeEntry(navigator)
        videoEntry(window)
        profileEntry(navigator)
        resetPasswordEntry(navigator)
        requestResetEntry(navigator)
    }

    val entries = appState.navigationState.toEntries(entryProvider)


    LaunchedEffect(Unit) {
        DeepLinkBus.intents.collect { intent ->
            val uri = intent.data ?: return@collect
            val key = uri.toNavKeyOrNull() ?: return@collect
            if (appState.navigationState.currentKey == SplashNavKey) {
                pendingDeepLink.value = key
            } else {
                navigator.navigate(key)
            }
        }
    }


    if (showSettingsDialog) {
        SettingsBottomSheet(
            onDismiss = { showSettingsDialog = false },
            onLogout = {
                scope.launch {
                    mainViewModel.logout()
                    navigator.navigateToLoginOnLogout()
                    showSettingsDialog = false
                }
            },
            onPremiumClick = {
                showSettingsDialog = false
                navigator.navigateToPremium()
            },
            onDeleteClick = {
                scope.launch {
                    mainViewModel.deleteAccount()
                    navigator.navigateToLoginOnLogout()
                    showSettingsDialog = false
                }
            },
        )
    }

    CompositionLocalProvider(LocalShouldShowBanners provides shouldShowBanners) {
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

                    TopAppBar(
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(horizontal = 4.dp),
                        profileImage = userImage,
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
                    mainViewModel.effects.collect { effect ->
                        when (effect) {
                            is MainEffect.ShowSnackBar -> snackBarHostState.showSnackbar(effect.message)
                            MainEffect.NavigateToLogin -> navigator.navigateToLogin()
                        }
                    }
                }
            }
        }
    }
    }
}


