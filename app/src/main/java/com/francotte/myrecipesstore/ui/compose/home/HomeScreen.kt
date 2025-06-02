package com.francotte.myrecipesstore.ui.compose.home

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.francotte.myrecipesstore.R
import com.francotte.myrecipesstore.model.AbstractRecipe
import com.francotte.myrecipesstore.model.RecipeResult
import com.francotte.myrecipesstore.model.toMealList
import com.francotte.myrecipesstore.ui.compose.composables.CustomCircularProgressIndicator
import com.francotte.myrecipesstore.ui.compose.composables.ErrorScreen
import com.francotte.myrecipesstore.ui.compose.composables.HorizontalRecipesList
import com.francotte.myrecipesstore.ui.compose.section.SectionType
import com.francotte.myrecipesstore.ui.navigation.TopAppBar


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(
    homeUiState: HomeUiState,
    onReload: () -> Unit,
    onOpenRecipe: (AbstractRecipe) -> Unit,
    onToggleFavorite: (AbstractRecipe) -> Unit,
    onOpenSection: (SectionType) -> Unit
) {
    when (homeUiState) {
        HomeUiState.Loading -> CustomCircularProgressIndicator()
        HomeUiState.Error -> ErrorScreen { onReload() }
        is HomeUiState.Success -> {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                homeUiState.latestRecipes?.let { mealResult ->
                    HorizontalRecipesList(
                        "Dernières recettes",
                        mealResult.toMealList(),
                        onOpenRecipe = onOpenRecipe,
                        onOpenSection = { onOpenSection(SectionType.LATEST_RECIPES) },
                        onToggleFavorite = onToggleFavorite
                    )
                }
                homeUiState.randomRecipes?.let { mealResult ->
                    HorizontalRecipesList(
                        "Top recettes",
                        mealResult.toMealList(),
                        onOpenRecipe = onOpenRecipe,
                        onOpenSection = { onOpenSection(SectionType.TOP_RECIPES) },
                        onToggleFavorite = onToggleFavorite
                    )
                }
            }

        }
    }
}


