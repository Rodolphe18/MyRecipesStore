package com.francotte.feature.search.api

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
enum class SearchMode(
    val title: String,
) {
    INGREDIENTS("Ingredients"), COUNTRY("Countries"), CATEGORIES("Categories")
}
