package com.francotte.section

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.francotte.designsystem.component.AdMobBanner
import com.francotte.designsystem.component.CustomCircularProgressIndicator
import com.francotte.designsystem.component.LazyGridWithBanners
import com.francotte.designsystem.component.TopAppBar
import com.francotte.designsystem.component.nbSectionColumns
import com.francotte.model.LikeableRecipe
import com.francotte.ui.ErrorScreen
import com.francotte.ui.RecipeItem

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectionScreen(
    sectionUiState: SectionUiState,
    windowSizeClass: WindowSizeClass,
    titleRes: String,
    onReload: () -> Unit,
    onToggleFavorite: (LikeableRecipe, Boolean) -> Unit,
    onOpenRecipe: (List<String>, Int,String) -> Unit,
    onBack: () -> Unit
) {
    val topAppBarScrollBehavior =
        TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    Scaffold(
        modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = titleRes,
                scrollBehavior = topAppBarScrollBehavior,
                navigationIconEnabled = true,
                onNavigationClick = onBack
            )
        }
    ) { padding ->
        when (sectionUiState) {
            SectionUiState.Loading -> CustomCircularProgressIndicator()
            SectionUiState.Error -> ErrorScreen { onReload() }
            is SectionUiState.Success -> {
                val gridState = rememberLazyGridState()
                val likeableRecipes = sectionUiState.recipes
                LazyGridWithBanners(
                    modifier = Modifier
                        .testTag("full_section_screen")
                        .semantics { contentDescription = "full_section_screen" },
                    totalItemCount = likeableRecipes.size,
                    columns = windowSizeClass.widthSizeClass.nbSectionColumns,
                    state = gridState,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    flingBehavior = ScrollableDefaults.flingBehavior(),
                    contentPadding = PaddingValues(
                        top = padding.calculateTopPadding(),
                        bottom = 16.dp,
                        start = 16.dp,
                        end = 16.dp
                    ),
                    bannerContent = {
                        AdMobBanner(height = 100.dp)
                    },
                    itemKey = { index -> likeableRecipes[index].recipe.idMeal },
                    itemContentType = { "recipe" },
                    bannerKey = { bannerIndex -> "section-banner-$bannerIndex" },
                    bannerContentType = "ad",
                ) { index ->
                    val likeableRecipe = likeableRecipes[index]
                    RecipeItem(
                        likeableRecipe = likeableRecipe,
                        onToggleFavorite = onToggleFavorite,
                        onOpenRecipe = {
                            onOpenRecipe(
                                likeableRecipes.map { it.recipe.idMeal },
                                index,
                                likeableRecipe.recipe.strMeal
                            )
                        }
                    )
                }
            }

        }
    }
}
