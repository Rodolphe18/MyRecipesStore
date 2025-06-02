package com.francotte.myrecipesstore.ui.compose.categories

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.francotte.myrecipesstore.model.Category
import com.francotte.myrecipesstore.ui.compose.composables.CustomCircularProgressIndicator
import com.francotte.myrecipesstore.ui.compose.composables.ErrorScreen

@Composable
fun CategoriesScreen(
    categoryUiState: CategoriesUiState,
    onReload: () -> Unit,
    onOpenCategory: (Category) -> Unit
) {
    val lazyListState = rememberLazyGridState()
    when (categoryUiState) {
        CategoriesUiState.Loading -> CustomCircularProgressIndicator()
        CategoriesUiState.Error -> ErrorScreen { onReload() }
        is CategoriesUiState.Success -> {
            (categoryUiState.categories.categories as? List<Category>)?.let { categories ->
                LazyVerticalGrid(
                    state = lazyListState,
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(20.dp)
                ) {
                    items(items = categories) { category ->
                        CategoryItem(
                            imageUrl = category.strCategoryThumb,
                            title = category.strCategory
                        ) {
                            onOpenCategory(category)
                        }
                    }
                }

            }

        }
    }
}


@Composable
fun CategoryItem(imageUrl: String, title: String, onClick: () -> Unit) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(20.dp))
                .clickable { onClick() }) {
            GradientBackGround(Modifier.fillMaxSize())
            CategoryImage(modifier = Modifier.fillMaxSize(), imageUrl = imageUrl)
        }
        Spacer(Modifier.height(4.dp))
        CategoryMetaData(modifier = Modifier.align(Alignment.CenterHorizontally), title = title)
    }

}

@Composable
fun CategoryImage(modifier: Modifier = Modifier, imageUrl: String) {
    Image(
        modifier = modifier,
        contentScale = ContentScale.FillBounds,
        contentDescription = "",
        painter = rememberAsyncImagePainter(model = imageUrl),
    )
}

@Composable
fun CategoryMetaData(modifier: Modifier= Modifier, title: String) {
    Text(modifier = modifier, text = title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
}

@Composable
fun GradientBackGround(modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        val aspectRatio = maxWidth / maxHeight
        Box(
            modifier
                .fillMaxSize()
                .scale(maxOf(aspectRatio, 1f), maxOf(1 / aspectRatio, 1f))
                .background(
                    brush = Brush.radialGradient(
                        colorStops =
                            arrayOf(
                                0f to Color.LightGray,
                                0.35f to Color.LightGray,
                                0.8f to Color.LightGray),
                    )
                )
        )
    }
}

