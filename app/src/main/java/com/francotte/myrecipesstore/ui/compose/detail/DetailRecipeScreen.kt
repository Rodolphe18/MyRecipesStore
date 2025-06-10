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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.francotte.myrecipesstore.domain.model.LikeableRecipe
import com.francotte.myrecipesstore.domain.model.Recipe
import com.francotte.myrecipesstore.network.model.NetworkRecipe
import com.francotte.myrecipesstore.ui.compose.composables.CustomCircularProgressIndicator
import com.francotte.myrecipesstore.ui.compose.composables.ErrorScreen
import com.francotte.myrecipesstore.ui.compose.composables.FavButton
import com.francotte.myrecipesstore.ui.navigation.TopAppBar


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailRecipeScreen(uiState: DetailRecipeUiState, onToggleFavorite:(LikeableRecipe, Boolean)->Unit, recipeName:String, onBackCLick:()->Unit) {
    val topAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    Scaffold(
        modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = recipeName, scrollBehavior = topAppBarScrollBehavior, navigationIconEnabled = true, onNavigationClick = onBackCLick
            )
        }
    ) { padding ->
    when (uiState) {
        DetailRecipeUiState.Loading -> CustomCircularProgressIndicator()
        DetailRecipeUiState.Error -> ErrorScreen {  }
        is DetailRecipeUiState.Success -> {
            uiState.recipe?.let { likeableRecipe ->
                val ingredients = (1..20).mapNotNull { i ->
                    val ingredient =
                        (likeableRecipe.recipe as? Recipe)?.javaClass?.getDeclaredField("strIngredient$i")
                            ?.apply { isAccessible = true }
                            ?.get(likeableRecipe.recipe) as? String
                    val measure = (likeableRecipe.recipe as Recipe).javaClass.getDeclaredField("strMeasure$i")
                        .apply { isAccessible = true }.get(likeableRecipe.recipe) as? String
                    if (!ingredient.isNullOrBlank()) {
                        ingredient to (measure ?: "")
                    } else null
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(top = padding.calculateTopPadding(), bottom =12.dp, start = 12.dp, end = 12.dp)
                ) {

                    Image(
                        painter = rememberAsyncImagePainter(model = likeableRecipe.recipe.strMealThumb),
                        contentDescription = "Image de ${likeableRecipe.recipe.strMeal}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row {
                        Text(
                            text = likeableRecipe.recipe.strMeal,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp).weight(1f)
                        )
                        FavButton(
                            modifier = Modifier.padding(8.dp),
                            onToggleFavorite = { checked -> onToggleFavorite(likeableRecipe, checked) },
                            isFavorite = likeableRecipe.isFavorite
                        )
                    }

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
                            Text(
                                text = measure,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
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
                        text = (likeableRecipe.recipe as Recipe).strInstructions.orEmpty(),
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 22.sp
                    )
                }
            }
        }
        }
    }


}