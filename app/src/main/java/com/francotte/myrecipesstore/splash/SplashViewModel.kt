package com.francotte.myrecipesstore.splash

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.francotte.ads.InterstitialAdState
import com.francotte.ads.InterstitialManager
import com.francotte.cmp.ConsentManager
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics,
    private val interstitialManager: InterstitialManager,
    private val consentManager: ConsentManager,
) : ViewModel() {
    private val _step = MutableStateFlow<SplashStep>(SplashStep.Consent)
    val step: StateFlow<SplashStep> = _step.asStateFlow()

    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Loading)
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    private var didRetry = false

    fun startConsent(activity: Activity) {
        viewModelScope.launch {
            val canRequestAds = consentManager.ensureConsent(activity)
            onConsentFinished(canRequestAds)
        }
    }

    fun startInterstitial(activity: Activity) {
        viewModelScope.launch {
            interstitialManager.loadAndShowInterstitialAd(activity).collect { onAdEvent(it) }
        }
    }

    fun onConsentFinished(canRequestAds: Boolean) {
        _step.value = if (canRequestAds) SplashStep.Interstitial else SplashStep.Done
        if (!canRequestAds) _uiState.value = SplashUiState.GoHome
    }

    fun onAdEvent(state: InterstitialAdState) {
        when (state) {
            InterstitialAdState.Loading -> _uiState.value = SplashUiState.Loading
            InterstitialAdState.Showing -> _uiState.value = SplashUiState.ShowingAd

            InterstitialAdState.Clicked -> {
                firebaseAnalytics.logEvent("interstitial_clicked", null)
            }

            InterstitialAdState.Shown -> {
                firebaseAnalytics.logEvent("interstitial_shown", null)
            }

            InterstitialAdState.Timeout -> {
                firebaseAnalytics.logEvent("interstitial_timeout", null)
                // retry une seule fois (optionnel)
                if (!didRetry) {
                    didRetry = true
                    _step.value = SplashStep.Interstitial
                } else {
                    _step.value = SplashStep.Done
                    _uiState.value = SplashUiState.GoHome
                }
            }

            is InterstitialAdState.FailedToLoad -> {
                firebaseAnalytics.logEvent("interstitial_failed_to_load", null)
                _step.value = SplashStep.Done
                _uiState.value = SplashUiState.GoHome
            }

            InterstitialAdState.Done -> {
                _step.value = SplashStep.Done
                _uiState.value = SplashUiState.GoHome
            }

            InterstitialAdState.Idle -> Unit
        }
    }
}

sealed interface SplashStep {
    data object Consent : SplashStep

    data object Interstitial : SplashStep

    data object Done : SplashStep
}

sealed interface SplashUiState {
    data object Loading : SplashUiState

    data object ShowingAd : SplashUiState

    data object GoHome : SplashUiState

    val isAdShowing: Boolean get() = this is ShowingAd
}
