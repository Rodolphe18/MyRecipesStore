package com.francotte.myrecipesstore.ui.compose.detail

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.francotte.myrecipesstore.domain.model.LikeableRecipe
import com.francotte.myrecipesstore.util.ScreenCounter
import kotlinx.serialization.Serializable

@Serializable
data class DetailRecipeRoute(val ids:List<String>?, val index:Int?, val title:String?)

fun NavController.navigateToDetailRecipeScreen(ids:List<String>, index:Int, title:String, navOptions: NavOptionsBuilder.() -> Unit = {}) {
    navigate(route = DetailRecipeRoute(ids, index,title)) {
        navOptions()
    }
}


fun NavGraphBuilder.detailRecipeScreen(onBackClick:() -> Unit,onToggleFavorite:(LikeableRecipe, Boolean)->Unit) {
    composable<DetailRecipeRoute> {
        DetailRecipeRoute(
            onToggleFavorite = onToggleFavorite,
            onBackCLick = onBackClick
        )
    }

}

fun NavGraphBuilder.deepLinkRecipeScreen(onBackClick:() -> Unit,onToggleFavorite:(LikeableRecipe, Boolean)->Unit) {

    composable(
        route = "recipe/{id}",
        arguments = listOf(navArgument("id") { type = NavType.StringType }),
        deepLinks = listOf(navDeepLink {
            uriPattern = "myapp://recipe/{id}"
        })
    ) { backStackEntry ->
        val recipeId = backStackEntry.arguments?.getString("id")

        val viewModel: DetailRecipeViewModel = hiltViewModel()

        LaunchedEffect(recipeId) {
            recipeId?.let { viewModel.loadRecipeById(it) }
        }

        DetailRecipeScreen(
            viewModel = viewModel,
            onToggleFavorite = onToggleFavorite,
            onBackCLick = onBackClick
        )
    }

}

@Composable
internal fun DetailRecipeRoute(viewModel: DetailRecipeViewModel= hiltViewModel(), onBackCLick:() -> Unit,onToggleFavorite:(LikeableRecipe, Boolean)->Unit) {
    DetailRecipeScreen(viewModel,onToggleFavorite, onBackCLick)
    LaunchedEffect(Unit) {
        ScreenCounter.increment()
    }
}