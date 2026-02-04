package com.francotte.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.francotte.ads.BannerAd
import com.francotte.ads.BannerPlacement
import com.francotte.feature.search.api.SearchMode
import com.francotte.ui.DeviceMode
import com.francotte.ui.LocalAppLayout
import com.francotte.ui.LocalBannerProvider

@Composable
fun SearchModeSelectionScreen(onSearchModeSelected: (SearchMode) -> Unit) {
    val mode = LocalAppLayout.current.mode
    val scrollState = rememberScrollState()
    val localBannerProvider = LocalBannerProvider.current
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BannerAd(
                placement = BannerPlacement.SEARCH,
                provider = localBannerProvider,
                horizontalPadding = 16.dp,
            )
            Spacer(Modifier.height(32.dp))
            Text(
                "How would you like to \n search recipes ?",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(32.dp))
            if (mode != DeviceMode.PhoneLandscape) {
                Row {
                    SearchModeButton("By ingredients", Icons.Default.ThumbUp) {
                        onSearchModeSelected(SearchMode.INGREDIENTS)
                    }
                    Spacer(Modifier.width(60.dp))
                    SearchModeButton("By country", Icons.Default.Notifications) {
                        onSearchModeSelected(SearchMode.COUNTRY)
                    }
                }
            } else {
            SearchModeButton("By ingredients", Icons.Default.ThumbUp) {
                onSearchModeSelected(SearchMode.INGREDIENTS)
            }
            Spacer(Modifier.height(32.dp))
            SearchModeButton("By country", Icons.Default.Notifications) {
                onSearchModeSelected(SearchMode.COUNTRY)
            }
        }
    }
}

@Composable
fun SearchModeButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    val mode = LocalAppLayout.current.mode
    val dimension = remember(mode) { searchModeButtonDimension(mode) }
    Button(
        onClick = onClick,
        modifier = Modifier.width(dimension.width).aspectRatio(dimension.ratio),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA000)),
        elevation = ButtonDefaults.buttonElevation(8.dp),
    ) {
        Icon(icon, contentDescription = null, Modifier.size(24.dp))
        Spacer(Modifier.width(12.dp))
        Text(label, fontWeight = FontWeight.SemiBold, color = Color.White)
    }
}


data class SearchModeButtonDimension(
    val width: Dp,
    val ratio: Float,
)

fun searchModeButtonDimension(mode: DeviceMode):SearchModeButtonDimension= when (mode) {
    DeviceMode.PhonePortrait -> SearchModeButtonDimension(width= 280.dp, ratio = 4f)
    DeviceMode.PhoneLandscape -> SearchModeButtonDimension(width = 200.dp, ratio = 2f)
    DeviceMode.TabletPortrait -> SearchModeButtonDimension(width = 200.dp, ratio  = 2f)
    DeviceMode.TabletLandscape -> SearchModeButtonDimension(width = 250.dp, ratio  = 2f)
}
