package com.francotte.myrecipesstore

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.rememberNavController
import com.francotte.ads.BannerAdProvider
import com.francotte.ads.InterstitialManager
import com.francotte.billing.BillingController
import com.francotte.cmp.ConsentManager
import com.francotte.common.counters.LaunchCounter
import com.francotte.designsystem.theme.FoodTheme
import com.francotte.domain.AuthManager
import com.francotte.domain.FavoriteManager
import com.francotte.inapp_rating.InAppRatingManager
import com.francotte.login.navigateToLoginScreen
import com.francotte.myrecipesstore.navigation.RootNavHost
import com.francotte.myrecipesstore.permissions.NotificationPermissionEffect
import com.francotte.ui.LocalAuthManager
import com.francotte.ui.LocalBillingController
import com.francotte.ui.LocalConsentManager
import com.francotte.ui.LocalFavoriteManager
import com.francotte.ui.LocalInAppRatingManager
import com.francotte.ui.LocalInterstitialManager
import com.francotte.ui.LocalLaunchCounterManager
import com.francotte.myrecipesstore.ui.rememberAppState
import com.francotte.ui.LocalBannerProvider
import com.google.android.play.core.appupdate.AppUpdateManager
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
    lateinit var launchCounterManager: LaunchCounter

    @Inject
    lateinit var billingController: BillingController

    @Inject
    lateinit var consentManager: ConsentManager

    @Inject
    lateinit var appUpdateManager: AppUpdateManager

    @Inject
    lateinit var inAppRatingManager: InAppRatingManager

    @Inject
    lateinit var interstitialManager: InterstitialManager

    @Inject
    lateinit var bannerAdProvider: BannerAdProvider


    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = android.graphics.Color.TRANSPARENT,
                darkScrim = android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.auto(
                lightScrim = lightScrim,
                darkScrim = darkScrim,
            ),
        )
        setContent {
            CompositionLocalProvider(
                LocalBillingController provides billingController,
                LocalConsentManager provides consentManager,
                LocalInterstitialManager provides interstitialManager,
                LocalAuthManager provides authManager,
                LocalFavoriteManager provides favoriteManager,
                LocalLaunchCounterManager provides launchCounterManager,
                LocalInAppRatingManager provides inAppRatingManager,
                LocalBannerProvider provides bannerAdProvider
            ) {
                val data: Uri? = intent?.data
                val rootNavController = rememberNavController()
                val appNavController = rememberNavController()
                val appState = rememberAppState(
                    navController = appNavController,
                    resetPasswordToken = data?.getQueryParameter("token")
                )

                FoodTheme {
                    RootNavHost(
                        context = this,
                        rootNavController = rootNavController,
                        appState = appState,
                        windowSizeClass = calculateWindowSizeClass(this),
                        window = window
                    )
                    NotificationPermissionEffect()
                }
            }
        }
    }

}


private val lightScrim = android.graphics.Color.argb(0xe6, 0xFF, 0xFF, 0xFF)
private val darkScrim = android.graphics.Color.argb(0x80, 0x1b, 0x1b, 0x1b)


