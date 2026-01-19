package com.francotte.search.result_recipe

import androidx.compose.material3.windowsizeclass.WindowSizeClass
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
import com.francotte.api.navigateToDetail
import com.francotte.feature.search.api.SearchMode
import com.francotte.feature.search.api.SearchRecipesNavKey
import com.francotte.model.LikeableRecipe
import com.francotte.navigation.Navigator
import com.francotte.search.result_mode.SearchModeViewModel
import kotlinx.serialization.Serializable


fun EntryProviderScope<NavKey>.searchRecipesEntry(navigator: Navigator,windowSizeClass: WindowSizeClass,
                                                  onToggleFavorite: (LikeableRecipe) -> Unit,) {
    entry<SearchRecipesNavKey> { key ->
        SearchRecipesRoute(viewModel =
            androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel<SearchRecipesViewModel, SearchRecipesViewModel.Factory>(
                key = key.mode.title,
            ) { factory ->
                factory.create(key.item,key.mode)
            },
            windowSizeClass = windowSizeClass,
            onOpenRecipe = navigator::navigateToDetail,
            onToggleFavorite = onToggleFavorite,
            onBack = navigator::goBack,
        )
    }
}


@Composable
fun SearchRecipesRoute(
    viewModel: SearchRecipesViewModel = hiltViewModel(),
    windowSizeClass: WindowSizeClass,
    onOpenRecipe: (List<String>, Int, String) -> Unit,
    onToggleFavorite: (LikeableRecipe) -> Unit,
    onBack: () -> Unit,
) {
    val uiState by viewModel.searchRecipesUiState.collectAsStateWithLifecycle()
    val title = viewModel.item
    SearchRecipesScreen(searchRecipesUiState = uiState, windowSizeClass = windowSizeClass, title = title, onReload = {
    }, onOpenRecipe = onOpenRecipe, onToggleFavorite = onToggleFavorite, onBack = onBack)
}
