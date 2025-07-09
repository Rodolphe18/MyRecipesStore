package com.francotte.myrecipesstore.ui.compose.composables

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.francotte.myrecipesstore.R
import com.francotte.myrecipesstore.domain.model.LikeableRecipe
import com.francotte.myrecipesstore.network.model.CustomRecipe
import com.francotte.myrecipesstore.ui.theme.Orange
import com.francotte.myrecipesstore.util.imageRequestBuilder

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
                .height(180.dp)
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
    val baseUrl = "http://46.202.170.205:8080/"
    val image = customRecipe.imageUrls.firstOrNull()
    val fullUrl = baseUrl + image
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .height(180.dp)
                .aspectRatio(1.5f)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { onOpenRecipe() }
        ) {
//            if (fullUrl != null) {
//                Image(
//                    painter = rememberAsyncImagePainter(model = image),
//                    contentDescription = image,
//                    contentScale = ContentScale.Crop,
//                    modifier = Modifier
//                        .fillMaxSize()
//                )
//            } else {
            Image(painterResource(R.drawable.istockphoto_1130112004_2048x2048), null)
            //    }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = customRecipe.title,
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
                    model = imageRequestBuilder(LocalContext.current, likeableRecipe.recipe.strMealThumb)
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

@Composable
fun FavButton(
    modifier: Modifier = Modifier,
    isFavorite: Boolean,
    buttonSize: Dp = 45.dp,
    iconSize: Dp = 25.dp,
    onToggleFavorite: (Boolean) -> Unit
) {
    val transition = updateTransition(label = "favorite", targetState = isFavorite)
    val backgroundColor by transition.animateColor(label = "backgroundColor") { isFav -> if (isFav) Orange else MaterialTheme.colorScheme.tertiary }
    val iconColor by transition.animateColor(label = "iconColor") { isFav -> if (isFav) Color.White else MaterialTheme.colorScheme.onTertiary }
    Box(
        modifier = modifier
            .size(buttonSize)
            .background(backgroundColor, CircleShape)
            .clip(CircleShape)
            .toggleable(
                value = isFavorite,
                onValueChange = { onToggleFavorite(it) },
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            )
    ) {
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.Center)
                .size(iconSize),
            tint = iconColor
        )
    }
}