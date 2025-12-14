package com.francotte.categories

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.francotte.common.imageRequestBuilder
import com.francotte.designsystem.component.AdMobBanner
import com.francotte.designsystem.component.CustomCircularProgressIndicator
import com.francotte.designsystem.component.nbCategoriesColumns
import com.francotte.model.AbstractCategory
import com.francotte.model.Category
import com.francotte.ui.ErrorScreen

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
            categoryUiState.categories.let { categories ->
                LazyVerticalGrid(
                    state = lazyListState,
                    columns = GridCells.Fixed(windowSizeClass.widthSizeClass.nbCategoriesColumns),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(20.dp)
                ) {
                    item(key = "banner_top", contentType = "ad", span = { GridItemSpan(2) }) {
                        AdMobBanner(
                            height = 50.dp
                        )
                    }
                    item(span = { GridItemSpan(2) }) { Spacer(Modifier.height(4.dp)) }
                    items(
                        key = { it.strCategory },
                        contentType = { "categories" },
                        items = categories
                    ) { category ->
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
                .aspectRatio(1.8f)
                .clip(RoundedCornerShape(20.dp))
                .clickable { onClick() }
                .animateContentSize()) {
            GradientBackGround(Modifier.fillMaxSize())
            CategoryImage(modifier = Modifier.fillMaxSize(), imageUrl = imageUrl)

        }
        Spacer(Modifier.height(2.dp))
        CategoryMetaData(modifier = Modifier.align(Alignment.CenterHorizontally), title = title)
    }

}

@Composable
fun CategoryImage(modifier: Modifier = Modifier, imageUrl: String) {
    Image(
        modifier = modifier,
        contentScale = ContentScale.None,
        contentDescription = "",
        painter = rememberAsyncImagePainter(
            imageRequestBuilder(LocalContext.current, imageUrl)
        )
    )
}

@Composable
fun CategoryMetaData(modifier: Modifier = Modifier, title: String) {
    Text(
        modifier = modifier,
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
    )
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

