package com.francotte.myrecipesstore.ui.compose.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.francotte.myrecipesstore.domain.model.LikeableRecipe
import com.francotte.myrecipesstore.domain.model.Recipe
import com.francotte.myrecipesstore.ui.compose.composables.BigRecipeItem
import com.francotte.myrecipesstore.ui.compose.composables.CustomCircularProgressIndicator
import com.francotte.myrecipesstore.ui.compose.composables.ErrorScreen
import com.francotte.myrecipesstore.ui.compose.composables.HorizontalRecipesList
import com.francotte.myrecipesstore.ui.compose.composables.SectionTitle
import com.francotte.myrecipesstore.ui.compose.composables.VideoRecipeItem


@Composable
fun HomeScreen(
    viewModel: HomeViewModel= hiltViewModel(),
    homeUiState: HomeUiState,
    onReload: () -> Unit,
    onOpenRecipe: (List<String>,Int,String) -> Unit,
    onToggleFavorite: (LikeableRecipe, Boolean) -> Unit,
    onOpenSection: (String) -> Unit,
    onVideoButtonClick:(String)->Unit
) {

    when (homeUiState) {
        HomeUiState.Loading -> CustomCircularProgressIndicator()
        HomeUiState.Error -> ErrorScreen { onReload() }
        is HomeUiState.Success -> {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                val lastRecipes = homeUiState.latestRecipes
                val pagerState = rememberPagerState(initialPage = 0, pageCount = { lastRecipes.size })
                Column(modifier = Modifier.padding(top = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    if (lastRecipes.isNotEmpty()) {
                        LaunchedEffect(pagerState) {
                            snapshotFlow { pagerState.currentPage }.collect { newPage ->
                                viewModel.currentPage = newPage
                            }
                        }
                        SectionTitle("LatestRecipes", lastRecipes.size, onOpenSection)
                        HorizontalPager(state = pagerState) { index ->
                            VideoRecipeItem(
                                likeableRecipe = lastRecipes[index],
                                onOpenRecipe = { onOpenRecipe(lastRecipes.map { it.recipe.idMeal }, index, lastRecipes[index].recipe.strMeal) },
                                onToggleFavorite = onToggleFavorite,
                                onVideoButtonClick = { onVideoButtonClick((lastRecipes[index].recipe as Recipe).strYoutube) }
                            )
                        }
                    }
                }
                homeUiState.areaSections.forEach { map ->
                    HorizontalRecipesList(
                        map.key,
                        homeUiState.areaSections[map.key]!!,
                        onOpenRecipe = onOpenRecipe,
                        onOpenSection = { onOpenSection(map.key) },
                        onToggleFavorite = onToggleFavorite
                    )
                }
                Column(Modifier.padding(horizontal = 16.dp)) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                    text = "English recipes",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(vertical = 10.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )
                    homeUiState.englishRecipes.forEachIndexed { index, likeableRecipe ->
                        BigRecipeItem(likeableRecipe, onToggleFavorite, { onOpenRecipe(homeUiState.englishRecipes.map { it.recipe.idMeal },index, likeableRecipe.recipe.strMeal) })
                    } }



            }
        }


    }

}
