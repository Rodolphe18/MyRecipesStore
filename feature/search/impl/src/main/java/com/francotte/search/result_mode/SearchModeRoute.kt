package com.francotte.search.result_mode

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.francotte.feature.search.api.SearchMode
import com.francotte.feature.search.api.SearchModeNavKey
import com.francotte.feature.search.api.navigateToSearchRecipes
import com.francotte.navigation.Navigator
import kotlinx.serialization.Serializable



fun EntryProviderScope<NavKey>.searchModeEntry(navigator: Navigator) {
    entry<SearchModeNavKey> {key ->
        SearchModeRoute(viewModel =
            androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel<SearchModeViewModel, SearchModeViewModel.Factory>(
                key = key.searchMode.title,
            ) { factory ->
                factory.create(key.searchMode)
            },onItemSelected = navigator::navigateToSearchRecipes, onBack = navigator::goBack)
    }
}


@Composable
fun SearchModeRoute(
    viewModel: SearchModeViewModel = hiltViewModel(),
    onItemSelected: (String, SearchMode) -> Unit,
    onBack: () -> Unit,
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    ItemSelectionGrid(searchMode = viewModel.searchMode, items = items, onItemSelected = onItemSelected, onBack = onBack)
}
