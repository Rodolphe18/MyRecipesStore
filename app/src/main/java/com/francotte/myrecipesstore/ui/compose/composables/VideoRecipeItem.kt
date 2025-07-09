package com.francotte.myrecipesstore.ui.compose.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.francotte.myrecipesstore.R
import com.francotte.myrecipesstore.domain.model.LikeableRecipe
import com.francotte.myrecipesstore.util.imageRequestBuilder

@Composable
fun VideoRecipeItem(
    modifier: Modifier = Modifier,
    likeableRecipe: LikeableRecipe,
    onToggleFavorite: (LikeableRecipe, Boolean) -> Unit,
    onOpenRecipe: () -> Unit,
    onVideoButtonClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(horizontal = 16.dp)
            .height(360.dp)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onOpenRecipe() }) {
        Box {
            Image(
                painter =
                    rememberAsyncImagePainter(
                        model = imageRequestBuilder(
                            LocalContext.current,
                            likeableRecipe.recipe.strMealThumb
                        )
                    ),
                contentDescription = likeableRecipe.recipe.strMeal,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
            )
            Text(
                text = likeableRecipe.recipe.strMeal,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                fontSize = 22.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 2,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopStart)
            )
            Image(
                painter = painterResource(id = R.drawable.ic_card_play),
                contentDescription = null,
                modifier = Modifier
                    .size(60.dp)
                    .align(Alignment.Center)
                    .clickable { onVideoButtonClick() }
            )
            FavButton(
                modifier = Modifier
                    .padding(12.dp)
                    .align(Alignment.BottomEnd),
                buttonSize = 55.dp,
                iconSize = 30.dp,
                onToggleFavorite = { checked -> onToggleFavorite(likeableRecipe, checked) },
                isFavorite = likeableRecipe.isFavorite
            )
        }
    }
}