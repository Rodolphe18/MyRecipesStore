package com.francotte.feature.search.api

import androidx.navigation3.runtime.NavKey
import com.francotte.navigation.Navigator
import kotlinx.serialization.Serializable



@Serializable
data class SearchModeNavKey(val searchMode: SearchMode) : NavKey

fun Navigator.navigateToSearchMode(searchMode: SearchMode) {
    navigate(SearchModeNavKey(searchMode))
}
