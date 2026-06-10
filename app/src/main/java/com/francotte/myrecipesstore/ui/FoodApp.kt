package com.francotte.myrecipesstore.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import androidx.window.core.layout.WindowSizeClass
import com.francotte.add_recipe.addRecipeEntry
import com.francotte.api.DetailRecipeNavKey
import com.francotte.api.navigateToProfile
import com.francotte.api.navigateToResetPassword
import com.francotte.categories.categoriesEntry
import com.francotte.categories.categoryEntry
import com.francotte.designsystem.component.FoodSnackbarHost
import com.francotte.designsystem.component.HideBottomSystemBar
import com.francotte.designsystem.component.TopAppBar
import com.francotte.designsystem.theme.Orange
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
import com.francotte.myrecipesstore.MainEffect
import com.francotte.myrecipesstore.MainViewModel
import com.francotte.myrecipesstore.deeplink.DeepLinkBus
import com.francotte.myrecipesstore.deeplink.toNavKeyOrNull
import com.francotte.myrecipesstore.navigation.FAVORITES
import com.francotte.myrecipesstore.navigation.LOGIN
import com.francotte.myrecipesstore.navigation.TOP_LEVEL_NAV_ITEMS
import com.francotte.myrecipesstore.splash.SplashNavKey
import com.francotte.myrecipesstore.splash.splashEntry
import com.francotte.navigation.Navigator
import com.francotte.navigation.toNavEntries
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
import com.francotte.ui.LocalShouldShowBanners
import com.francotte.ui.LocalSnackbarHostState
import com.francotte.video.videoEntry
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun FoodApp(appState: AppState) {

    val mainViewModel: MainViewModel = hiltViewModel()

    val isAuthenticated by mainViewModel.isAuthenticated.collectAsStateWithLifecycle()
    val shouldShowBanners by mainViewModel.shouldShowBanners.collectAsStateWithLifecycle()
    val userImage by mainViewModel.userImage.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snackBarHostState = LocalSnackbarHostState.current
    var showSettingsDialog by rememberSaveable { mutableStateOf(false) }

    val navigator = remember { Navigator(appState.navigationState) }

    val pendingDeepLink = rememberSaveable { mutableStateOf<NavKey?>(null) }

    // Détail : on masque la nav suite (bottom bar / rail) en portrait comme en paysage.
    // Comme HideBottomSystemBar() n'est appelé qu'en mode rail, la barre système Android
    // reste affichée sur le détail → l'user garde le back système.
    val showNavigationSuite = appState.navigationState.currentKey != SplashNavKey &&
        appState.navigationState.currentKey !is VideoNavKey &&
        appState.navigationState.currentKey !is DetailRecipeNavKey

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

    // On force le rail dès que la largeur est >= MEDIUM
    val isWidthAtLeastMedium = windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

    val windowAdaptiveInfo =
        if (!showNavigationSuite) {
            NavigationSuiteType.None
        } else if (isWidthAtLeastMedium) {
            NavigationSuiteType.NavigationRail
        } else {
            NavigationSuiteType.NavigationBar
        }

    val useNavigationRail = windowAdaptiveInfo == NavigationSuiteType.NavigationRail

    // La TopAppBar se cache au scroll vers le bas et réapparaît dès qu'on scrolle vers le haut.
    val topBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    // Item sélectionné (bottom bar / nav rail) : icône + texte en orange.
    val navItemColors = NavigationSuiteDefaults.itemColors(
        navigationBarItemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = Orange,
            selectedTextColor = Orange,
        ),
        navigationRailItemColors = NavigationRailItemDefaults.colors(
            selectedIconColor = Orange,
            selectedTextColor = Orange,
        ),
    )

    val entryProvider = entryProvider {
        splashEntry(navigator) {
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
        videoEntry()
        profileEntry(navigator)
        resetPasswordEntry(navigator)
        requestResetEntry(navigator)
    }

    val entries = appState.navigationState.toNavEntries(entryProvider)

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
        NavigationSuiteScaffold(
            layoutType = windowAdaptiveInfo,
            navigationSuiteItems = {
                if (showNavigationSuite) {
                    val currentNavItems =
                        if (isAuthenticated) {
                            TOP_LEVEL_NAV_ITEMS.filterNot { it.value == LOGIN }
                        } else {
                            TOP_LEVEL_NAV_ITEMS.filterNot { it.value == FAVORITES }
                        }
                    currentNavItems.forEach { (navKey, navItem) ->
                        item(
                            selected = navKey == appState.navigationState.currentTopLevelKey,
                            onClick = { navigator.navigate(navKey) },
                            icon = {
                                Icon(
                                    imageVector = navItem.selectedIcon,
                                    contentDescription = null,
                                )
                            },
                            label = { Text(stringResource(navItem.titleTextId)) },
                            colors = navItemColors,
                        )
                    }
                }
            },
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                snackbarHost = { FoodSnackbarHost(snackBarHostState) },
            ) { padding ->
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .consumeWindowInsets(padding)
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                        .nestedScroll(topBarScrollBehavior.nestedScrollConnection),
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
                            scrollBehavior = topBarScrollBehavior,
                        )
                    }

                    Box(
                        modifier = Modifier.consumeWindowInsets(
                            if (shouldShowTopAppBar) {
                                WindowInsets.safeDrawing.only(WindowInsetsSides.Top)
                            } else {
                                WindowInsets(0, 0, 0, 0)
                            },
                        ),
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
