package com.francotte.ads

import android.app.Activity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Singleton
class InterstitialManager {

    private var mInterstitialAd: InterstitialAd? = null
    private var hasShownAd = false

    private val _shouldKeepSplashScreen = MutableStateFlow(true)
    val shouldKeepSplashScreen = _shouldKeepSplashScreen.asStateFlow()

    private var _hasTimedOut = MutableStateFlow(false)
    var hasTimedOut = _hasTimedOut.asStateFlow()

    init {
        CoroutineScope(Dispatchers.Main).launch {
            delay(2000)
            _shouldKeepSplashScreen.value = false
        }
    }

    fun loadAndShowAd(activity: Activity) {
        if (hasShownAd) {
            _shouldKeepSplashScreen.value = false
            return
        }
        _hasTimedOut.value = false

        CoroutineScope(Dispatchers.Main).launch {
            delay(3000)
            if (mInterstitialAd == null) {
                _hasTimedOut.value = true
                _shouldKeepSplashScreen.update { false }
            }
        }

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            activity,
            "ca-app-pub-3940256099942544/1033173712",
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    mInterstitialAd = ad
                    mInterstitialAd?.fullScreenContentCallback =
                        object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                _shouldKeepSplashScreen.update { false }
                            }
                            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                                super.onAdFailedToShowFullScreenContent(p0)
                                _shouldKeepSplashScreen.update { false }
                            }
                        }
                    hasShownAd = true
                    mInterstitialAd?.show(activity)
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    _shouldKeepSplashScreen.update { false }
                }
            })
    }

    fun retryLoadAd(activity: Activity) {
        loadAndShowAd(activity)
    }


}

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideInterstitialManager(): InterstitialManager = InterstitialManager()
}