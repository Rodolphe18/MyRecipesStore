package com.francotte.categories

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.francotte.api.CategoriesNavKey
import com.francotte.api.navigateToCategory
import com.francotte.common.counters.ScreenCounter
import com.francotte.model.AbstractCategory
import com.francotte.navigation.Navigator


fun EntryProviderScope<NavKey>.categoriesEntry(
    navigator: Navigator
) {
    entry<CategoriesNavKey> {
        CategoriesRoute(onOpenCategory = { category -> navigator.navigateToCategory(category.strCategory)})
    }
}


@Composable
fun CategoriesRoute(
    viewModel: CategoriesViewModel = hiltViewModel(),
    onOpenCategory: (AbstractCategory) -> Unit,
) {
    val homeUiState by viewModel.categories.collectAsStateWithLifecycle()
    CategoriesScreen(
        categoryUiState = homeUiState,
        onOpenCategory = onOpenCategory,
        onReload = {
            viewModel.refresh()
        })
    LaunchedEffect(Unit) {
        ScreenCounter.increment()
    }
}
