package com.francotte.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.francotte.common.imageRequestBuilder
import com.francotte.model.LikeableRecipe
import com.francotte.network.model.CustomRecipe

@Composable
fun RecipeItem(
    likeableRecipe: LikeableRecipe,
    onToggleFavorite: (LikeableRecipe, Boolean) -> Unit,
    onOpenRecipe: () -> Unit,
) {
    val context = LocalContext.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .height(175.dp)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { onOpenRecipe() }
        ) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = imageRequestBuilder(context, likeableRecipe.recipe.strMealThumb)
                ),
                contentDescription = likeableRecipe.recipe.strMeal,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
            )
            FavButton(
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.BottomEnd),
                onToggleFavorite = { checked -> onToggleFavorite(likeableRecipe, checked) },
                isFavorite = likeableRecipe.isFavorite
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = likeableRecipe.recipe.strMeal,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center,
            maxLines = 2,
            modifier = Modifier.width(180.dp)
        )
    }
}

@Composable
fun CustomRecipeItem(
    customRecipe: CustomRecipe,
    onOpenRecipe: () -> Unit,
) {
    val image = customRecipe.imageUrl
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .height(175.dp)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { onOpenRecipe() }
        ) {
            if (image != null) {
                Image(
                    painter = rememberAsyncImagePainter(model = image),
                    contentDescription = image,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                )
            } else {
                Image(imageVector = Icons.Default.Photo, contentDescription = null, modifier = Modifier.fillMaxSize())
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = customRecipe.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}


@Composable
fun BigRecipeItem(
    likeableRecipe: LikeableRecipe,
    onToggleFavorite: (LikeableRecipe, Boolean) -> Unit,
    onOpenRecipe: () -> Unit,
) {
    Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16 / 9f)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { onOpenRecipe() }
        ) {
            Image(
                painter = rememberAsyncImagePainter(
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
            FavButton(
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.BottomEnd),
                onToggleFavorite = { checked -> onToggleFavorite(likeableRecipe, checked) },
                isFavorite = likeableRecipe.isFavorite
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = likeableRecipe.recipe.strMeal,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Left,
            maxLines = 2
        )
    }
}

