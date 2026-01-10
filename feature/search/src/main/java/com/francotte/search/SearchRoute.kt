package com.francotte.search


import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import com.francotte.common.counters.ScreenCounter


const val SEARCH_ROUTE = "search"

fun NavController.navigateToSearchScreen(navOptions: NavOptions? = null) {
    this.navigate(SEARCH_ROUTE, navOptions)
}

fun NavGraphBuilder.searchScreen(onSearchModeSelected: (SearchMode) -> Unit) {
    composable(route = SEARCH_ROUTE, deepLinks = listOf(navDeepLink { uriPattern = "myapp://search" })) {
        SearchRoute {
            onSearchModeSelected(it)
        }
    }
}

@Composable
fun SearchRoute(onSearchModeSelected: (SearchMode) -> Unit) {
    SearchModeSelectionScreen(onSearchModeSelected)
    LaunchedEffect(Unit) {
        ScreenCounter.increment()
    }
}