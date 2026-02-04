package com.francotte.categories

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.francotte.api.CategoriesNavKey
import com.francotte.api.CategoryNavKey
import com.francotte.api.navigateToDetail
import com.francotte.common.counters.ScreenCounter
import com.francotte.model.LikeableRecipe
import com.francotte.navigation.Navigator
import kotlinx.serialization.Serializable


fun EntryProviderScope<NavKey>.categoryEntry(
    navigator: Navigator,
    onToggleFavorite: (LikeableRecipe) -> Unit,
) {
    entry<CategoryNavKey> { key ->
        CategoryRoute(
            viewModel = hiltViewModel<CategoryViewModel, CategoryViewModel.Factory>(key = key.category,
            ) { factory ->
                factory.create(key.category)
            },
            onOpenRecipe = navigator::navigateToDetail,
            onToggleFavorite = onToggleFavorite,
            onBack = navigator::goBack
        )
    }
}


@Composable
fun CategoryRoute(
    viewModel: CategoryViewModel = hiltViewModel(),
    onOpenRecipe: (List<String>, Int, String) -> Unit,
    onToggleFavorite: (LikeableRecipe) -> Unit,
    onBack: () -> Unit,
) {
    val uiState by viewModel.categoryUiState.collectAsStateWithLifecycle()
    val title = viewModel.category

    CategoryScreen(
        categoryUiState = uiState,
        title = title,
        onReload = {
            viewModel.refresh()
        },
        onOpenRecipe = onOpenRecipe,
        onToggleFavorite = onToggleFavorite,
        onBack
    )
    LaunchedEffect(Unit) {
        ScreenCounter.increment()
    }
}
