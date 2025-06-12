package com.francotte.myrecipesstore.ui.compose.user_recipes

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.francotte.myrecipesstore.network.model.CustomRecipe

@Composable
fun UserRecipesScreen(modifier: Modifier=Modifier,recipes: List<CustomRecipe>?) {
    val baseUrl = "http://46.202.170.205:8080/"
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        recipes?.let {
            items(it) { recipe ->
                Card(
                    elevation = CardDefaults.cardElevation(8.dp),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = recipe.title,
                            style = MaterialTheme.typography.titleLarge
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Instructions: ${recipe.instructions}",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Ingrédients :", fontWeight = FontWeight.Bold)
                        recipe.ingredients.forEach {
                            Text("- ${it.quantity} ${it.name}")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (recipe.imageUrls.isNotEmpty()) {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(recipe.imageUrls) { imageRelativeUrl ->
                                    val fullUrl = baseUrl + imageRelativeUrl
                                    Log.d("debug_", fullUrl)
                                    Image(
                                        painter = rememberAsyncImagePainter(fullUrl),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(150.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

    }
}