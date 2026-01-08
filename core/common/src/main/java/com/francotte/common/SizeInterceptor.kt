package com.francotte.common

import android.net.Uri
import androidx.annotation.VisibleForTesting
import androidx.core.net.toUri
import coil.intercept.Interceptor
import coil.request.ImageRequest
import coil.request.ImageResult
import coil.size.Dimension
import coil.size.Size

object SizeInterceptor : Interceptor {

    override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
        val request = chain.request
        val data = request.data
        // Only treat Uri or String
        val uri = (data as? String)?.toUri() ?: data as? Uri ?: return chain.proceed(request)
        // Transform with size
        return chain.proceed(
            request
                .newBuilder()
                .data(uri.withSize(request.downloadSize ?: chain.size) ?: return chain.proceed(request))
                .build()
        )
    }
}

private val ACCEPTED_SCHEMES = arrayOf("http", "https")

@field:VisibleForTesting
const val SIZE_TEMPLATE = "[SIZE]"

@field:VisibleForTesting
const val AUTO_SIZE = "auto"

private val Dimension.remoteSize: String
    get() = when (this) {
        is Dimension.Pixels -> px.toString()
        is Dimension.Undefined -> AUTO_SIZE
    }

fun Uri.withSize(size: Size): Uri? {
    // Only treat http or https
    if (scheme !in ACCEPTED_SCHEMES) return null
    val pathSegments = pathSegments.toMutableList()
    val sizeSegmentIndex = pathSegments.indexOf(SIZE_TEMPLATE)
    // If the size template is not present there is nothing to do
    if (sizeSegmentIndex < 0) return null
    pathSegments[sizeSegmentIndex] = "${size.width.remoteSize}-${size.height.remoteSize}"
    // Rebuild URL with correct size
    return buildUpon()
        .path("") // Clear path
        .apply {
            pathSegments.forEach { segment ->
                appendPath(segment)
            }
        }
        .build()
}

private const val DOWNLOAD_SIZE = "downloadSize"


val ImageRequest.downloadSize: Size?
    get() = parameters.value(DOWNLOAD_SIZE)