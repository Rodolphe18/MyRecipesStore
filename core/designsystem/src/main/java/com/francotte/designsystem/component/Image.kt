package com.francotte.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import coil.compose.SubcomposeAsyncImage
import coil.imageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Precision
import com.francotte.designsystem.R


@Composable
fun DesignAsyncImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    width: Dp,
    height: Dp,
    contentScale: ContentScale = ContentScale.Crop,
    alignment: Alignment = Alignment.Center) {

    val context = LocalContext.current
    val density = LocalDensity.current
    val placeholder: Painter = painterResource(R.drawable.ic_image)

    val widthPx = remember(width, density) {
        with(density) { width.roundToPx() }
    }

    val heightPx = remember(height, density) {
        with(density) { height.roundToPx() }
    }

    val request = remember(model, widthPx, heightPx) {
        ImageRequest.Builder(context)
            .data(model)
            .size(widthPx, heightPx)
            .precision(Precision.INEXACT)
            .crossfade(false)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .build()
    }

    SubcomposeAsyncImage(
        model = request,
        contentDescription = contentDescription,
        imageLoader = context.imageLoader,
        modifier = modifier
            .size(width, height)
            .background(Color.LightGray.copy(alpha = 0.7f)),
        contentScale = contentScale,
        alignment = alignment,
        loading = { PlaceholderContent(placeholder, contentDescription) },
        error = { PlaceholderContent(placeholder, contentDescription) }
    )
}

@Composable
private fun PlaceholderContent(placeholder: Painter, contentDescription: String?) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = placeholder,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(0.5f)
        )
    }
}
