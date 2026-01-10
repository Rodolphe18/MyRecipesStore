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
import com.francotte.common.counters.ScreenCounter
import com.francotte.model.AbstractCategory

const val CATEGORIES_ROUTE = "categories"

fun NavController.navigateToCategoriesScreen(navOptions: NavOptions? = null) {
    this.navigate(CATEGORIES_ROUTE, navOptions)
}

fun NavGraphBuilder.categoriesScreen(windowSizeClass: WindowSizeClass,onOpenCategory: (String) -> Unit) {
    composable(route = CATEGORIES_ROUTE,deepLinks = listOf(navDeepLink { uriPattern = "myapp://categories" })) {
        CategoriesRoute(windowSizeClass = windowSizeClass) {
            onOpenCategory(it.strCategory)
        }
    }
}

@Composable
fun CategoriesRoute(viewModel: CategoriesViewModel = hiltViewModel(), windowSizeClass: WindowSizeClass, onOpenCategory: (AbstractCategory) -> Unit) {
    val homeUiState by viewModel.categories.collectAsStateWithLifecycle()
    CategoriesScreen(categoryUiState = homeUiState, windowSizeClass = windowSizeClass, onOpenCategory = onOpenCategory, onReload =  { viewModel.refresh() })
    LaunchedEffect(Unit) {
        ScreenCounter.increment()
    }
}