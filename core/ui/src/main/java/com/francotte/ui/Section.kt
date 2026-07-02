package com.francotte.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.francotte.model.LikeableRecipe

@Immutable
data class LikeableRecipesWrapper(val recipes: List<LikeableRecipe>)

@Composable
fun HorizontalRecipesList(
    title: String,
    likeableRecipesWrapper:LikeableRecipesWrapper,
    onOpenRecipe: (Int) -> Unit,
    onOpenSection: (String) -> Unit,
    onToggleFavorite: (LikeableRecipe) -> Unit,
) {
    val listState = rememberLazyListState()
    TrackScrollJank(scrollableState = listState, stateName = "section:row:list")
    Column(modifier = Modifier.padding(top = 10.dp)) {
        if (likeableRecipesWrapper.recipes.isNotEmpty()) {
            SectionTitle(
                modifier =
                    Modifier.testTag("SectionTitle_$title").semantics {
                        contentDescription = "SectionTitle_$title"
                    },
                title = title,
                count = likeableRecipesWrapper.recipes.size,
                onOpenMore = onOpenSection,
            )
            LazyRow(
                state = listState,
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                itemsIndexed(
                    items = likeableRecipesWrapper.recipes,
                    key = { index, likeableRecipe -> likeableRecipe.recipe.idMeal },
                ) { index, likeableRecipe ->
                    HorizontalRecipeItem(
                        likeableRecipe = likeableRecipe,
                        onOpenRecipe = { onOpenRecipe(index) },
                        onToggleFavorite = onToggleFavorite,
                    )
                }
            }
        }
    }
}

@Composable
fun SimpleHorizontalRecipesList(
    likeableRecipesWrapper:LikeableRecipesWrapper,
    onOpenRecipe: (Int) -> Unit,
    onToggleFavorite: (LikeableRecipe) -> Unit,
) {
    val listState = rememberLazyListState()
    Column(modifier = Modifier.padding(top = 10.dp)) {
        if (likeableRecipesWrapper.recipes.isNotEmpty()) {
            LazyRow(
                state = listState,
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                itemsIndexed(
                    items = likeableRecipesWrapper.recipes,
                    key = { index, likeableRecipe -> likeableRecipe.recipe.idMeal },
                ) { index, likeableRecipe ->
                    HorizontalRecipeItem(
                        likeableRecipe = likeableRecipe,
                        onOpenRecipe = { onOpenRecipe(index) },
                        onToggleFavorite = onToggleFavorite
                    )
                }
            }
        }
    }
}
