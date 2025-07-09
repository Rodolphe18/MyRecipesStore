package com.francotte.myrecipesstore.ui.compose.search


import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.francotte.myrecipesstore.util.ScreenCounter


const val SEARCH_ROUTE = "search_route"

fun NavController.navigateToSearchScreen(navOptions: NavOptions? = null) {
    this.navigate(SEARCH_ROUTE, navOptions)
}

fun NavGraphBuilder.searchScreen(onSearchModeSelected: (SearchMode) -> Unit, modeDestination: NavGraphBuilder.() -> Unit) {
    composable(route = SEARCH_ROUTE) {
        SearchRoute {
            onSearchModeSelected(it)
        }
    }
    modeDestination()
}

@Composable
fun SearchRoute(onSearchModeSelected: (SearchMode) -> Unit) {
    SearchModeSelectionScreen(onSearchModeSelected)
    LaunchedEffect(Unit) {
        ScreenCounter.increment()
    }
}