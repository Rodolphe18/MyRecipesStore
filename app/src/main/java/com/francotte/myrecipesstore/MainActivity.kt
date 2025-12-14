package com.francotte.myrecipesstore

import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.francotte.billing.BillingController
import com.francotte.billing.PremiumStatusProvider
import com.francotte.common.LaunchCounterManager
import com.francotte.designsystem.component.LocalBillingController
import com.francotte.designsystem.component.LocalPremiumStatusProvider
import com.francotte.designsystem.theme.FoodTheme
import com.francotte.domain.AuthManager
import com.francotte.domain.FavoriteManager
import com.francotte.myrecipesstore.permissions.NotificationPermissionEffect
import com.francotte.myrecipesstore.ui.FoodApp
import com.francotte.myrecipesstore.ui.rememberAppState
import com.francotte.ui.SplashScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authManager: AuthManager

    @Inject
    lateinit var favoriteManager: FavoriteManager

    @Inject
    lateinit var launchCounterManager: LaunchCounterManager

    @Inject lateinit var billingController: BillingController

    private val viewModel: MainActivityViewModel by viewModels()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val splashScreen = installSplashScreen()
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    enableEdgeToEdge(
                        statusBarStyle = SystemBarStyle.auto(
                            lightScrim = android.graphics.Color.TRANSPARENT,
                            darkScrim = android.graphics.Color.TRANSPARENT,
                        ),
                        navigationBarStyle = SystemBarStyle.auto(
                            lightScrim = lightScrim,
                            darkScrim = darkScrim,
                        ),
                    )
                }
            }
        }
        splashScreen.setKeepOnScreenCondition { viewModel.uiState.value.shouldKeepSplashScreen() }
        viewModel.uiState.value.shouldKeepSplashScreen()
        if (viewModel.uiState.value is MainActivityUiState.Success && (viewModel.uiState.value as MainActivityUiState.Success).hasTimedOut) {
            viewModel.retryLoadInterstitial(this)
        }
        setContent {
            CompositionLocalProvider(LocalBillingController provides billingController) {
                viewModel.loadInterstitial(this)
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val isAdShowing by viewModel.isAdShowing.collectAsStateWithLifecycle()
                ApplyAdSystemBarsFix(isAdShowing)
                val data: Uri? = intent?.data
                val state = rememberAppState(
                    favoriteManager = favoriteManager,
                    authManager = authManager,
                    launchCounterManager = launchCounterManager,
                    resetPasswordToken = data?.getQueryParameter("token")
                )

                FoodTheme {
                    FoodApp(
                        context = this,
                        appState = state,
                        windowSizeClass = calculateWindowSizeClass(activity = this),
                        window = this.window
                    )
                    NotificationPermissionEffect()
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S && uiState.shouldKeepSplashScreen()) {
                        SplashScreen()
                    }

                }
            }
        }
    }
}


private val lightScrim = android.graphics.Color.argb(0xe6, 0xFF, 0xFF, 0xFF)
private val darkScrim = android.graphics.Color.argb(0x80, 0x1b, 0x1b, 0x1b)


@Composable
fun ApplyAdSystemBarsFix(isAdShowing: Boolean) {
    val activity = LocalActivity.current
    val window = activity?.window ?: return
    val view = LocalView.current

    // Sauvegarde pour restaurer
    val oldStatusColor = remember { window.statusBarColor }
    val oldNavColor = remember { window.navigationBarColor }

    DisposableEffect(isAdShowing) {
        val controller = WindowCompat.getInsetsController(window, view)

        if (isAdShowing) {
            // 1) ne plus dessiner derrière la status bar
            WindowCompat.setDecorFitsSystemWindows(window, true)

            // 2) barre du haut noire opaque
            window.statusBarColor = android.graphics.Color.BLACK
            controller.isAppearanceLightStatusBars = false
        } else {
            // restauration
            WindowCompat.setDecorFitsSystemWindows(window, false)
            window.statusBarColor = oldStatusColor
            window.navigationBarColor = oldNavColor
        }

        onDispose { }
    }
}