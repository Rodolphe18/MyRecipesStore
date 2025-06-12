package com.francotte.myrecipesstore.ui.compose.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.francotte.myrecipesstore.domain.model.LikeableRecipe
import com.francotte.myrecipesstore.domain.model.Recipe

@Composable
fun HorizontalRecipesList(
    title: String,
    recipes: List<LikeableRecipe>,
    onOpenRecipe: (List<String>,Int,String) -> Unit,
    onOpenSection: (String) -> Unit,
    onToggleFavorite: (LikeableRecipe, Boolean) -> Unit
) {
    val listState = rememberLazyListState()
    Column(modifier = Modifier.padding(top = 10.dp)) {
        if (recipes.isNotEmpty()) {
            SectionTitle(title, recipes.size, onOpenSection)
            LazyRow(
                state = listState,
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(
                    items = recipes,
                    key = { index, likeableRecipe -> likeableRecipe.recipe.idMeal}) { index, likeableRecipe ->
                    RecipeItem(
                        likeableRecipe = likeableRecipe,
                        onOpenRecipe = { onOpenRecipe(recipes.map { it.recipe.idMeal }, index, likeableRecipe.recipe.strMeal) },
                        onToggleFavorite = onToggleFavorite,
                    )
                }
            }
        }
    }
}

@Composable
fun HorizontalVideoRecipesPager(
    title: String,
    pageCount:Int,
    currentIndex:Int,
    recipes: List<LikeableRecipe>,
    onOpenRecipe: (List<String>,Int,String) -> Unit,
    onOpenSection: (String) -> Unit,
    onToggleFavorite: (LikeableRecipe, Boolean) -> Unit,
    onVideoButtonClick: (String) -> Unit
) {

}