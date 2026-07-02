package com.francotte.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.francotte.designsystem.component.DesignAsyncImage
import com.francotte.designsystem.theme.Orange
import com.francotte.model.AbstractRecipe
import com.francotte.model.LikeableRecipe


@Immutable
data class RecipesWrapper(val recipes: Map<Int, LikeableRecipe>)

@Composable
internal fun RecipeListPane(
    recipesWrapper: RecipesWrapper,
    count: Int,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    topPadding: Dp
) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(top = topPadding, bottom = 12.dp)) {
        items(count) { index ->
            RecipeListItem(
                recipe = recipesWrapper.recipes[index]?.recipe,
                selected = index == selectedIndex,
                onClick = { onSelect(index) },
            )
        }
    }
}

@Composable
private fun RecipeListItem(
    recipe: AbstractRecipe?,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val background =
        if (selected) Orange.copy(alpha = 0.12f)
        else MaterialTheme.colorScheme.surface
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(background)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DesignAsyncImage(
            model = recipe?.strMealThumb,
            contentDescription = recipe?.strMeal,
            width = 72.dp,
            height = 72.dp,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(8.dp)),
        )
        Text(
            text = recipe?.strMeal.orEmpty(),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(start = 12.dp)
                .weight(1f),
        )
    }
}
