package com.francotte.ads

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.francotte.core.ads.R
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

enum class BannerPlacement {
    HOME_POS_1,
    HOME_POS_2,
    SEARCH,
    RECIPE_POS_1,
    RECIPE_POS_2,
    FOOD_LIST,
}

data class BannerConfig(
    val adUnitId: String,
    val adSizes: List<AdSize>,
)

interface BannerConfigProvider {
    fun configFor(placement: BannerPlacement): BannerConfig
}

class DefaultBannerConfigProvider : BannerConfigProvider {
    override fun configFor(placement: BannerPlacement): BannerConfig =
        when (placement) {
            BannerPlacement.HOME_POS_1 ->
                BannerConfig(
                    adUnitId = "ca-app-pub-8828725570000941/7772142176",
                    adSizes = listOf(AdSize.BANNER),
                )

            BannerPlacement.HOME_POS_2 ->
                BannerConfig(
                    adUnitId = "ca-app-pub-8828725570000941/7772142176",
                    adSizes = listOf(AdSize.BANNER, AdSize.LARGE_BANNER, AdSize.FULL_BANNER),
                )

            BannerPlacement.SEARCH ->
                BannerConfig(
                    adUnitId = "ca-app-pub-8828725570000941/4514766905",
                    adSizes =
                        listOf(
                            AdSize.BANNER,
                            AdSize.LARGE_BANNER,
                            AdSize.FULL_BANNER,
                            AdSize.MEDIUM_RECTANGLE,
                            AdSize.LEADERBOARD,
                            AdSize.WIDE_SKYSCRAPER,
                        ),
                )

            BannerPlacement.RECIPE_POS_1 ->
                BannerConfig(
                    adUnitId = "ca-app-pub-8828725570000941/6814283725",
                    adSizes =
                        listOf(
                            AdSize.BANNER,
                            AdSize.LARGE_BANNER,
                            AdSize.FULL_BANNER,
                        ),
                )

            BannerPlacement.RECIPE_POS_2 ->
                BannerConfig(
                    adUnitId = "ca-app-pub-8828725570000941/6814283725",
                    adSizes =
                        listOf(
                            AdSize.BANNER,
                            AdSize.LARGE_BANNER,
                            AdSize.FULL_BANNER,
                            AdSize.MEDIUM_RECTANGLE,
                        ),
                )

            BannerPlacement.FOOD_LIST ->
                BannerConfig(
                    adUnitId = "ca-app-pub-8828725570000941/1942049235",
                    adSizes =
                        listOf(
                            AdSize.BANNER,
                            AdSize.LARGE_BANNER,
                            AdSize.MEDIUM_RECTANGLE,
                        ),
                )
        }
}

fun AdView.ensureConfig(
    adUnitId: String,
    adSize: AdSize,
) {
    if (this.adUnitId != adUnitId) {
        this.adUnitId = adUnitId
    }
    setAdSize(adSize)
}

fun Context.adaptiveBannerSize(availableWidthPx: Int): AdSize {
    val density = resources.displayMetrics.density
    val adWidthDp = (availableWidthPx / density).toInt()
    return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidthDp)
}

private fun Dp.getAdSizeFromHeight(): AdSize =
    when (this) {
        in 0.dp..55.dp -> AdSize.BANNER
        in 56.dp..80.dp -> AdSize.FULL_BANNER
        100.dp -> AdSize.LARGE_BANNER
        else -> AdSize.BANNER
    }

interface BannerAdProvider {
    @Composable
    fun Banner(
        placement: BannerPlacement,
        useAdaptiveSize: Boolean,
        horizontalPadding: Dp,
        heightFallback: Dp,
    )
}

class AdMobBannerAdProvider(
    private val bannerConfigProvider: BannerConfigProvider = DefaultBannerConfigProvider(),
) : BannerAdProvider {
    @Composable
    override fun Banner(
        placement: BannerPlacement,
        useAdaptiveSize: Boolean,
        horizontalPadding: Dp,
        heightFallback: Dp,
    ) {
        val context = LocalContext.current
        val config = remember(placement) { bannerConfigProvider.configFor(placement) }

        val density = LocalDensity.current
        val screenWidthPx =
            with(density) {
                LocalConfiguration.current.screenWidthDp.dp
                    .roundToPx()
            }
        val paddingPx = with(density) { (horizontalPadding * 2).roundToPx() }
        val availableWidthPx = (screenWidthPx - paddingPx).coerceAtLeast(0)

        val adSize =
            remember(useAdaptiveSize, heightFallback, availableWidthPx) {
                if (useAdaptiveSize) {
                    context.adaptiveBannerSize(availableWidthPx)
                } else {
                    heightFallback.getAdSizeFromHeight()
                }
            }
        var isLoading by remember(placement, adSize) { mutableStateOf(true) }
        var hasFailed by remember(placement, adSize) { mutableStateOf(false) }

        val adView =
            remember(config.adUnitId, adSize) {
                AdView(context).apply {
                    ensureConfig(adUnitId = config.adUnitId, adSize = adSize)

                    adListener = object : AdListener() {
                        override fun onAdLoaded() {
                            isLoading = false
                            hasFailed = false
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            isLoading = false
                            hasFailed = true
                        }
                    }
                }
            }

        DisposableEffect(adView) {
            adView.loadAd(AdRequest.Builder().build())
            onDispose {
                adView.destroy()
            }
        }
        Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
            if (isLoading) { PlaceHolder() }
            if (!hasFailed) {
                AndroidView(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = horizontalPadding)
                            .height(if (useAdaptiveSize) adSize.height.dp else heightFallback),
                    factory = { adView },
                )
            }
        }
    }
}

@Composable
fun PlaceHolder(height: Dp = 120.dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .background(Color.LightGray),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(R.drawable.ic_recipe_launcher),
            contentDescription = null,
            tint = Color.Gray
        )
    }
}

@Module
@InstallIn(SingletonComponent::class)
object AdsModule {
    @Provides
    @Singleton
    fun provideBannerAdProvider(): BannerAdProvider = AdMobBannerAdProvider()
}


