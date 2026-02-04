package com.francotte.detail

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.francotte.api.DetailRecipeNavKey
import com.francotte.common.counters.ScreenCounter
import com.francotte.model.LikeableRecipe
import com.francotte.navigation.Navigator
import kotlinx.serialization.Serializable
import kotlin.String
import kotlin.collections.List
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Serializable
data class DetailRecipeRoute(
    val ids: List<String>?,
    val index: Int?,
    val title: String?,
)


fun EntryProviderScope<NavKey>.detailRecipeEntry(
    navigator: Navigator,
    onToggleFavorite: (LikeableRecipe) -> Unit
) {
    entry<DetailRecipeNavKey> { key ->
        DetailRecipeRoute(
            viewModel = hiltViewModel<DetailRecipeViewModel, DetailRecipeViewModel.Factory>(
                key = key.title,
            ) { factory ->
                factory.create(key.ids,key.index,key.title)
            },
            onToggleFavorite = onToggleFavorite,
            onBackCLick = navigator::goBack,
        )
    }
}


fun NavGraphBuilder.deepLinkRecipeScreen(
    onBackClick: () -> Unit,
    onToggleFavorite: (LikeableRecipe) -> Unit,
) {
    composable(
        route = "recipe/{id}",
        arguments = listOf(navArgument("id") { type = NavType.StringType }),
        deepLinks =
            listOf(
                navDeepLink {
                    uriPattern = "myapp://recipe/{id}"
                },
            ),
    ) { backStackEntry ->
        val recipeId = backStackEntry.arguments?.getString("id")

        val viewModel: DetailRecipeViewModel = hiltViewModel()

        LaunchedEffect(recipeId) {
            recipeId?.let { viewModel.loadRecipeById(it) }
        }

        DetailRecipeScreen(
            viewModel = viewModel,
            onToggleFavorite = onToggleFavorite,
            onBackCLick = onBackClick,
        )
    }
}

@Composable
internal fun DetailRecipeRoute(
    viewModel: DetailRecipeViewModel = hiltViewModel(),
    onBackCLick: () -> Unit,
    onToggleFavorite: (LikeableRecipe) -> Unit,
) {
    DetailRecipeScreen(viewModel, onToggleFavorite, onBackCLick)
    LaunchedEffect(Unit) {
        ScreenCounter.increment()
    }
}
