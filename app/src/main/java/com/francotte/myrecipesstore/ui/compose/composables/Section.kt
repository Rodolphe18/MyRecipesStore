package com.francotte.myrecipesstore.ui.compose.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.francotte.myrecipesstore.domain.model.LikeableRecipe

@Composable
fun HorizontalRecipesList(
    title: String,
    recipes: List<LikeableRecipe>,
    onOpenRecipe: (LikeableRecipe) -> Unit,
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
                items(
                    items = recipes,
                    key = { it.recipe.idMeal + it.recipe.strMeal }) { recipe ->
                    RecipeItem(
                        likeableRecipe = recipe,
                        onOpenRecipe = onOpenRecipe,
                        onToggleFavorite = onToggleFavorite,
                    )
                }
            }
        }
    }
}