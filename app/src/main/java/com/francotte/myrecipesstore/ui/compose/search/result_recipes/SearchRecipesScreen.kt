package com.francotte.myrecipesstore.ui.compose.search.result_recipes

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.francotte.myrecipesstore.R
import com.francotte.myrecipesstore.domain.model.LikeableRecipe
import com.francotte.myrecipesstore.ui.compose.composables.CustomCircularProgressIndicator
import com.francotte.myrecipesstore.ui.compose.composables.ErrorScreen
import com.francotte.myrecipesstore.ui.compose.composables.RecipeItem
import com.francotte.myrecipesstore.ui.compose.composables.nbSectionColumns
import com.francotte.myrecipesstore.ui.navigation.TopAppBar
import com.francotte.myrecipesstore.ui.theme.LightYellow

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchRecipesScreen(
    searchRecipesUiState: SearchRecipesUiState,
    windowSizeClass: WindowSizeClass,
    title: String,
    onReload: () -> Unit,
    onOpenRecipe: (List<String>, Int, String) -> Unit,
    onToggleFavorite: (LikeableRecipe, Boolean) -> Unit,
    onBack: () -> Unit
) {
    val topAppBarScrollBehavior =
        TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    Scaffold(
        modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = title,
                navigationIconEnabled = true,
                onNavigationClick = onBack,
                scrollBehavior = topAppBarScrollBehavior
            )
        }
    ) { padding ->
        when (searchRecipesUiState) {
            SearchRecipesUiState.Loading -> CustomCircularProgressIndicator()
            SearchRecipesUiState.Error -> ErrorScreen { onReload() }
            is SearchRecipesUiState.Success -> {
                if (searchRecipesUiState.recipes.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize().background(LightYellow.copy(0.2f)),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.think),
                            contentDescription = null,
                            modifier = Modifier.size(200.dp),
                        )
                        Spacer(Modifier.height(30.dp))
                        Text(
                            text = stringResource(R.string.empty_recipes_screen),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            color = Color.Gray,
                            lineHeight = 35.sp
                        )
                        Spacer(Modifier.height(40.dp))
                        Box(
                            modifier = Modifier
                                .height(60.dp)
                                .width(240.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFFA000))
                                .clickable {
                                   onBack()
                                }, contentAlignment = Alignment.Center
                        ) {
                            Text("Go back", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
                } else {
                    LazyVerticalGrid(
                        state = rememberLazyGridState(),
                        columns = GridCells.Fixed(windowSizeClass.widthSizeClass.nbSectionColumns),
                        reverseLayout = false,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        flingBehavior = ScrollableDefaults.flingBehavior(),
                        contentPadding = PaddingValues(
                            top = padding.calculateTopPadding(),
                            bottom = 16.dp,
                            start = 16.dp,
                            end = 16.dp
                        )
                    ) {
                        val likeableRecipes = searchRecipesUiState.recipes
                        itemsIndexed(
                            items = likeableRecipes,
                            key = { index, likeableRecipe -> likeableRecipe.recipe.idMeal + index }
                        ) { index, likeableRecipe ->
                            RecipeItem(
                                likeableRecipe = likeableRecipe,
                                onToggleFavorite = onToggleFavorite,
                                onOpenRecipe = {
                                    onOpenRecipe(
                                        likeableRecipes.map { it.recipe.idMeal },
                                        index,
                                        likeableRecipe.recipe.strMeal
                                    )
                                })
                        }
                    }
                }

            }
        }
    }
}