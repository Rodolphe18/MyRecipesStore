package com.francotte.myrecipesstore.ui.compose.section

import android.annotation.SuppressLint
import androidx.annotation.StringRes
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
import com.francotte.myrecipesstore.model.AbstractRecipe
import com.francotte.myrecipesstore.model.toMealList
import com.francotte.myrecipesstore.ui.compose.composables.CustomCircularProgressIndicator
import com.francotte.myrecipesstore.ui.compose.composables.ErrorScreen
import com.francotte.myrecipesstore.ui.compose.composables.RecipeItem
import com.francotte.myrecipesstore.ui.navigation.TopAppBar

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectionScreen(sectionUiState: SectionUiState, @StringRes titleRes: Int, onReload:() -> Unit, onOpenRecipe:(AbstractRecipe) -> Unit) {
    val topAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    Scaffold(
        modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
               titleRes= titleRes, actionIcon = Icons.Filled.Search, scrollBehavior = topAppBarScrollBehavior, navigationIconEnabled = true
            )
        }
    ) { padding ->
        when (sectionUiState) {
            SectionUiState.Loading -> CustomCircularProgressIndicator()
            SectionUiState.Error -> ErrorScreen { onReload() }
            is SectionUiState.Success -> {
                LazyVerticalGrid(
                    state = rememberLazyGridState(),
                    columns = GridCells.Fixed(2),
                    reverseLayout = false,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    flingBehavior = ScrollableDefaults.flingBehavior(),
                    contentPadding = PaddingValues(
                        top = padding.calculateTopPadding(),
                        bottom = 16.dp,
                        start = 16.dp,
                        end = 16.dp)
                ) {
                    items(
                        items = sectionUiState.recipes.toMealList(),
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
