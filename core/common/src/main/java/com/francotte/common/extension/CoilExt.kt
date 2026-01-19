package com.francotte.common.extension

import android.content.Context
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Precision

fun imageRequestBuilder(
    context: Context,
    data: String?,
    widthPx: Int,
    heightPx: Int
): ImageRequest =
    ImageRequest
        .Builder(context)
        .data(data)
        .size(widthPx,heightPx)
        .precision(Precision.INEXACT)
        .crossfade(false)
        .diskCachePolicy(CachePolicy.ENABLED)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .build()

fun imageRequestBuilder(
    context: Context,
    data: String?
): ImageRequest =
    ImageRequest
        .Builder(context)
        .data(data)
        .precision(Precision.INEXACT)
        .crossfade(false)
        .diskCachePolicy(CachePolicy.ENABLED)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .build()
