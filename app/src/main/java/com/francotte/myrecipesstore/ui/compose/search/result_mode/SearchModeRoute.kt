package com.francotte.myrecipesstore.ui.compose.search.result_mode

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import com.francotte.myrecipesstore.ui.compose.search.SearchMode
import kotlinx.serialization.Serializable

@Serializable
data class SearchModeNavRoute(val searchMode: SearchMode)

fun NavController.navigateToSearchModeScreen(searchMode: SearchMode, navOptions: NavOptionsBuilder.() -> Unit={}) {
    navigate(route = SearchModeNavRoute(searchMode)) {
        navOptions()
    }
}

fun NavGraphBuilder.searchModeScreen(onItemSelected:(String,SearchMode)->Unit, onBack: () -> Unit, recipesResultDestination: NavGraphBuilder.() -> Unit) {
    composable<SearchModeNavRoute> {
        SearchModeRoute(onItemSelected=  { item, mode -> onItemSelected(item,mode) }, onBack = onBack)
    }
    recipesResultDestination()
}

@Composable
fun SearchModeRoute(viewModel: SearchModeViewModel = hiltViewModel(), onItemSelected:(String,SearchMode)->Unit, onBack:()->Unit) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    ItemSelectionGrid(searchMode = viewModel.searchMode, items = items, onItemSelected= onItemSelected, onBack = onBack)
}