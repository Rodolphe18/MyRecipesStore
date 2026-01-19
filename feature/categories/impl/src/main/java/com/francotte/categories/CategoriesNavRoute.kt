package com.francotte.categories

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.francotte.api.CategoriesNavKey
import com.francotte.api.navigateToCategory
import com.francotte.common.counters.ScreenCounter
import com.francotte.model.AbstractCategory
import com.francotte.navigation.Navigator



fun EntryProviderScope<NavKey>.categoriesEntry(
    navigator: Navigator,
    windowSizeClass: WindowSizeClass
) {
    entry<CategoriesNavKey> {
        CategoriesRoute(windowSizeClass = windowSizeClass, onOpenCategory = { category -> navigator.navigateToCategory(category.strCategory)})
    }
}


@Composable
fun CategoriesRoute(
    viewModel: CategoriesViewModel = hiltViewModel(),
    windowSizeClass: WindowSizeClass,
    onOpenCategory: (AbstractCategory) -> Unit,
) {
    val homeUiState by viewModel.categories.collectAsStateWithLifecycle()
    CategoriesScreen(
        categoryUiState = homeUiState,
        windowSizeClass = windowSizeClass,
        onOpenCategory = onOpenCategory,
        onReload = {
            viewModel.refresh()
        })
    LaunchedEffect(Unit) {
        ScreenCounter.increment()
    }
}
