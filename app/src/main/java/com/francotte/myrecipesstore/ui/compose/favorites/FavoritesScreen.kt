package com.francotte.myrecipesstore.ui.compose.favorites

import android.annotation.SuppressLint
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
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
import androidx.compose.ui.unit.dp
import com.francotte.myrecipesstore.R
import com.francotte.myrecipesstore.model.AbstractRecipe
import com.francotte.myrecipesstore.model.toMealList
import com.francotte.myrecipesstore.ui.compose.categories.category.CategoryUiState
import com.francotte.myrecipesstore.ui.compose.composables.ErrorScreen
import com.francotte.myrecipesstore.ui.compose.composables.RecipeItem
import com.francotte.myrecipesstore.ui.navigation.TopAppBar

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(favoritesUiState: CategoryUiState, onReload:() -> Unit, onOpenRecipe:(AbstractRecipe) -> Unit) {
    val topAppBarScrollBehavior =
        TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    Scaffold(
        modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
               titleRes =  R.string.favorites, actionIcon = Icons.Filled.Search
            )
        }
    ) { _ ->
        when (favoritesUiState) {
            CategoryUiState.Loading -> Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            CategoryUiState.Error -> Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                ErrorScreen { onReload() }
            }

            is CategoryUiState.Success -> {
                LazyVerticalGrid(
                    state = rememberLazyGridState(),
                    columns = GridCells.Fixed(2),
                    reverseLayout = false,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    flingBehavior = ScrollableDefaults.flingBehavior(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(
                        items = favoritesUiState.recipes.toMealList(),
                        key = { it.idMeal }
                    ) { recipe ->
                        RecipeItem(recipe = recipe, {
                        }, {
                            onOpenRecipe(it)
                        })
                    }
                }
            }

        }
    }
}