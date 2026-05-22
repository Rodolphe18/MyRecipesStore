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
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.francotte.ads.BannerAd
import com.francotte.ads.BannerPlacement
import com.francotte.designsystem.component.CustomCircularProgressIndicator
import com.francotte.model.LikeableRecipe
import com.francotte.model.Recipe
import com.francotte.testing.util.HomeTags
import com.francotte.ui.BigRecipeItem
import com.francotte.ui.DeviceMode
import com.francotte.ui.ErrorScreen
import com.francotte.ui.HorizontalRecipesList
import com.francotte.ui.LocalAppLayout
import com.francotte.ui.LocalBannerProvider
import com.francotte.ui.SectionTitle
import com.francotte.ui.SimpleHorizontalRecipesList
import com.francotte.ui.VideoRecipeItem
import com.francotte.ui.nbHomeColumns
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onOpenRecipe: (List<String>, Int, String) -> Unit,
    onToggleFavorite: (LikeableRecipe) -> Unit,
    onOpenSection: (String) -> Unit,
    onVideoButtonClick: (String) -> Unit,
    onRefreshAll: () -> Unit,
    onRetryLatest: () -> Unit,
    onRetryJapanese: () -> Unit,
    onRetryAreas: () -> Unit,
    onRetryEnglish: () -> Unit,
    onCurrentPageChange: (Int) -> Unit
) {
    val mode = LocalAppLayout.current.mode
    val localBannerProvider = LocalBannerProvider.current
    val spanSize = GridItemSpan(mode.nbHomeColumns)
    val scrollState = rememberLazyGridState()
    val pullRefreshState = rememberPullToRefreshState()
    val isDataReady = uiState.latest.hasRecipes
    val areaSections =
        remember(uiState.areas) { uiState.areas.recipes.toList().sortedBy { it.first } }
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
        isRefreshing = uiState.isRefreshing,
        onRefresh = onRefreshAll,
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
                        uiState.latest.error -> ErrorScreen { onRetryLatest() }
                        uiState.latest.loading -> {
                            CustomCircularProgressIndicator()
                        }

                        uiState.latest.hasRecipes -> {
                            if (mode == DeviceMode.PhonePortrait) {
                                val pagerState =
                                    rememberPagerState(
                                        initialPage = uiState.latest.currentPage,
                                        pageCount = { uiState.latest.recipes.size },
                                    )

                                LaunchedEffect(pagerState) {
                                    snapshotFlow { pagerState.currentPage }
                                        .distinctUntilChanged()
                                        .collect(onCurrentPageChange)
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
                                        likeableRecipe = uiState.latest.recipes[index],
                                        onOpenRecipe = {
                                            onOpenRecipe(
                                                uiState.latest.recipes.map { it.recipe.idMeal },
                                                index,
                                                uiState.latest.recipes[index].recipe.strMeal,
                                            )
                                        },
                                        onToggleFavorite = onToggleFavorite,
                                        onVideoButtonClick = {
                                            onVideoButtonClick((uiState.latest.recipes[index].recipe as Recipe).strYoutube)
                                        },
                                    )
                                }
                            } else {
                                SimpleHorizontalRecipesList(
                                    recipes = uiState.latest.recipes,
                                    onOpenRecipe = onOpenRecipe,
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
                    uiState.japanese.error -> ErrorScreen { onRetryJapanese() }
                    uiState.japanese.loading -> {
                        CustomCircularProgressIndicator()
                    }

                    uiState.japanese.hasRecipes -> {
                        HorizontalRecipesList(
                            "Japanese",
                            uiState.japanese.recipes,
                            onOpenRecipe = onOpenRecipe,
                            onOpenSection = onOpenSection,
                            onToggleFavorite = onToggleFavorite,
                        )
                    }
                }
            }

            when {
                uiState.areas.error -> item(span = { spanSize }) { ErrorScreen { onRetryAreas() } }
                uiState.areas.loading -> item { }
                uiState.areas.hasRecipes -> {
                    areaSections.forEach { (key, list) ->
                        item(
                            key = "area_section_$key",
                            contentType = "section",
                            span = { spanSize },
                        ) {
                            HorizontalRecipesList(
                                key,
                                list,
                                onOpenRecipe = onOpenRecipe,
                                onOpenSection = { onOpenSection(key) },
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
                uiState.english.error -> item { ErrorScreen { onRetryEnglish() } }
                uiState.english.loading -> item { CustomCircularProgressIndicator() }
                uiState.english.hasRecipes -> {
                    items(
                        items = uiState.english.recipes,
                        key = { it.recipe.idMeal },
                        contentType = { "recipe_big" },
                    ) { recipe ->
                        BigRecipeItem(
                            recipe,
                            onToggleFavorite = onToggleFavorite,
                            onOpenRecipe = {
                                onOpenRecipe(
                                    uiState.english.recipes.map { it.recipe.idMeal },
                                    uiState.english.recipes.indexOf(recipe),
                                    recipe.recipe.strMeal,
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}


