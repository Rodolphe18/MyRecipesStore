package com.francotte.myrecipesstore.ui.compose.user_recipes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.francotte.myrecipesstore.network.model.CustomRecipe
import com.francotte.myrecipesstore.ui.compose.composables.CustomRecipeItem
import com.francotte.myrecipesstore.ui.compose.composables.SectionTitle

@Composable
fun UserRecipesScreen(recipes: List<CustomRecipe>?) {
    Column {
        SectionTitle(title = "My recipes", count = recipes?.size, paddingStart = 4.dp)
        Spacer(Modifier.height(8.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            recipes?.let {
                items(it) { recipe ->
                    CustomRecipeItem(recipe, {})
            }
        }
    }
}
}
