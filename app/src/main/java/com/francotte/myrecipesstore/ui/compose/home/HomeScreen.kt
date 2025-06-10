package com.francotte.myrecipesstore.ui.compose.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.francotte.myrecipesstore.domain.model.LikeableRecipe
import com.francotte.myrecipesstore.ui.compose.composables.CustomCircularProgressIndicator
import com.francotte.myrecipesstore.ui.compose.composables.ErrorScreen
import com.francotte.myrecipesstore.ui.compose.composables.HorizontalRecipesList


@Composable
fun HomeScreen(
    homeUiState: HomeUiState,
    onReload: () -> Unit,
    onOpenRecipe: (LikeableRecipe) -> Unit,
    onToggleFavorite: (LikeableRecipe, Boolean) -> Unit,
    onOpenSection: (String) -> Unit
) {

    when (homeUiState) {
        HomeUiState.Loading -> CustomCircularProgressIndicator()
        HomeUiState.Error -> ErrorScreen { onReload() }
        is HomeUiState.Success -> {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                HorizontalRecipesList(
                    "Dernières recettes",
                    homeUiState.latestRecipes,
                    onOpenRecipe = onOpenRecipe,
                    onOpenSection = { onOpenSection("latest_recipes") },
                    onToggleFavorite = onToggleFavorite
                )
                homeUiState.areaSections.forEach { map ->
                    HorizontalRecipesList(
                        map.key,
                        homeUiState.areaSections[map.key]!!,
                        onOpenRecipe = onOpenRecipe,
                        onOpenSection = { onOpenSection(map.key) },
                        onToggleFavorite = onToggleFavorite
                    )
                }

            }
        }


    }

}
