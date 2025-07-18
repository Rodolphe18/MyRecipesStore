package com.francotte.myrecipesstore.ui.compose.categories.category

import android.annotation.SuppressLint
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.francotte.myrecipesstore.domain.model.LikeableRecipe
import com.francotte.myrecipesstore.ui.compose.composables.CustomCircularProgressIndicator
import com.francotte.myrecipesstore.ui.compose.composables.ErrorScreen
import com.francotte.myrecipesstore.ui.compose.composables.RecipeItem
import com.francotte.myrecipesstore.ui.compose.composables.nbSectionColumns
import com.francotte.myrecipesstore.ui.navigation.TopAppBar

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(categoryUiState: CategoryUiState,windowSizeClass:WindowSizeClass, title:String, onReload:() -> Unit, onOpenRecipe:(List<String>,Int,String) -> Unit, onToggleFavorite:(LikeableRecipe, Boolean) -> Unit, onBack:()->Unit) {
    val topAppBarScrollBehavior =
        TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    Scaffold(
        modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(title = title, navigationIconEnabled = true, onNavigationClick = onBack, scrollBehavior = topAppBarScrollBehavior )
        }
    ) { padding ->
        when (categoryUiState) {
            CategoryUiState.Loading -> CustomCircularProgressIndicator()
            CategoryUiState.Error -> ErrorScreen { onReload() }
            is CategoryUiState.Success -> {
                LazyVerticalGrid(
                    state = rememberLazyGridState(),
                    columns = GridCells.Fixed(windowSizeClass.widthSizeClass.nbSectionColumns),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    flingBehavior = ScrollableDefaults.flingBehavior(),
                    contentPadding = PaddingValues(
                        top = padding.calculateTopPadding(),
                        bottom = 16.dp,
                        start = 16.dp,
                        end = 16.dp)
                ) {
                    val likeableRecipes = categoryUiState.recipes
                    itemsIndexed(
                        items = likeableRecipes,
                        key = { index, likeableRecipe -> likeableRecipe.recipe.idMeal + index }
                    ) { index, likeableRecipe ->
                        RecipeItem(
                            likeableRecipe = likeableRecipe,
                            onToggleFavorite = onToggleFavorite,
                            onOpenRecipe = {
                                onOpenRecipe(likeableRecipes.map { it.recipe.idMeal }, index, likeableRecipe.recipe.strMeal)
                            })
                    }
                }
            }

        }
    }
}