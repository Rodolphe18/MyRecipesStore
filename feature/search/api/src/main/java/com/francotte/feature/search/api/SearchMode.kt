package com.francotte.feature.search.api

import kotlinx.serialization.Serializable

@Serializable
enum class SearchMode(
    val title: String,
) {
    INGREDIENTS("Ingredients"), COUNTRY("Countries"), CATEGORIES("Categories")
}
