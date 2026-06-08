package com.francotte.myrecipesstore.splash

import android.util.Log
import android.view.Window
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.francotte.api.DetailRecipeNavKey
import com.francotte.designsystem.component.HideNavigationBar
import com.francotte.feature.home.api.HomeNavKey
import com.francotte.navigation.Navigator
import kotlinx.serialization.Serializable


fun Navigator.goToBaseAndClearSplash(key: NavKey) {
    state.rootStack.clear()
    when (key) {
        is DetailRecipeNavKey -> {
            navigate(HomeNavKey)
            navigate(key)
        }
        else -> {
            navigate(key)
        }
    }
}


@Serializable
object SplashNavKey : NavKey

fun EntryProviderScope<NavKey>.splashEntry(navigator: Navigator,window: Window, resolveNextKey: () -> NavKey) {
    entry<SplashNavKey> {
        SplashRoute(window,{ navigator.goToBaseAndClearSplash(resolveNextKey()) })
    }
}

@Composable
fun SplashRoute(
    window: Window,
    onDone: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel(),
) {
    val activity = LocalActivity.current

    val step by viewModel.step.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HideNavigationBar(window)
    LaunchedEffect(step) {
        when (step) {
            SplashStep.Consent -> {
                Log.d("debug_consent", step.toString())
                val safeActivity = activity ?: return@LaunchedEffect
                viewModel.startConsent(safeActivity)
            }

            SplashStep.Interstitial -> {
                val safeActivity = activity ?: return@LaunchedEffect
                viewModel.startInterstitial(safeActivity)
            }

            SplashStep.Done -> Unit
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is SplashUiState.GoHome) onDone()
    }

    SplashScreen()
}
