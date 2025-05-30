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
import com.francotte.myrecipesstore.model.AbstractRecipe

@Composable
fun HorizontalRecipesList(
    title: String,
    recipes: List<AbstractRecipe>?,
    onOpenRecipe: (AbstractRecipe) -> Unit,
    onOpenSection: (String) -> Unit,
    onToggleFavorite: (AbstractRecipe) -> Unit
) {
    val listState = rememberLazyListState()
    Column(modifier = Modifier.padding(top = 10.dp)) {
        if (recipes?.size != 0) {
            SectionTitle(title, recipes?.size, onOpenSection)
            LazyRow(
                state = listState,
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(
                    items = requireNotNull(recipes?.take(10)),
                    key = { it.idMeal }) { recipe ->
                    RecipeItem(
                        recipe = recipe,
                        onOpenRecipe = onOpenRecipe,
                        onToggleFavorite = onToggleFavorite,
                    )
                }
            }
        }
    }
}