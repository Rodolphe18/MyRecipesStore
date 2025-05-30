package com.francotte.myrecipesstore.ui.compose.categories

import android.annotation.SuppressLint
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.francotte.myrecipesstore.R
import com.francotte.myrecipesstore.model.Category
import com.francotte.myrecipesstore.ui.compose.composables.ErrorScreen
import com.francotte.myrecipesstore.ui.navigation.TopAppBar

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(categoryUiState: CategoriesUiState, onReload:() -> Unit, onOpenCategory:(Category) -> Unit) {
    val lazyListState = rememberLazyListState()
    val topAppBarScrollBehavior =
        TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    Scaffold(
        modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                R.string.categories, Icons.Filled.Search, ""
            )
        }
    ) {  _ ->
        when (categoryUiState) {
            CategoriesUiState.Loading -> Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            CategoriesUiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                ErrorScreen { onReload() }
            }

            is CategoriesUiState.Success -> {
                (categoryUiState.categories?.data as? List<Category>)?.let { categories ->
                    LazyColumn(
                        state = lazyListState,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        item { Spacer(modifier = Modifier.height(40.dp)) }
                        item {
                            Text(
                                modifier = Modifier.padding(start = 6.dp),
                                text = "Catégories",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        items(items = categories) { category ->
                            CategoryItem(
                                imageUrl = category.strCategoryThumb,
                                title = category.strCategoryDescription
                            ) {
                                onOpenCategory(category)
                            }
                        }
                    }

                }

            }
        }
    }

}


@Composable
fun CategoryItem(imageUrl: String, title: String, onClick: () -> Unit) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .height(120.dp)
        .clip(RoundedCornerShape(20.dp))
        .clickable { onClick() }) {
        CategoryImage(modifier = Modifier.fillMaxSize(), imageUrl = imageUrl)
        GradientBackGround(Modifier.fillMaxSize())
        CategoryMetaData(
            modifier = Modifier.align(Alignment.Center),
            title = title
        )
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
fun CategoryMetaData(modifier: Modifier, title: String) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.LightGray)
    }
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
                                0f to Color(0x5E, 0x3C, 0x23).copy(alpha = 0.2f),
                                0.35f to Color(0xFF, 0x82, 0x27).copy(alpha = 0.35f),
                                1f to Color(0x5E, 0x3C, 0x23).copy(alpha = 0.2f),
                            ),
                    )
                ))
    }
}

