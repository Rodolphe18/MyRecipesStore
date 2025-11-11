package com.francotte.favorites

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import com.francotte.model.CustomIngredient
import kotlinx.serialization.Serializable


@Serializable
data class CustomRecipeDetailRoute(val id: String)

fun NavController.navigateToCustomRecipeDetailScreen(id:String, navOptions: NavOptionsBuilder.() -> Unit = {}) {
    this.navigate(CustomRecipeDetailRoute(id), navOptions)
}

fun NavGraphBuilder.customRecipeDetailScreen(onBackClick:() -> Unit) {
    composable<CustomRecipeDetailRoute> {
       CustomRecipeDetailRoute(onBackCLick = onBackClick)
    }
}

@Composable
internal fun CustomRecipeDetailRoute(viewModel: CustomRecipeDetailViewModel= hiltViewModel(), onBackCLick:() -> Unit) {
    val customRecipe by viewModel.recipe.collectAsStateWithLifecycle()
    CustomRecipeDetailScreen(viewModel = viewModel, customRecipe = customRecipe, onBackCLick= onBackCLick)
}