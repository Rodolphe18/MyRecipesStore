package com.francotte.myrecipesstore.ui.compose.detail

import android.content.Intent
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier

import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.francotte.myrecipesstore.domain.model.LikeableRecipe
import com.francotte.myrecipesstore.domain.model.Recipe
import com.francotte.myrecipesstore.ui.compose.composables.CustomCircularProgressIndicator
import com.francotte.myrecipesstore.ui.compose.composables.FavButton
import com.francotte.myrecipesstore.ui.navigation.TopAppBar
import com.francotte.myrecipesstore.util.imageRequestBuilder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailRecipeScreen(
    viewModel: DetailRecipeViewModel,
    onToggleFavorite: (LikeableRecipe, Boolean) -> Unit,
    onBackCLick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val topAppBarScrollBehavior =
        TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val title by viewModel.title.collectAsStateWithLifecycle()
    val pageCount = viewModel.pageCount
    val pagerState =
        rememberPagerState(initialPage = viewModel.index ?: 0, pageCount = { pageCount })
    val context = LocalContext.current
    val deepLink by viewModel.deeplinkRecipe.collectAsStateWithLifecycle()
    Scaffold(
        modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = title,
                scrollBehavior = topAppBarScrollBehavior,
                navigationIconEnabled = true,
                onNavigationClick = {
                    scope.launch {
                        while (pagerState.isScrollInProgress) {
                            delay(50)
                        }
                        onBackCLick()
                    }
                }
            )
        }
    ) { padding ->
        if (deepLink != null) {
            deepLink?.also { link ->
                val ingredients = (1..20).mapNotNull { i ->
                    val ingredient =
                        (link.recipe as? Recipe)?.javaClass?.getDeclaredField("strIngredient$i")
                            ?.apply { isAccessible = true }
                            ?.get(link.recipe) as? String
                    val measure =
                        (link.recipe as Recipe).javaClass.getDeclaredField("strMeasure$i")
                            .apply { isAccessible = true }
                            .get(link.recipe) as? String
                    if (!ingredient.isNullOrBlank()) {
                        ingredient to (measure ?: "")
                    } else null
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(
                            top = padding.calculateTopPadding() + 12.dp,
                            bottom = 12.dp
                        )
                        .testTag("full_detail_screen")
                        .semantics { contentDescription = "full_detail_screen" }
                ) {
                    val youtubeUrl = (link.recipe as Recipe).strYoutube
                    val videoId = youtubeUrl.substringAfter("v=").substringBefore("&")
                    if (videoId.isNotBlank()) {
                        val embedUrl = "https://www.youtube.com/embed/$videoId?autoplay=1&mute=1"
                        var webView: WebView? = null

                        DisposableEffect(videoId) {
                            onDispose {
                                webView?.apply {
                                    stopLoading()
                                    loadUrl("about:blank")
                                    removeAllViews()
                                    destroy()
                                }
                                webView = null
                            }
                        }
                        AndroidView(
                            factory = { context ->
                                WebView(context).apply {
                                    layoutParams = ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        220
                                    )
                                    webViewClient = WebViewClient()
                                    settings.javaScriptEnabled = true
                                    loadUrl(embedUrl)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            update = {
                                it.loadUrl(embedUrl)
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(Modifier.padding(horizontal = 12.dp)) {
                        Row {
                            Text(
                                text = link.recipe.strMeal,
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .padding(bottom = 8.dp)
                                    .weight(1f)
                            )
                            FavButton(
                                modifier = Modifier.padding(8.dp),
                                onToggleFavorite = { checked ->
                                    onToggleFavorite(
                                        link,
                                        checked
                                    )
                                },
                                isFavorite = link.isFavorite
                            )
                        }

                        Text(
                            text = "Ingrédients",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 8.dp),
                            color = MaterialTheme.colorScheme.secondary,
                        )

                        ingredients.forEach { (ingredient, measure) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = ingredient,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.secondary,
                                )
                                Text(
                                    text = measure,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.secondary,
                                )
                            }
                        }
                        Button(
                            onClick = {
                                val shoppingListText = buildString {
                                    appendLine("🛒 Liste de courses : ${link.recipe.strMeal}")
                                    appendLine()
                                    ingredients.forEach { (ingredient, measure) ->
                                        appendLine("- $ingredient: $measure")
                                    }
                                }

                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(
                                        Intent.EXTRA_SUBJECT,
                                        "Ma liste de courses pour ${link.recipe.strMeal}"
                                    )
                                    putExtra(Intent.EXTRA_TEXT, shoppingListText)
                                }

                                context.startActivity(
                                    Intent.createChooser(
                                        shareIntent,
                                        "Partager la liste de courses via"
                                    )
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Partager",
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text("Partager la liste de courses")
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Instructions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 8.dp),
                            color = MaterialTheme.colorScheme.secondary,
                        )

                        Text(
                            text = link.recipe.strInstructions.orEmpty(),
                            style = MaterialTheme.typography.bodyLarge,
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                }
            }
        } else {
            LaunchedEffect(pagerState) {
                snapshotFlow { pagerState.settledPage}.collect { newPage ->
                    viewModel.currentPage = newPage
                    viewModel.getRecipes()
                }
            }
            HorizontalPager(
                state = pagerState,
                beyondViewportPageCount = 1,
                modifier = Modifier.fillMaxSize()
            ) { index ->
                viewModel.recipe.getOrDefault(index, null)?.let { likeableRecipe ->
                    val ingredients = (1..20).mapNotNull { i ->
                        val ingredient =
                            (likeableRecipe.recipe as? Recipe)?.javaClass?.getDeclaredField(
                                "strIngredient$i"
                            )
                                ?.apply { isAccessible = true }
                                ?.get(likeableRecipe.recipe) as? String
                        val measure =
                            (likeableRecipe.recipe as Recipe).javaClass.getDeclaredField("strMeasure$i")
                                .apply { isAccessible = true }
                                .get(likeableRecipe.recipe) as? String
                        if (!ingredient.isNullOrBlank()) {
                            ingredient to (measure ?: "")
                        } else null
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(
                                top = padding.calculateTopPadding() + 12.dp,
                                bottom = 12.dp
                            )
                    ) {
                        val youtubeUrl = (likeableRecipe.recipe as Recipe).strYoutube
                        val videoId = youtubeUrl.substringAfter("v=").substringBefore("&")

                        if (videoId.isNotBlank()) {
                            val embedUrl =
                                "https://www.youtube.com/embed/$videoId?autoplay=1&mute=1"
                            AndroidView(
                                factory = { context ->

                                    WebView(context).apply {
                                        layoutParams = ViewGroup.LayoutParams(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            220
                                        )
                                        webViewClient = WebViewClient()
                                        settings.javaScriptEnabled = true
                                        loadUrl(embedUrl)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                            )
                        } else {
                            Image(
                                painter = rememberAsyncImagePainter(model = imageRequestBuilder(LocalContext.current, likeableRecipe.recipe.strMealThumb)),
                                contentDescription = "Image de ${likeableRecipe.recipe.strMeal}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Column(Modifier.padding(horizontal = 12.dp)) {
                            Row {
                                Text(
                                    text = likeableRecipe.recipe.strMeal,
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .padding(bottom = 8.dp)
                                        .weight(1f)
                                )
                                FavButton(
                                    modifier = Modifier.padding(8.dp),
                                    onToggleFavorite = { checked ->
                                        onToggleFavorite(
                                            likeableRecipe,
                                            checked
                                        )
                                    },
                                    isFavorite = likeableRecipe.isFavorite
                                )
                            }

                            Text(
                                text = "Ingrédients",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 8.dp),
                                color = MaterialTheme.colorScheme.secondary,
                            )

                            ingredients.forEach { (ingredient, measure) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = ingredient,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.secondary,
                                    )
                                    Text(
                                        text = measure,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.secondary,
                                    )
                                }
                            }
                            Button(
                                onClick = {
                                    val shoppingListText = buildString {
                                        appendLine("🛒 Liste de courses : ${likeableRecipe.recipe.strMeal}")
                                        appendLine()
                                        ingredients.forEach { (ingredient, measure) ->
                                            appendLine("- $ingredient: $measure")
                                        }
                                    }

                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(
                                            Intent.EXTRA_SUBJECT,
                                            "Ma liste de courses pour ${likeableRecipe.recipe.strMeal}"
                                        )
                                        putExtra(Intent.EXTRA_TEXT, shoppingListText)
                                    }

                                    context.startActivity(
                                        Intent.createChooser(
                                            shareIntent,
                                            "Partager la liste de courses via"
                                        )
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Partager",
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text("Partager la liste de courses")
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = "Instructions",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 8.dp),
                                color = MaterialTheme.colorScheme.secondary,
                            )

                            Text(
                                text = likeableRecipe.recipe.strInstructions.orEmpty(),
                                style = MaterialTheme.typography.bodyLarge,
                                lineHeight = 22.sp,
                                color = MaterialTheme.colorScheme.secondary,
                            )
                        }
                    }
                }
            }
        }
    }
}

