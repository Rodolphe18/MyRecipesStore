package com.francotte.ads

import android.app.Activity
import com.francotte.billing.PremiumStatusProvider
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InterstitialManager @Inject constructor(
    private val premiumStatus: PremiumStatusProvider
) {

    private var mInterstitialAd: InterstitialAd? = null
    private var hasShownAd = false

    private val _shouldKeepSplashScreen = MutableStateFlow(true)
    val shouldKeepSplashScreen = _shouldKeepSplashScreen.asStateFlow()

    private val _isAdShowing = MutableStateFlow(false)
    val isAdShowing = _isAdShowing.asStateFlow()

    private val _hasTimedOut = MutableStateFlow(false)
    val hasTimedOut = _hasTimedOut.asStateFlow()

    init {
        // Si premium -> on libère direct (pas de pub, pas de splash d'attente)
        CoroutineScope(Dispatchers.Main.immediate).launch {
            premiumStatus.isPremium.collect { premium ->
                if (premium) _shouldKeepSplashScreen.value = false
            }
        }

        // Fallback: même non premium, on ne bloque pas plus de 2s
        CoroutineScope(Dispatchers.Main.immediate).launch {
            delay(2000)
            _shouldKeepSplashScreen.value = false
        }
    }

    fun loadAndShowAd(activity: Activity) {
        // ✅ Premium => aucune pub
        if (premiumStatus.isPremium.value) {
            _shouldKeepSplashScreen.value = false
            return
        }

        if (hasShownAd) {
            _shouldKeepSplashScreen.value = false
            return
        }

        _hasTimedOut.value = false

        CoroutineScope(Dispatchers.Main.immediate).launch {
            delay(3000)
            if (mInterstitialAd == null) {
                _hasTimedOut.value = true
                _shouldKeepSplashScreen.value = false
            }
        }

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            activity,
            "ca-app-pub-3940256099942544/1033173712",
            adRequest,
            object : InterstitialAdLoadCallback() {

                override fun onAdLoaded(ad: InterstitialAd) {
                    // Re-check au moment du load (au cas où premium vient d’être acheté)
                    if (premiumStatus.isPremium.value) {
                        _shouldKeepSplashScreen.value = false
                        return
                    }

                    mInterstitialAd = ad
                    mInterstitialAd?.fullScreenContentCallback =
                        object : FullScreenContentCallback() {

                            override fun onAdShowedFullScreenContent() {
                                _isAdShowing.value = true
                            }

                            override fun onAdDismissedFullScreenContent() {
                                _isAdShowing.value = false
                                _shouldKeepSplashScreen.value = false
                                mInterstitialAd = null
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                _isAdShowing.value = false
                                _shouldKeepSplashScreen.value = false
                                mInterstitialAd = null
                            }
                        }

                    hasShownAd = true
                    mInterstitialAd?.show(activity)
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    _shouldKeepSplashScreen.value = false
                }
            }
        )
    }

    fun retryLoadAd(activity: Activity) {
        loadAndShowAd(activity)
    }
}
