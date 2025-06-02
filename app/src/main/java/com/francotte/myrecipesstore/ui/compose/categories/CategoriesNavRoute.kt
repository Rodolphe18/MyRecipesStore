package com.francotte.myrecipesstore.ui.compose.categories

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.francotte.myrecipesstore.model.Category
import kotlinx.serialization.Serializable

@Serializable
object CategoriesNavigationRoute

fun NavController.navigateToCategoriesScreen(navOptions: NavOptions? = null) {
    this.navigate(CategoriesNavigationRoute, navOptions)
}

fun NavGraphBuilder.categoriesScreen(onOpenCategory: (String) -> Unit, categoryDestination: NavGraphBuilder.() -> Unit) {
    composable<CategoriesNavigationRoute> {
        CategoriesRoute {
            onOpenCategory(it.strCategory)
        }
    }
    categoryDestination()
}

@Composable
fun CategoriesRoute(viewModel: CategoriesViewModel = hiltViewModel(), onOpenCategory: (Category) -> Unit) {
    val homeUiState by viewModel.categories.collectAsStateWithLifecycle()
    CategoriesScreen(categoryUiState = homeUiState, onOpenCategory = onOpenCategory, onReload =  { viewModel.reload() })
}