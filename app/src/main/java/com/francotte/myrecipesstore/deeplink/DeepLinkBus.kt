package com.francotte.myrecipesstore.deeplink

import android.content.Intent
import android.net.Uri
import androidx.navigation3.runtime.NavKey
import com.francotte.api.CategoriesNavKey
import com.francotte.api.FavoritesNavKey
import com.francotte.feature.search.api.SearchNavKey
import kotlinx.coroutines.flow.MutableSharedFlow

object DeepLinkBus {
    val intents = MutableSharedFlow<Intent>(
        extraBufferCapacity = 1
    )
}

fun Uri.toNavKeyOrNull(): NavKey? = when {
    scheme == "myapp" && host == "favorites" -> FavoritesNavKey
    scheme == "myapp" && host == "categories" -> CategoriesNavKey
    scheme == "myapp" && host == "search" -> SearchNavKey
    else -> null
}
