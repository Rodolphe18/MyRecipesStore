package com.francotte.myrecipesstore.ui.compose.categories

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
import com.francotte.myrecipesstore.domain.model.AbstractCategory
import com.francotte.myrecipesstore.network.model.NetworkCategory
import com.francotte.myrecipesstore.util.ScreenCounter
import kotlinx.serialization.Serializable

const val CATEGORIES_ROUTE = "categories_route"

fun NavController.navigateToCategoriesScreen(navOptions: NavOptions? = null) {
    this.navigate(CATEGORIES_ROUTE, navOptions)
}

fun NavGraphBuilder.categoriesScreen(windowSizeClass: WindowSizeClass,onOpenCategory: (String) -> Unit, categoryDestination: NavGraphBuilder.() -> Unit) {
    composable(route = CATEGORIES_ROUTE) {
        CategoriesRoute(windowSizeClass = windowSizeClass) {
            onOpenCategory(it.strCategory)
        }
    }
    categoryDestination()
}

@Composable
fun CategoriesRoute(viewModel: CategoriesViewModel = hiltViewModel(), windowSizeClass: WindowSizeClass, onOpenCategory: (AbstractCategory) -> Unit) {
    val homeUiState by viewModel.categories.collectAsStateWithLifecycle()
    CategoriesScreen(categoryUiState = homeUiState, windowSizeClass = windowSizeClass, onOpenCategory = onOpenCategory, onReload =  { viewModel.reload() })
    LaunchedEffect(Unit) {
        ScreenCounter.increment()
    }
}