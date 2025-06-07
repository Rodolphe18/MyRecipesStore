package com.francotte.myrecipesstore.ui.compose.categories.category

import android.annotation.SuppressLint
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.francotte.myrecipesstore.model.AbstractRecipe
import com.francotte.myrecipesstore.model.LikeableRecipe
import com.francotte.myrecipesstore.ui.compose.composables.CustomCircularProgressIndicator
import com.francotte.myrecipesstore.ui.compose.composables.ErrorScreen
import com.francotte.myrecipesstore.ui.compose.composables.RecipeItem

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(categoryUiState: CategoryUiState, title:String, onReload:() -> Unit, onOpenRecipe:(AbstractRecipe) -> Unit, onToggleFavorite:(LikeableRecipe,Boolean) -> Unit) {
    val topAppBarScrollBehavior =
        TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    Scaffold(
        modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(title = { Text(title) }, navigationIcon = {Icons.Filled.ArrowBack}, scrollBehavior = topAppBarScrollBehavior )
        }
    ) { padding ->
        when (categoryUiState) {
            CategoryUiState.Loading -> CustomCircularProgressIndicator()
            CategoryUiState.Error -> ErrorScreen { onReload() }
            is CategoryUiState.Success -> {
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
                        items = categoryUiState.recipes,
                        key = { it.recipe.idMeal }
                    ) { recipe ->
                        RecipeItem(likeableRecipe = recipe, onToggleFavorite = onToggleFavorite, onOpenRecipe= onOpenRecipe
                        )
                    }
                }
            }

        }
    }
}