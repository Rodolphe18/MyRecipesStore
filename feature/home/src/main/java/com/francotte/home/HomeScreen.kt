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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.francotte.designsystem.component.CustomCircularProgressIndicator
import com.francotte.designsystem.component.nbHomeColumns
import com.francotte.model.LikeableRecipe
import com.francotte.model.Recipe
import com.francotte.ui.BigRecipeItem
import com.francotte.ui.ErrorScreen
import com.francotte.ui.HorizontalRecipesList
import com.francotte.ui.SectionTitle
import com.francotte.ui.SimpleHorizontalRecipesList
import com.francotte.ui.VideoRecipeItem
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    windowSizeClass: WindowSizeClass,
    latestRecipes: LatestRecipes,
    americanRecipes: AmericanRecipes,
    areasRecipes: AreasRecipes,
    englishRecipes: EnglishRecipes,
    onOpenRecipe: (List<String>, Int, String) -> Unit,
    onToggleFavorite: (LikeableRecipe, Boolean) -> Unit,
    onOpenSection: (String) -> Unit,
    onVideoButtonClick: (String) -> Unit
) {
    val spanSize = GridItemSpan(windowSizeClass.widthSizeClass.nbHomeColumns)
    val scrollState = rememberLazyGridState()
    val pullRefreshState = rememberPullToRefreshState()
    val coroutineScope = rememberCoroutineScope()
    val isDataReady =
        latestRecipes is LatestRecipes.Success && americanRecipes is AmericanRecipes.Success
    PullToRefreshBox(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (isDataReady)
                    Modifier
                        .testTag("homeScreenReady")
                        .semantics { contentDescription = "homeScreenReady" } else Modifier
            ),
        isRefreshing = viewModel.isReloading,
        onRefresh = {
            coroutineScope.launch {
                viewModel.reload()
            }
        },
        state = pullRefreshState
    ) {
        if (viewModel.isReloading) {
            CustomCircularProgressIndicator()
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
                item(span = { spanSize }) {
                    // Latest Recipes Section
                    Column {
                        SectionTitle(
                            title = stringResource(R.string.latest_recipes_title),
                            count = null,
                            showNavIcon = false
                        )
                        when (latestRecipes) {
                            LatestRecipes.Error -> ErrorScreen { viewModel.reload() }
                            LatestRecipes.Loading -> {}
                            is LatestRecipes.Success -> {
                                if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact) {
                                    val pagerState = rememberPagerState(
                                        initialPage = 0,
                                        pageCount = { latestRecipes.latestRecipes.size }
                                    )
                                    LaunchedEffect(pagerState) {
                                        snapshotFlow { pagerState.currentPage }.collect { newPage ->
                                            viewModel.currentPage = newPage
                                        }
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

                //   item(span = { spanSize }) { Spacer(Modifier.height(32.dp)) }
                //   item(span = { spanSize }) { AdMobBanner(height = 50.dp) }
                item(span = { spanSize }) {
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
                        areasRecipes.areasRecipes.forEach { (key, list) ->
                            item(span = { spanSize }) {
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

                item(span = { spanSize }) {
                    Column(Modifier.padding(horizontal = 16.dp)) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "English recipes",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(vertical = 10.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                when (englishRecipes) {
                    EnglishRecipes.Error -> item { ErrorScreen { } }
                    EnglishRecipes.Loading -> item { }
                    is EnglishRecipes.Success -> {
                        items(englishRecipes.englishRecipes.size) { index ->
                            val recipe = englishRecipes.englishRecipes[index]
                            BigRecipeItem(
                                recipe,
                                onToggleFavorite = onToggleFavorite,
                                onOpenRecipe = {
                                    onOpenRecipe(
                                        englishRecipes.englishRecipes.map { it.recipe.idMeal },
                                        index,
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
