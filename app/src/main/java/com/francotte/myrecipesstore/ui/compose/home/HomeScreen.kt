package com.francotte.myrecipesstore.ui.compose.home

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.francotte.myrecipesstore.R
import com.francotte.myrecipesstore.model.AbstractRecipe
import com.francotte.myrecipesstore.model.RecipeResult
import com.francotte.myrecipesstore.model.toMealList
import com.francotte.myrecipesstore.ui.compose.composables.ErrorScreen
import com.francotte.myrecipesstore.ui.compose.composables.HorizontalRecipesList
import com.francotte.myrecipesstore.ui.compose.section.SectionType
import com.francotte.myrecipesstore.ui.navigation.TopAppBar


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeUiState: HomeUiState,
    onReload: () -> Unit,
    onOpenRecipe: (AbstractRecipe) -> Unit,
    onToggleFavorite: (AbstractRecipe) -> Unit,
    onOpenSection: (SectionType) -> Unit
) {
    val topAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    Scaffold(
        modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                R.string.home, Icons.Filled.Search, ""
            )
        }
    ) { _ ->
        when (homeUiState) {
            HomeUiState.Loading -> Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            HomeUiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                ErrorScreen { onReload() }
            }
            is HomeUiState.Success -> {
                homeUiState.latestRecipes?.let { mealResult ->
                    HorizontalRecipesList(
                        "Dernières recettes",
                        mealResult.toMealList(),
                        onOpenRecipe = onOpenRecipe,
                        onOpenSection = { onOpenSection(SectionType.LATEST_RECIPES) },
                        onToggleFavorite = onToggleFavorite)
                }
                homeUiState.randomRecipes?.let { mealResult ->

                    HorizontalRecipesList(
                        "Top recettes",
                        mealResult.toMealList(),
                        onOpenRecipe = onOpenRecipe,
                        onOpenSection = { onOpenSection(SectionType.TOP_RECIPES) },
                        onToggleFavorite = onToggleFavorite)
                }
            }
        }
    }

}

