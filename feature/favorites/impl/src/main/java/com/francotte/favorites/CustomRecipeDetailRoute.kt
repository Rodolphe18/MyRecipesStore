package com.francotte.favorites

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.francotte.api.CustomRecipeNavKey
import com.francotte.navigation.Navigator


fun EntryProviderScope<NavKey>.customRecipeEntry(navigator: Navigator) {
    entry<CustomRecipeNavKey> { key ->
        CustomRecipeDetailRoute(onBackCLick = navigator::goBack)
    }
}


@Composable
internal fun CustomRecipeDetailRoute(
    viewModel: CustomRecipeDetailViewModel = hiltViewModel(),
    onBackCLick: () -> Unit,
) {
    val customRecipe by viewModel.recipe.collectAsStateWithLifecycle()
    CustomRecipeDetailScreen(viewModel = viewModel, customRecipe = customRecipe, onBackCLick = onBackCLick)
}
