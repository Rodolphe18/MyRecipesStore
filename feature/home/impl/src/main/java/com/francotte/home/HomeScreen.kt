package com.francotte.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.francotte.ads.BannerAd
import com.francotte.ads.BannerPlacement
import com.francotte.designsystem.component.CustomCircularProgressIndicator
import com.francotte.model.LikeableRecipe
import com.francotte.ui.BigRecipeItem
import com.francotte.ui.DeviceMode
import com.francotte.ui.SectionErrorScreen
import com.francotte.ui.HorizontalRecipesList
import com.francotte.ui.LocalBannerProvider
import com.francotte.ui.SectionTitle
import com.francotte.ui.SimpleHorizontalRecipesList
import com.francotte.ui.VideoRecipeItem
import com.francotte.ui.nbHomeColumns
import com.francotte.ui.rememberDeviceMode
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeState,
    onAction: (HomeAction) -> Unit,
) {
    val onToggleFavorite: (LikeableRecipe) -> Unit = { onAction(HomeAction.OnToggleFavorite(it)) }

    val mode = rememberDeviceMode()
    val localBannerProvider = LocalBannerProvider.current
    val spanSize = GridItemSpan(mode.nbHomeColumns)
    val scrollState = rememberLazyGridState()
    val pullRefreshState = rememberPullToRefreshState()
    val isDataReady = state.latest.hasRecipes
    PullToRefreshBox(
        modifier =
            Modifier
                .fillMaxSize()
                .then(
                    if (isDataReady) {
                        Modifier
                            .testTag("homeScreenReady")
                            .semantics { contentDescription = "homeScreenReady" }
                    } else {
                        Modifier
                    },
                ),
        isRefreshing = state.isRefreshing,
        onRefresh = { onAction(HomeAction.OnRefreshAll) },
        state = pullRefreshState,
    ) {
        LazyVerticalGrid(
            modifier =
                Modifier
                    .fillMaxSize()
                    .testTag("feed")
                    .semantics { contentDescription = "feed" },
            state = scrollState,
            contentPadding = PaddingValues(bottom = 16.dp),
            columns = GridCells.Fixed(mode.nbHomeColumns),
        ) {
            item(key = "latest_section", contentType = "section", span = { spanSize }) {
                Column {
                    SectionTitle(
                        title = stringResource(R.string.latest_recipes_title),
                        count = null,
                        showNavIcon = false,
                    )
                    when {
                        state.latest.error -> SectionErrorScreen { onAction(HomeAction.OnRetryLatest) }
                        state.latest.loading -> {
                            CustomCircularProgressIndicator()
                        }

                        state.latest.hasRecipes -> {
                            if (mode == DeviceMode.PhonePortrait) {
                                val pagerState =
                                    rememberPagerState(
                                        initialPage = state.latest.currentPage,
                                        pageCount = { state.latest.recipes.size },
                                    )

                                LaunchedEffect(pagerState) {
                                    snapshotFlow { pagerState.currentPage }
                                        .distinctUntilChanged()
                                        .collect { onAction(HomeAction.OnCurrentPageChange(it)) }
                                }
                                HorizontalPager(state = pagerState) { index ->
                                    VideoRecipeItem(
                                        modifier =
                                            Modifier
                                                .testTag("VideoRecipeItem_$index")
                                                .semantics {
                                                    contentDescription =
                                                        "VideoRecipeItem_$index"
                                                },
                                        likeableRecipe = state.latest.recipes[index],
                                        onOpenRecipe = {
                                            onAction(HomeAction.OnRecipeClick(HomeRecipeSource.Latest, index))
                                        },
                                        onToggleFavorite = onToggleFavorite,
                                        onVideoButtonClick = {
                                            onAction(HomeAction.OnVideoClick(index))
                                        },
                                    )
                                }
                            } else {
                                SimpleHorizontalRecipesList(
                                    recipes = state.latest.recipes,
                                    onOpenRecipe = { index ->
                                        onAction(HomeAction.OnRecipeClick(HomeRecipeSource.Latest, index))
                                    },
                                    onToggleFavorite = onToggleFavorite,
                                )
                            }
                        }
                    }
                }
            }
            if (mode != DeviceMode.PhoneLandscape) {
                item(span = { spanSize }) { Spacer(Modifier.height(16.dp)) }
                item(key = "banner_top", contentType = "ad", span = { spanSize }) {
                    BannerAd(
                        placement = BannerPlacement.HOME_POS_1,
                        provider = localBannerProvider,
                        horizontalPadding = 16.dp,
                    )
                }
            }
            item(
                key = "japanese_section",
                contentType = "section",
                span = { spanSize },
            ) {
                when {
                    state.japanese.error -> SectionErrorScreen { onAction(HomeAction.OnRetryJapanese) }
                    state.japanese.loading -> {
                        CustomCircularProgressIndicator()
                    }

                    state.japanese.hasRecipes -> {
                        HorizontalRecipesList(
                            "Japanese",
                            state.japanese.recipes,
                            onOpenRecipe = { index ->
                                onAction(HomeAction.OnRecipeClick(HomeRecipeSource.Japanese, index))
                            },
                            onOpenSection = { onAction(HomeAction.OnOpenSection(it)) },
                            onToggleFavorite = onToggleFavorite,
                        )
                    }
                }
            }

            when {
                state.areas.error -> item(span = { spanSize }) { SectionErrorScreen { onAction(HomeAction.OnRetryAreas) } }
                state.areas.loading -> item { }
                state.areas.hasRecipes -> {
                    state.areas.sortedSections.forEach { (key, list) ->
                        item(
                            key = "area_section_$key",
                            contentType = "section",
                            span = { spanSize },
                        ) {
                            HorizontalRecipesList(
                                key,
                                list,
                                onOpenRecipe = { index ->
                                    onAction(HomeAction.OnRecipeClick(HomeRecipeSource.Area(key), index))
                                },
                                onOpenSection = { onAction(HomeAction.OnOpenSection(key)) },
                                onToggleFavorite = onToggleFavorite,
                            )
                        }
                    }
                }
            }
            item(span = { spanSize }) { Spacer(Modifier.height(4.dp)) }
            item(
                key = "banner_mid",
                contentType = "ad",
                span = { spanSize },
            ) {
                BannerAd(
                    placement = BannerPlacement.HOME_POS_2,
                    provider = localBannerProvider,
                    horizontalPadding = 16.dp,
                )
            }
            item(span = { spanSize }) { Spacer(Modifier.height(4.dp)) }
            item(span = { spanSize }) {
                Text(
                    text = "English recipes",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            when {
                state.english.error -> item { SectionErrorScreen { onAction(HomeAction.OnRetryEnglish) } }
                state.english.loading -> item { CustomCircularProgressIndicator() }
                state.english.hasRecipes -> {
                    itemsIndexed(
                        items = state.english.recipes,
                        key = { _, recipe -> recipe.recipe.idMeal },
                        contentType = { _, _ -> "recipe_big" },
                    ) { index, recipe ->
                        BigRecipeItem(
                            recipe,
                            onToggleFavorite = onToggleFavorite,
                            onOpenRecipe = {
                                onAction(HomeAction.OnRecipeClick(HomeRecipeSource.English, index))
                            },
                        )
                    }
                }
            }
        }
    }
}
