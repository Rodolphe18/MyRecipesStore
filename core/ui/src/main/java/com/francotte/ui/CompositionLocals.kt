package com.francotte.ui

import androidx.compose.runtime.staticCompositionLocalOf
import com.francotte.ads.BannerAdProvider
import com.francotte.ads.InterstitialManager
import com.francotte.billing.BillingController
import com.francotte.cmp.ConsentManager
import com.francotte.common.counters.LaunchCounter
import com.francotte.domain.AuthManager
import com.francotte.domain.FavoriteManager
import com.francotte.inapp_rating.InAppRatingManager

val LocalConsentManager = staticCompositionLocalOf<ConsentManager> {
    error("ConsentManager not provided")
}

val LocalInterstitialManager = staticCompositionLocalOf<InterstitialManager> {
    error("InterstitialManager not provided")
}

val LocalBillingController = staticCompositionLocalOf<BillingController> {
    error("PremiumController not provided")
}

val LocalAuthManager = staticCompositionLocalOf<AuthManager> {
    error("AuthManager not provided")
}
val LocalFavoriteManager = staticCompositionLocalOf<FavoriteManager> {
    error("FavoriteManager not provided")
}
val LocalLaunchCounterManager = staticCompositionLocalOf<LaunchCounter> {
    error("LaunchCounterManager not provided")
}
val LocalInAppRatingManager = staticCompositionLocalOf<InAppRatingManager> {
    error("InAppRatingManager not provided")
}

val LocalBannerProvider = staticCompositionLocalOf<BannerAdProvider> {
    error("BannerAdProvider not provided")
}