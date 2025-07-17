package com.francotte.myrecipesstore.ui.compose.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.francotte.myrecipesstore.R
import com.francotte.myrecipesstore.network.model.CustomRecipe
import com.francotte.myrecipesstore.ui.compose.composables.CustomRecipeItem
import com.francotte.myrecipesstore.ui.compose.composables.SectionTitle

@Composable
fun CustomRecipesSection(
    modifier: Modifier=Modifier,
    recipes: List<CustomRecipe>?,
    onOpenRecipe: (String) -> Unit,
) {
    Column(modifier = modifier) {
        SectionTitle(
            title = stringResource(R.string.my_recipes),
            count = recipes?.size,
            showNavIcon = false,
            paddingStart = 16.dp
        )
        Spacer(Modifier.height(8.dp))
        LazyRow(
            state = rememberLazyListState(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            recipes?.let { customRecipes ->
                items(
                    items = customRecipes,
                    key = { it.id }) {customRecipe ->
                    CustomRecipeItem(
                        customRecipe = customRecipe,
                        onOpenRecipe = { onOpenRecipe(customRecipe.id) },
                    )
                }
            }



        }
    }
}
