package com.francotte.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Precision
import com.francotte.common.extension.imageRequestBuilder
import com.francotte.designsystem.component.DesignAsyncImage
import com.francotte.model.CustomRecipe
import com.francotte.model.LikeableRecipe

@Composable
fun RecipeItem(
    likeableRecipe: LikeableRecipe,
    onToggleFavorite: (LikeableRecipe) -> Unit,
    onOpenRecipe: () -> Unit,
    size: Dp = 175.dp
) {
    val shape = RoundedCornerShape(16.dp)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier =
                Modifier
                    .size(size)
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.surfaceVariant, shape)
                    .clickable(onClick = onOpenRecipe),
        ) {
            DesignAsyncImage(
                model = likeableRecipe.recipe.strMealThumb,
                contentDescription = likeableRecipe.recipe.strMeal,
                width = size,
                height = size,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            FavButton(
                modifier =
                    Modifier
                        .padding(8.dp)
                        .align(Alignment.BottomEnd),
                onToggleFavorite = { onToggleFavorite(likeableRecipe) },
                syncState = likeableRecipe.favoriteState,
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
            modifier = Modifier.width(180.dp),
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
            modifier =
                Modifier
                    .height(175.dp)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onOpenRecipe() },
        ) {
            if (image != null) {
                DesignAsyncImage(
                    model = image,
                    width = 175.dp,
                    height = 175.dp,
                    contentDescription = image,
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    imageVector = Icons.Default.Photo,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = customRecipe.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center,
            maxLines = 2,
        )
    }
}

@Composable
fun BigRecipeItem(
    likeableRecipe: LikeableRecipe,
    onToggleFavorite: (LikeableRecipe) -> Unit,
    onOpenRecipe: () -> Unit,
) {
    val context = LocalContext.current
    var imageSizePx by remember { mutableStateOf<IntSize?>(null) }

    Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(16 / 9f)
                    .onSizeChanged { imageSizePx = it }
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onOpenRecipe() },
        ) {
            val model = remember(likeableRecipe.recipe.strMealThumb, imageSizePx) {
                val s = imageSizePx
                if (s == null) {
                    // pas encore mesuré : laisse Coil choisir (ou un placeholder)
                    likeableRecipe.recipe.strMealThumb
                } else {
                    ImageRequest.Builder(context)
                        .data(likeableRecipe.recipe.strMealThumb)
                        .size(s.width, s.height)               // <- clé perf
                        .precision(Precision.INEXACT)
                        .crossfade(false)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .build()
                }
            }
            AsyncImage(
                model = model,
                contentDescription = likeableRecipe.recipe.strMeal,
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier
                        .fillMaxSize(),
            )
            FavButton(
                modifier =
                    Modifier
                        .padding(8.dp)
                        .align(Alignment.BottomEnd),
                onToggleFavorite = { onToggleFavorite(likeableRecipe) },
                syncState = likeableRecipe.favoriteState,
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
            maxLines = 2,
        )
    }
}

@Composable
fun HorizontalRecipeItem(
    likeableRecipe: LikeableRecipe,
    onToggleFavorite: (LikeableRecipe) -> Unit,
    onOpenRecipe: () -> Unit,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val shape = RoundedCornerShape(16.dp)

    val mode = rememberDeviceMode()
    val dimension = remember(mode) { horizontalRecipeItemDimension(mode) }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier =
                Modifier
                    .height(dimension.boxHeight)
                    .aspectRatio(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant, shape)
                    .clickable(onClick = onOpenRecipe),
        ) {

            val pxSize = with(density) { dimension.boxHeight.roundToPx() }
            AsyncImage(
                model = imageRequestBuilder(
                    context,
                    likeableRecipe.recipe.strMealThumb,
                    pxSize,
                    pxSize
                ),
                contentDescription = likeableRecipe.recipe.strMeal,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape),
            )
            FavButton(
                modifier =
                    Modifier
                        .padding(8.dp)
                        .align(Alignment.BottomEnd),
                onToggleFavorite = { onToggleFavorite(likeableRecipe) },
                syncState = likeableRecipe.favoriteState
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
            modifier = Modifier.width(dimension.textWidth),
        )
    }
}

data class HorizontalRecipeItemDims(
    val boxHeight: Dp,
    val textWidth: Dp,
)

fun horizontalRecipeItemDimension(mode: DeviceMode): HorizontalRecipeItemDims = when (mode) {
    DeviceMode.PhonePortrait -> HorizontalRecipeItemDims(boxHeight = 180.dp, textWidth = 180.dp)
    DeviceMode.PhoneLandscape -> HorizontalRecipeItemDims(boxHeight = 100.dp, textWidth = 100.dp)
    DeviceMode.TabletPortrait -> HorizontalRecipeItemDims(boxHeight = 200.dp, textWidth = 200.dp)
    DeviceMode.TabletLandscape -> HorizontalRecipeItemDims(boxHeight = 250.dp, textWidth = 250.dp)
}
