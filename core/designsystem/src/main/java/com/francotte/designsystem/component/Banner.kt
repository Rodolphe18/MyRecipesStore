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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
fun AdMobBanner(
    modifier: Modifier = Modifier,
    height: Dp,
    adUnitId: String = "ca-app-pub-3940256099942544/6300978111" // ID de test bannière
) {
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        factory = { context ->
            AdView(context).apply {
                setAdSize(height.getAdSizeFromHeight())
                setAdUnitId(adUnitId)
                loadAd(AdRequest.Builder().build())
            }
        }
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
            itemContent(index,items[actualItemIndex])
        }
    }
}

@Composable
fun <T> LazyListWithBanners(items: List<T>, bannerInterval: Int = 5,itemContent: @Composable (item: T) -> Unit) {
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
fun LazyGridWithBannersIndexed(
    totalItemCount: Int,
    columns: Int = 2,
    bannerInterval: Int = 4,
    state: LazyGridState,
    horizontalArrangement: Arrangement.HorizontalOrVertical,
    verticalArrangement:Arrangement.HorizontalOrVertical,
    flingBehavior: FlingBehavior,
    contentPadding: PaddingValues,
    bannerContent: @Composable () -> Unit,
    itemContent: @Composable LazyGridItemScope.(index: Int) -> Unit,
) {
    val totalCountWithBanners = totalItemCount + (totalItemCount / bannerInterval)

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = Modifier.fillMaxSize(),
        state = state,
        contentPadding = contentPadding,
        flingBehavior = flingBehavior,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement
    ) {
        items(
            count = totalCountWithBanners,
            span = { index ->
                if ((index + 1) % (bannerInterval + 1) == 0) {
                    GridItemSpan(columns)
                } else {
                    GridItemSpan(1)
                }
            }
        ) { index ->
            if ((index + 1) % (bannerInterval + 1) == 0) {
                bannerContent()
            } else {
                val realIndex = index - (index / (bannerInterval + 1))
                if (realIndex < totalItemCount) {
                    itemContent(realIndex)
                }
            }
        }
    }
}
