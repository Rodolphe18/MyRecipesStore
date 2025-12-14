package com.francotte.designsystem.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.francotte.billing.BillingManager
import com.francotte.billing.PremiumStatusProvider
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView


@Composable
fun AdMobBanner(
    height: Dp,
    modifier: Modifier = Modifier
) {
    val localBilling = LocalBillingController.current
    val isPremium by localBilling.isPremium.collectAsStateWithLifecycle()
    if (isPremium) return
    BaseAdMobBanner(modifier = modifier, height = height)
}


@Composable
private fun BaseAdMobBanner(
    modifier: Modifier = Modifier,
    height: Dp,
    adUnitId: String = "ca-app-pub-3940256099942544/6300978111"
) {
    val context = LocalContext.current

    val adView = remember(adUnitId, height) {
        AdView(context).apply {
            setAdSize(height.getAdSizeFromHeight())
            setAdUnitId(adUnitId)
        }
    }
    DisposableEffect(Unit) {
        adView.loadAd(AdRequest.Builder().build())
        onDispose { adView.destroy() }
    }
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        factory = { adView }
    )
}

private fun Dp.getAdSizeFromHeight(): AdSize {
    return when (this) {
        in 0.dp..55.dp -> AdSize.BANNER
        in 56.dp..80.dp -> AdSize.FULL_BANNER
        100.dp -> AdSize.LARGE_BANNER
        else -> AdSize.BANNER
    }
}


@Composable
fun <T> ListWithBanners(
    items: List<T>,
    bannerInterval: Int = 5,
    itemContent: @Composable (index: Int, item: T) -> Unit
) {
    val totalItemCount = items.size + (items.size / bannerInterval)
    for (index in 0 until totalItemCount) {
        val actualItemIndex = index - (index / (bannerInterval + 1))
        if ((index + 1) % (bannerInterval + 1) == 0) {
            AdMobBanner(
                height = 50.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
        } else if (actualItemIndex in items.indices) {
            itemContent(index, items[actualItemIndex])
        }
    }
}

@Composable
fun <T> LazyListWithBanners(
    items: List<T>,
    bannerInterval: Int = 5,
    itemContent: @Composable (item: T) -> Unit
) {
    LazyColumn {
        val totalCount = items.size + items.size / bannerInterval
        items(totalCount) { index ->
            // Calculer la position dans la liste réelle
            val actualItemIndex = index - index / (bannerInterval + 1)
            if ((index + 1) % (bannerInterval + 1) == 0) {
                AdMobBanner(
                    height = 50.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            } else if (actualItemIndex < items.size) {
                itemContent(items[actualItemIndex])
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LazyGridWithBanners(
    modifier: Modifier = Modifier,
    totalItemCount: Int,
    columns: Int = 2,
    bannerInterval: Int = 4,
    state: LazyGridState,
    horizontalArrangement: Arrangement.HorizontalOrVertical,
    verticalArrangement: Arrangement.HorizontalOrVertical,
    flingBehavior: FlingBehavior,
    contentPadding: PaddingValues,
    bannerContent: @Composable () -> Unit,
    itemKey: (index: Int) -> Any,                 // <-- NEW
    itemContentType: (index: Int) -> Any = { "item" }, // <-- NEW
    bannerKey: (bannerIndex: Int) -> Any = { i -> "banner-$i" }, // <-- NEW
    bannerContentType: Any = "banner",            // <-- NEW
    itemContent: @Composable LazyGridItemScope.(index: Int) -> Unit,
) {
    val bannerStep = bannerInterval + 1
    val bannerCount = totalItemCount / bannerInterval
    val totalCountWithBanners = totalItemCount + bannerCount

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier.fillMaxSize(),
        state = state,
        contentPadding = contentPadding,
        flingBehavior = flingBehavior,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement
    ) {
        items(
            count = totalCountWithBanners,
            key = { index ->
                if ((index + 1) % bannerStep == 0) {
                    val b = (index / bannerStep) // 0,1,2...
                    bannerKey(b)
                } else {
                    val realIndex = index - (index / bannerStep)
                    itemKey(realIndex)
                }
            },
            contentType = { index ->
                if ((index + 1) % bannerStep == 0) bannerContentType
                else {
                    val realIndex = index - (index / bannerStep)
                    itemContentType(realIndex)
                }
            },
            span = { index ->
                if ((index + 1) % bannerStep == 0) GridItemSpan(columns) else GridItemSpan(1)
            }
        ) { index ->
            if ((index + 1) % bannerStep == 0) {
                bannerContent()
            } else {
                val realIndex = index - (index / bannerStep)
                if (realIndex < totalItemCount) itemContent(realIndex)
            }
        }
    }
}
