package com.francotte.categories

import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.francotte.designsystem.component.CustomCircularProgressIndicator
import com.francotte.designsystem.component.TopAppBar
import com.francotte.ui.nbSectionColumns
import com.francotte.ui.FullErrorScreen
import com.francotte.ui.RecipeItem
import com.francotte.ui.TrackScrollJank
import com.francotte.ui.rememberDeviceMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    state: CategoryState,
    onAction: (CategoryAction) -> Unit,
) {
    val mode = rememberDeviceMode()
    val topAppBarScrollBehavior =
        TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val pullToRefreshState = rememberPullToRefreshState()
    Scaffold(
        modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = state.title,
                navigationIconEnabled = true,
                onNavigationClick = { onAction(CategoryAction.OnBackClick) },
                scrollBehavior = topAppBarScrollBehavior,
            )
        },
    ) { padding ->
        PullToRefreshBox(
            modifier = Modifier.padding(top = padding.calculateTopPadding()),
            isRefreshing = state.isRefreshing,
            onRefresh = { onAction(CategoryAction.OnReload) },
            state = pullToRefreshState,
        ) {
            when {
                state.isLoading -> CustomCircularProgressIndicator()
                state.error != null -> FullErrorScreen()
                else -> {
                    val scrollableState = rememberLazyGridState()
                    TrackScrollJank(scrollableState = scrollableState, stateName = "categories:grid")
                    LazyVerticalGrid(
                        state = scrollableState,
                        columns = GridCells.Fixed(mode.nbSectionColumns),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        flingBehavior = ScrollableDefaults.flingBehavior(),
                        contentPadding =
                            PaddingValues(
                                top = 0.dp,
                                bottom = 16.dp,
                                start = 16.dp,
                                end = 16.dp,
                            ),
                    ) {
                        val likeableRecipes = state.recipes
                        itemsIndexed(
                            items = likeableRecipes,
                            key = { index, likeableRecipe -> likeableRecipe.recipe.idMeal + index },
                        ) { index, likeableRecipe ->
                            RecipeItem(
                                likeableRecipe = likeableRecipe,
                                onToggleFavorite = { onAction(CategoryAction.OnToggleFavorite(it)) },
                                onOpenRecipe = { onAction(CategoryAction.OnRecipeClick(index)) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun CategoryScreenPreview() {
    CategoryScreen(state = CategoryState(title = "Beef"), onAction = {})
}
