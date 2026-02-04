package com.francotte.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.francotte.common.counters.ScreenCounter
import com.francotte.feature.search.api.SearchMode
import com.francotte.feature.search.api.SearchNavKey
import com.francotte.feature.search.api.navigateToSearchMode
import com.francotte.navigation.Navigator


fun EntryProviderScope<NavKey>.searchEntry(navigator: Navigator) {
    entry<SearchNavKey> {
        SearchRoute(navigator::navigateToSearchMode)
    }
}


@Composable
fun SearchRoute(onSearchModeSelected: (SearchMode) -> Unit) {
    SearchModeSelectionScreen(onSearchModeSelected)
    LaunchedEffect(Unit) {
        ScreenCounter.increment()
    }
}
