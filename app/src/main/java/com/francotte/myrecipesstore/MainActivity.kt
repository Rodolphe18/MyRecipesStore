package com.francotte.myrecipesstore

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.CompositionLocalProvider
import androidx.metrics.performance.JankStats
import com.francotte.ads.BannerAdProvider
import com.francotte.ads.InterstitialManager
import com.francotte.billing.BillingController
import com.francotte.cmp.ConsentManager
import com.francotte.common.counters.LaunchCounter
import com.francotte.designsystem.theme.FoodTheme
import com.francotte.domain.AuthManager
import com.francotte.domain.FavoriteManager
import com.francotte.inapp_rating.InAppRatingManager
import com.francotte.myrecipesstore.deeplink.DeepLinkBus
import com.francotte.myrecipesstore.permissions.NotificationPermissionEffect
import com.francotte.myrecipesstore.ui.rememberAppState
import com.francotte.ui.LocalAuthManager
import com.francotte.ui.LocalBannerProvider
import com.francotte.ui.LocalBillingController
import com.francotte.ui.LocalConsentManager
import com.francotte.ui.LocalFavoriteManager
import com.francotte.ui.LocalInAppRatingManager
import com.francotte.ui.LocalInterstitialManager
import com.francotte.ui.LocalLaunchCounterManager
import com.francotte.ui.ProvideDeviceMode
import com.google.android.play.core.appupdate.AppUpdateManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var lazyStats: dagger.Lazy<JankStats>

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
        if (intent?.action == Intent.ACTION_VIEW) {
            DeepLinkBus.intents.tryEmit(intent)
        }
        enableEdgeToEdge(
            statusBarStyle =
                SystemBarStyle.auto(
                    lightScrim = android.graphics.Color.TRANSPARENT,
                    darkScrim = android.graphics.Color.TRANSPARENT,
                ),
            navigationBarStyle =
                SystemBarStyle.auto(
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
                LocalBannerProvider provides bannerAdProvider,
            ) {
                ProvideDeviceMode {
                    val data: Uri? = intent?.data
                    val appState = rememberAppState(resetPasswordToken = data?.getQueryParameter("token"))
                    FoodTheme {
                        FoodApp(
                            context = this,
                            appState = appState,
                            window = window,
                            onToggleFavorite = favoriteManager::toggleRecipeFavorite
                        )
                        NotificationPermissionEffect()
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        DeepLinkBus.intents.tryEmit(intent)
    }


    override fun onResume() {
        super.onResume()
        lazyStats.get().isTrackingEnabled = true
    }

    override fun onPause() {
        super.onPause()
        lazyStats.get().isTrackingEnabled = false
    }
}

private val lightScrim = android.graphics.Color.argb(0xe6, 0xFF, 0xFF, 0xFF)
private val darkScrim = android.graphics.Color.argb(0x80, 0x1b, 0x1b, 0x1b)
