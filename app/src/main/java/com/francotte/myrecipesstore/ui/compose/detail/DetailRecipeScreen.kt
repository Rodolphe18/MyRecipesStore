package com.francotte.myrecipesstore.ui.compose.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.francotte.myrecipesstore.model.Recipe
import com.francotte.myrecipesstore.model.RecipeResult



@Composable
fun DetailRecipeScreen(meal: RecipeResult?) {
    when (meal) {
        is RecipeResult.Single -> {
            (meal.meal as? Recipe)?.let { recipe ->
                val ingredients = (1..20).mapNotNull { i ->
                    val ingredient = meal.javaClass.getDeclaredField("strIngredient$i").apply { isAccessible = true }.get(meal) as? String
                    val measure = meal.javaClass.getDeclaredField("strMeasure$i").apply { isAccessible = true }.get(meal) as? String
                    if (!ingredient.isNullOrBlank()) {
                        ingredient to (measure ?: "")
                    } else null
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(12.dp)
                ) {
                    Spacer(Modifier.height(78.dp))
                    Text(
                        text = recipe.strMeal,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Image(
                        painter = rememberAsyncImagePainter(model = recipe.strMealThumb),
                        contentDescription = "Image de ${recipe.strMeal}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Ingrédients",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    ingredients.forEach { (ingredient, measure) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = ingredient, style = MaterialTheme.typography.bodyLarge)
                            Text(text = measure, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Instructions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = recipe.strInstructions.orEmpty(),
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 22.sp
                    )
                }
            }

        }
        else -> {}
    }


}