package com.francotte.myrecipesstore.ui.compose.composables

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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