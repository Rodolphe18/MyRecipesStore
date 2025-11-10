package com.francotte.categories

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import com.francotte.common.ScreenCounter
import com.francotte.model.LikeableRecipe
import kotlinx.serialization.Serializable


@Serializable
data class CategoryNavigationRoute(val category:String)

fun NavController.navigateToCategoryScreen(category: String, navOptions: NavOptionsBuilder.() -> Unit={}) {
    navigate(route = CategoryNavigationRoute(category)) {
        navOptions()
    }
}

fun NavGraphBuilder.categoryScreen(windowSizeClass:WindowSizeClass,onBackClick: () -> Unit, onOpenRecipe: (List<String>,Int,String) -> Unit, onToggleFavorite:(LikeableRecipe, Boolean) -> Unit) {
    composable<CategoryNavigationRoute> {
        CategoryRoute(windowSizeClass = windowSizeClass,onOpenRecipe =  { ids, index, title -> onOpenRecipe(ids,index,title) }, onToggleFavorite = onToggleFavorite, onBack= onBackClick)
    }
}

@Composable
fun CategoryRoute(viewModel: CategoryViewModel = hiltViewModel(),windowSizeClass:WindowSizeClass, onOpenRecipe: (List<String>, Int,String) -> Unit, onToggleFavorite:(LikeableRecipe, Boolean)->Unit, onBack:()->Unit) {
    val uiState by viewModel.categoryUiState.collectAsStateWithLifecycle()
    val title = viewModel.category

    CategoryScreen(categoryUiState = uiState,windowSizeClass = windowSizeClass, title = title, onReload =  { viewModel.reload() }, onOpenRecipe = onOpenRecipe, onToggleFavorite = onToggleFavorite, onBack)
    LaunchedEffect(Unit) {
        ScreenCounter.increment()
    }
}