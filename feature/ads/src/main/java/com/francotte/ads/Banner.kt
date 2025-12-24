package com.francotte.ads

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.francotte.ui.LocalBillingController


@Composable
fun BannerAd(
    horizontalPadding: Dp= 0.dp,
    placement: BannerPlacement,
    provider: BannerAdProvider,
) {
    val localBilling = LocalBillingController.current
    val isPremium by localBilling.isPremium.collectAsStateWithLifecycle()
    if (isPremium) return

    provider.Banner(
            placement = placement,
            useAdaptiveSize = true,
            horizontalPadding = horizontalPadding,
            heightFallback = 100.dp
    )
}


@Composable
fun <T> ListWithBanners(
    items: List<T>,
    horizontalPadding:Dp,
    bannerInterval: Int = 5,
    placement: BannerPlacement,
    provider: BannerAdProvider,
    itemContent: @Composable (index: Int, item: T) -> Unit
) {
    val totalItemCount = items.size + (items.size / bannerInterval)
    for (index in 0 until totalItemCount) {
        val actualItemIndex = index - (index / (bannerInterval + 1))
        if ((index + 1) % (bannerInterval + 1) == 0) {
            provider.Banner(
                placement = placement,
                useAdaptiveSize = true,
                horizontalPadding = horizontalPadding,
                heightFallback = 50.dp
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
    placement: BannerPlacement,
    provider: BannerAdProvider,
    itemContent: @Composable (item: T) -> Unit
) {
    LazyColumn {
        val totalCount = items.size + items.size / bannerInterval
        items(totalCount) { index ->
            // Calculer la position dans la liste réelle
            val actualItemIndex = index - index / (bannerInterval + 1)
            if ((index + 1) % (bannerInterval + 1) == 0) {
                provider.Banner(
                    placement = placement,
                    useAdaptiveSize = true,
                    horizontalPadding = 16.dp,
                    heightFallback = 50.dp
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
    itemKey: (index: Int) -> Any,
    itemContentType: (index: Int) -> Any = { "item" },
    bannerKey: (bannerIndex: Int) -> Any = { i -> "banner-$i" },
    bannerContentType: Any = "banner",
    bannerContent: @Composable () -> Unit,
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
                Box(modifier = Modifier.layout { measurable, constraints ->
                    val placeable = measurable.measure(
                        constraints.copy(
                            maxWidth = constraints.maxWidth + 32.dp.roundToPx(),
                        ),
                    )
                    layout(placeable.width, placeable.height) {
                        placeable.place(0, 0)
                    }
                }) {
                    bannerContent()
                }
            } else {
                val realIndex = index - (index / bannerStep)
                if (realIndex < totalItemCount) itemContent(realIndex)
            }
        }
    }
}
