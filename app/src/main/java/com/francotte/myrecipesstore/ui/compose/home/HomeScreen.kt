package com.francotte.myrecipesstore.ui.compose.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.francotte.myrecipesstore.model.AbstractRecipe
import com.francotte.myrecipesstore.model.LikeableRecipe
import com.francotte.myrecipesstore.ui.compose.composables.CustomCircularProgressIndicator
import com.francotte.myrecipesstore.ui.compose.composables.ErrorScreen
import com.francotte.myrecipesstore.ui.compose.composables.HorizontalRecipesList
import com.francotte.myrecipesstore.ui.compose.section.SectionType
import com.francotte.myrecipesstore.ui.compose.section.SectionUiState
import kotlinx.coroutines.flow.collectLatest


@Composable
fun HomeScreen(
    homeUiState: HomeUiState,
    onReload: () -> Unit,
    onOpenRecipe: (AbstractRecipe) -> Unit,
    onToggleFavorite: (LikeableRecipe, Boolean) -> Unit,
    onOpenSection: (SectionType) -> Unit
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
                    onOpenSection = { onOpenSection(SectionType.LATEST_RECIPES) },
                    onToggleFavorite = onToggleFavorite
                )
                HorizontalRecipesList(
                    "Top recettes",
                    homeUiState.topRecipes,
                    onOpenRecipe = onOpenRecipe,
                    onOpenSection = { onOpenSection(SectionType.TOP_RECIPES) },
                    onToggleFavorite = onToggleFavorite
                )
            }
        }


    }

}
