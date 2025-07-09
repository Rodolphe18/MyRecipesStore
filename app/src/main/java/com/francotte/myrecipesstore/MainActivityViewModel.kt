package com.francotte.myrecipesstore

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.francotte.myrecipesstore.ads.InterstitialManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val interstitialManager: InterstitialManager
) : ViewModel() {


    val uiState: StateFlow<MainActivityUiState> = combine(interstitialManager.shouldKeepSplashScreen, interstitialManager.hasTimedOut){
        keep, timedOut -> if (keep) MainActivityUiState.Loading else MainActivityUiState.Success(timedOut) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MainActivityUiState.Loading)


    fun loadInterstitial(activity: Activity) {
        viewModelScope.launch {
         //   interstitialManager.loadAndShowAd(activity)
        }
    }

    fun retryLoadInterstitial(activity: Activity) {
        viewModelScope.launch {
            interstitialManager.retryLoadAd(activity)
        }
    }
}

sealed interface MainActivityUiState {
    data object Loading : MainActivityUiState
    data class Success(val hasTimedOut:Boolean) : MainActivityUiState

    fun shouldKeepSplashScreen() = this is Loading

    fun shouldShowToolTipEffect() = this is Success
}