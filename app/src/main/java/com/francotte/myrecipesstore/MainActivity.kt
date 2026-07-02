package com.francotte.myrecipesstore

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.core.net.toUri
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.lifecycle.lifecycleScope
import androidx.metrics.performance.JankStats
import com.francotte.ads.BannerAdProvider
import com.francotte.designsystem.theme.FoodTheme
import com.francotte.myrecipesstore.deeplink.DeepLinkBus
import com.francotte.myrecipesstore.permissions.NotificationPermissionEffect
import com.francotte.myrecipesstore.ui.FoodApp

import com.francotte.myrecipesstore.ui.rememberAppState
import com.francotte.ui.LocalBannerProvider
import com.francotte.ui.LocalSnackbarHostState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var lazyStats: dagger.Lazy<JankStats>

    @Inject
    lateinit var bannerAdProvider: BannerAdProvider

    private val mainViewModel: MainViewModel by viewModels()


    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        emitDeepLinkIfAny(intent)
        showInAppReviewIfNeeded()

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

        // Bottom bar (NavigationSuiteScaffold) edge-to-edge : on empêche le système d'ajouter
        // un scrim translucide sur la barre de navigation, pour que les couleurs de la bottom bar
        // s'étendent jusqu'en bas de l'écran.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            CompositionLocalProvider(
                LocalBannerProvider provides bannerAdProvider,
                LocalSnackbarHostState provides snackbarHostState,
            ) {
                val data: Uri? = intent?.data
                val appState = rememberAppState(resetPasswordToken = data?.getQueryParameter("token"))
                FoodTheme {
                    FoodApp(appState = appState)
                    NotificationPermissionEffect(this)
                }
            }
        }
    }

    private fun showInAppReviewIfNeeded() {
        lifecycleScope.launch {
            mainViewModel.showInAppReviewIfNeeded(this@MainActivity)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        emitDeepLinkIfAny(intent)
    }

    /**
     * Résout le deep link d'un intent puis l'émet dans le [DeepLinkBus].
     *
     * Deux origines possibles :
     * - tap sur un lien `myapp://...` → l'URI est dans [Intent.getData] (action `ACTION_VIEW`) ;
     * - tap sur une push FCM en arrière-plan → le système ouvre le launcher et place
     *   les custom data dans les extras ; on reconstruit alors l'URI depuis `idMeal`.
     */
    private fun emitDeepLinkIfAny(intent: Intent?) {
        val uri = intent?.resolveDeepLinkUri() ?: return
        DeepLinkBus.intents.tryEmit(Intent(Intent.ACTION_VIEW, uri))
    }

    private fun Intent.resolveDeepLinkUri(): Uri? = when {
        action == Intent.ACTION_VIEW && data != null -> data
        !getStringExtra("idMeal").isNullOrBlank() ->
            "myapp://recipe/${getStringExtra("idMeal")}".toUri()
        else -> null
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
