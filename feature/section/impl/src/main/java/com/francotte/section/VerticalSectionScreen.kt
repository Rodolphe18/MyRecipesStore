package com.francotte.section

import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.francotte.ads.BannerAd
import com.francotte.ads.BannerPlacement
import com.francotte.ads.LazyGridWithBanners
import com.francotte.designsystem.component.CustomCircularProgressIndicator
import com.francotte.designsystem.component.TopAppBar
import com.francotte.ui.nbSectionColumns
import com.francotte.ui.FullErrorScreen
import com.francotte.ui.LocalBannerProvider
import com.francotte.ui.RecipeItem
import com.francotte.ui.TrackScrollJank
import com.francotte.ui.rememberDeviceMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerticalSectionScreen(
    state: SectionState,
    onAction: (SectionAction) -> Unit,
) {
    val mode = rememberDeviceMode()
    val localBannerProvider = LocalBannerProvider.current
    val topAppBarScrollBehavior =
        TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val pullToRefreshState = rememberPullToRefreshState()
    Scaffold(
        modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = state.title,
                scrollBehavior = topAppBarScrollBehavior,
                navigationIconEnabled = true,
                onNavigationClick = { onAction(SectionAction.OnBackClick) },
            )
        },
    ) { padding ->
        PullToRefreshBox(
            modifier = Modifier.padding(top = padding.calculateTopPadding()),
            isRefreshing = state.isRefreshing,
            onRefresh = { onAction(SectionAction.OnReload) },
            state = pullToRefreshState,
        ) {
            when {
                state.recipes.isNotEmpty() -> {
                    val gridState = rememberLazyGridState()
                    TrackScrollJank(scrollableState = gridState, stateName = "section:grid")
                    val likeableRecipes = state.recipes
                    LazyGridWithBanners(
                        modifier =
                            Modifier
                                .testTag("full_section_screen")
                                .semantics { contentDescription = "full_section_screen" },
                        totalItemCount = likeableRecipes.size,
                        columns = mode.nbSectionColumns,
                        state = gridState,
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
                        itemKey = { index -> likeableRecipes[index].recipe.idMeal },
                        itemContentType = { "recipe" },
                        bannerKey = { bannerIndex -> "section-banner-$bannerIndex" },
                        bannerContentType = "ad",
                        bannerContent = {
                            BannerAd(placement = BannerPlacement.FOOD_LIST, provider = localBannerProvider, horizontalPadding = 16.dp)
                        },
                    ) { index ->
                        val likeableRecipe = likeableRecipes[index]
                        RecipeItem(
                            likeableRecipe = likeableRecipe,
                            onToggleFavorite = {
                                onAction(SectionAction.OnToggleFavorite(likeableRecipe))
                            },
                            onOpenRecipe = {
                                onAction(SectionAction.OnRecipeClick(index))
                            },
                        )
                    }
                }
                state.isLoading -> CustomCircularProgressIndicator()
                else -> FullErrorScreen()
            }
        }
    }
}
