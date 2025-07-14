package com.francotte.myrecipesstore.ui.compose.categories

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.francotte.myrecipesstore.domain.model.AbstractCategory
import com.francotte.myrecipesstore.domain.model.Category
import com.francotte.myrecipesstore.network.model.NetworkCategory
import com.francotte.myrecipesstore.ui.compose.composables.CustomCircularProgressIndicator
import com.francotte.myrecipesstore.ui.compose.composables.ErrorScreen
import com.francotte.myrecipesstore.ui.compose.composables.nbCategoriesColumns
import com.francotte.myrecipesstore.ui.compose.composables.nbHomeColumns
import com.francotte.myrecipesstore.ui.compose.composables.whiteYellowVerticalGradient
import com.francotte.myrecipesstore.ui.theme.LightYellow
import com.francotte.myrecipesstore.ui.theme.Orange
import com.francotte.myrecipesstore.util.imageRequestBuilder

@Composable
fun CategoriesScreen(
    categoryUiState: CategoriesUiState,
    windowSizeClass: WindowSizeClass,
    onReload: () -> Unit,
    onOpenCategory: (AbstractCategory) -> Unit
) {
    val lazyListState = rememberLazyGridState()
    when (categoryUiState) {
        CategoriesUiState.Loading -> CustomCircularProgressIndicator()
        CategoriesUiState.Error -> ErrorScreen { onReload() }
        is CategoriesUiState.Success -> {
            (categoryUiState.categories as? List<AbstractCategory>)?.let { categories ->
                LazyVerticalGrid(
                    modifier = Modifier.drawBehind {
                        drawRect(LightYellow.copy(0.1f))
                    },
                    state = lazyListState,
                    columns = GridCells.Fixed(windowSizeClass.widthSizeClass.nbCategoriesColumns),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(20.dp)
                ) {
                    items(items = categories) { category ->
                        CategoryItem(
                            imageUrl = (category as Category).strCategoryThumb,
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
                .height(130.dp)
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
        painter = rememberAsyncImagePainter(
            imageRequestBuilder(LocalContext.current, imageUrl)))
}

@Composable
fun CategoryMetaData(modifier: Modifier= Modifier, title: String) {
    Text(modifier = modifier, text = title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
}

@Composable
fun GradientBackGround(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize()) {
        Box(
            modifier
                .fillMaxSize()
                .background(
                    color = Color.LightGray.copy(alpha = 0.1f)
                )
        )
    }
}

