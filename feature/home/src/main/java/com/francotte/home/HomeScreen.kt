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
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.francotte.designsystem.component.nbHomeColumns
import com.francotte.model.LikeableRecipe
import com.francotte.model.Recipe
import com.francotte.testing.util.HomeTags
import com.francotte.ui.BigRecipeItem
import com.francotte.ui.ErrorScreen
import com.francotte.ui.HorizontalRecipesList
import com.francotte.ui.LocalBannerProvider
import com.francotte.ui.SectionTitle
import com.francotte.ui.SimpleHorizontalRecipesList
import com.francotte.ui.VideoRecipeItem
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    windowSizeClass: WindowSizeClass,
    latestRecipes: LatestRecipes,
    americanRecipes: AmericanRecipes,
    areasRecipes: AreasRecipes,
    englishRecipes: EnglishRecipes,
    onOpenRecipe: (List<String>, Int, String) -> Unit,
    onToggleFavorite: (LikeableRecipe, Boolean) -> Unit,
    onOpenSection: (String) -> Unit,
    onVideoButtonClick: (String) -> Unit,
    isReloading: Boolean,
    onReload: () -> Unit,
    currentPage: Int,
    onCurrentPageChange: (Int) -> Unit,
) {
    val localBannerProvider = LocalBannerProvider.current
    val spanSize = GridItemSpan(windowSizeClass.widthSizeClass.nbHomeColumns)
    val scrollState = rememberLazyGridState()
    val pullRefreshState = rememberPullToRefreshState()
    val coroutineScope = rememberCoroutineScope()
    val isDataReady =
        latestRecipes is LatestRecipes.Success && americanRecipes is AmericanRecipes.Success
    val areaSections = remember(areasRecipes) {
        (areasRecipes as? AreasRecipes.Success)
            ?.areasRecipes
            ?.toList()
            ?.sortedBy { it.first }
            ?: emptyList()
    }
    PullToRefreshBox(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (isDataReady)
                    Modifier
                        .testTag("homeScreenReady")
                        .semantics { contentDescription = "homeScreenReady" } else Modifier
            ),
        isRefreshing = isReloading,
        onRefresh = {
            coroutineScope.launch {
                onReload()
            }
        },
        state = pullRefreshState
    ) {
        if (latestRecipes == LatestRecipes.Loading) {
            CustomCircularProgressIndicator(Modifier.semantics {
                testTag = HomeTags.LOADING
            })
        } else {
            LazyVerticalGrid(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("feed")
                    .semantics { contentDescription = "feed" },
                state = scrollState,
                contentPadding = PaddingValues(bottom = 16.dp),
                columns = GridCells.Fixed(windowSizeClass.widthSizeClass.nbHomeColumns)
            ) {
                item(key = "latest_section", contentType = "section", span = { spanSize }) {
                    Column {
                        SectionTitle(
                            title = stringResource(R.string.latest_recipes_title),
                            count = null,
                            showNavIcon = false
                        )
                        when (latestRecipes) {
                            LatestRecipes.Error -> ErrorScreen { onReload() }
                            LatestRecipes.Loading -> {}
                            is LatestRecipes.Success -> {
                                if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact) {
                                    val pagerState = rememberPagerState(
                                        initialPage = currentPage,
                                        pageCount = { latestRecipes.latestRecipes.size }
                                    )
                                    LaunchedEffect(currentPage) {
                                        if (pagerState.currentPage != currentPage) {
                                            pagerState.scrollToPage(currentPage)
                                        }
                                    }
                                    LaunchedEffect(pagerState) {
                                        snapshotFlow { pagerState.currentPage }
                                            .distinctUntilChanged()
                                            .collect(onCurrentPageChange)
                                    }
                                    HorizontalPager(state = pagerState) { index ->
                                        VideoRecipeItem(
                                            modifier = Modifier
                                                .testTag("VideoRecipeItem_$index")
                                                .semantics {
                                                    contentDescription = "VideoRecipeItem_$index"
                                                },
                                            likeableRecipe = latestRecipes.latestRecipes[index],
                                            onOpenRecipe = {
                                                onOpenRecipe(
                                                    latestRecipes.latestRecipes.map { it.recipe.idMeal },
                                                    index,
                                                    latestRecipes.latestRecipes[index].recipe.strMeal
                                                )
                                            },
                                            onToggleFavorite = onToggleFavorite,
                                            onVideoButtonClick = {
                                                onVideoButtonClick((latestRecipes.latestRecipes[index].recipe as Recipe).strYoutube)
                                            }
                                        )
                                    }
                                } else {
                                    SimpleHorizontalRecipesList(
                                        latestRecipes.latestRecipes,
                                        onOpenRecipe = onOpenRecipe,
                                        onToggleFavorite = onToggleFavorite
                                    )
                                }
                            }
                        }
                    }
                }
                item(span = { spanSize }) { Spacer(Modifier.height(16.dp)) }
                item(key = "banner_top", contentType = "ad", span = { spanSize }) {
                    BannerAd(
                        placement = BannerPlacement.HOME_POS_1,
                        provider = localBannerProvider,
                        horizontalPadding = 16.dp
                    )
                }
                item(
                    key = "american_section",
                    contentType = "section", span = { spanSize }) {
                    when (americanRecipes) {
                        AmericanRecipes.Error -> ErrorScreen { }
                        AmericanRecipes.Loading -> {}
                        is AmericanRecipes.Success -> {
                            HorizontalRecipesList(
                                "American",
                                americanRecipes.americanRecipes,
                                onOpenRecipe = onOpenRecipe,
                                onOpenSection = onOpenSection,
                                onToggleFavorite = onToggleFavorite
                            )
                        }
                    }
                }

                when (areasRecipes) {
                    AreasRecipes.Error -> item { ErrorScreen { } }
                    AreasRecipes.Loading -> item { }
                    is AreasRecipes.Success -> {
                        areaSections.forEach { (key, list) ->
                            item(
                                key = "area_section_$key",
                                contentType = "section", span = { spanSize }) {
                                HorizontalRecipesList(
                                    key,
                                    list,
                                    onOpenRecipe = onOpenRecipe,
                                    onOpenSection = { onOpenSection(key) },
                                    onToggleFavorite = onToggleFavorite
                                )
                            }
                        }
                    }
                }
                item(
                    key = "banner_mid",
                    contentType = "ad", span = { spanSize }) {
                    BannerAd(
                        placement = BannerPlacement.HOME_POS_2,
                        provider = localBannerProvider,
                        horizontalPadding = 16.dp
                    )
                }
                item(span = { spanSize }) { Spacer(Modifier.height(12.dp)) }
                item(span = { spanSize }) {
                    Text(
                        text = "English recipes",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                when (englishRecipes) {
                    EnglishRecipes.Error -> item { ErrorScreen { } }
                    EnglishRecipes.Loading -> item { }
                    is EnglishRecipes.Success -> {
                        items(
                            items = englishRecipes.englishRecipes,
                            key = { it.recipe.idMeal },
                            contentType = { "recipe_big" }
                        ) { recipe ->
                            BigRecipeItem(
                                recipe,
                                onToggleFavorite = onToggleFavorite,
                                onOpenRecipe = {
                                    onOpenRecipe(
                                        englishRecipes.englishRecipes.map { it.recipe.idMeal },
                                        englishRecipes.englishRecipes.indexOf(recipe),
                                        recipe.recipe.strMeal
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
