package com.francotte.common.extension

import android.content.Context
import coil.request.CachePolicy
import coil.request.ImageRequest


fun imageRequestBuilder(context: Context, data: String?): ImageRequest {
    return ImageRequest.Builder(context)
        .data(data)
        .crossfade(true)
        .diskCachePolicy(CachePolicy.ENABLED)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .build()
}

