package com.francotte.myrecipesstore.splash

import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.francotte.feature.home.api.HomeNavKey
import com.francotte.navigation.Navigator
import com.francotte.ui.LocalConsentManager
import com.francotte.ui.LocalInterstitialManager
import kotlinx.serialization.Serializable


fun Navigator.goToBaseAndClearSplash(targetTopLevelKey: NavKey) {
    state.rootStack.clear()
    navigate(targetTopLevelKey)
}


@Serializable
object SplashNavKey : NavKey

fun EntryProviderScope<NavKey>.splashEntry(navigator: Navigator, resolveNextKey: () -> NavKey) {
    entry<SplashNavKey> {
        SplashRoute({ navigator.goToBaseAndClearSplash(resolveNextKey()) })
    }
}

@Composable
fun SplashRoute(
    onDone: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel(),
) {
    val activity = LocalActivity.current
    val consentManager = LocalConsentManager.current
    val interstitialManager = LocalInterstitialManager.current

    val step by viewModel.step.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(step) {
        when (step) {
            SplashStep.Consent -> {
                Log.d("debug_consent", step.toString())
                val safeActivity = activity ?: return@LaunchedEffect
                val canRequestAds = consentManager.ensureConsent(safeActivity)
                viewModel.onConsentFinished(canRequestAds)
            }

            SplashStep.Interstitial -> {
                val safeActivity = activity ?: return@LaunchedEffect
                interstitialManager
                    .loadAndShowInterstitialAd(safeActivity)
                    .collect { viewModel.onAdEvent(it) }
            }

            SplashStep.Done -> Unit
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is SplashUiState.GoHome) onDone()
    }

    ApplyAdSystemBarsFix(uiState.isAdShowing)
    SplashScreen()
}
