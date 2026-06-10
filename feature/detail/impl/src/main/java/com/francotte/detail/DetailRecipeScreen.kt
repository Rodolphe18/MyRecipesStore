package com.francotte.detail

import android.content.Context
import android.content.Intent
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.francotte.ads.BannerAd
import com.francotte.ads.BannerPlacement
import com.francotte.common.extension.imageRequestBuilder
import com.francotte.designsystem.component.DesignAsyncImage
import com.francotte.designsystem.component.TopAppBar
import com.francotte.designsystem.component.YouTubeWebViewPlayer
import com.francotte.domain.YouTubeUrlParser
import com.francotte.model.LikeableRecipe
import com.francotte.model.Recipe
import com.francotte.ui.FavButton
import com.francotte.ui.LocalBannerProvider
import com.francotte.ui.rememberDeviceMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailRecipeScreen(
    state: DetailState,
    onAction: (DetailAction) -> Unit,
) {
    val onToggleFavorite: (LikeableRecipe) -> Unit = { onAction(DetailAction.OnToggleFavorite(it)) }
    val mode = rememberDeviceMode()
    val localBannerProvider = LocalBannerProvider.current
    val scope = rememberCoroutineScope()
    val topAppBarScrollBehavior =
        TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val title = state.title
    val pageCount = state.pageCount
    val pagerState =
        rememberPagerState(initialPage = state.initialPage, pageCount = { pageCount })
    val context = LocalContext.current
    val deepLink = state.deeplinkRecipe
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
                        onAction(DetailAction.OnBackClick)
                    }
                },
            )
        },
    ) { padding ->
        if (deepLink != null) {
            deepLink?.also { link ->
                val ingredients =
                    remember(link.recipe) {
                        (1..20).mapNotNull { i ->
                            val ingredient =
                                (link.recipe as? Recipe)
                                    ?.javaClass
                                    ?.getDeclaredField("strIngredient$i")
                                    ?.apply { isAccessible = true }
                                    ?.get(link.recipe) as? String
                            val measure =
                                (link.recipe as Recipe)
                                    .javaClass
                                    .getDeclaredField("strMeasure$i")
                                    .apply { isAccessible = true }
                                    .get(link.recipe) as? String
                            if (!ingredient.isNullOrBlank()) {
                                ingredient to (measure ?: "")
                            } else {
                                null
                            }
                        }
                    }
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(
                                top = padding.calculateTopPadding() + 12.dp,
                                bottom = 12.dp,
                            )
                            .testTag("full_detail_screen")
                            .semantics { contentDescription = "full_detail_screen" },
                ) {
                    DetailVideoScreen(link)
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(Modifier.padding(horizontal = 12.dp)) {
                        DetailScreenMainSectionTitle(link, onToggleFavorite)
                        DetailScreenSectionTitle(R.string.ingredients)
                        BannerAd(
                            placement = BannerPlacement.RECIPE_POS_1,
                            provider = localBannerProvider,
                        )
                        IngredientRow(ingredients)
                        DetailRecipeShareRecipeButton(link, ingredients, context)
                        BannerAd(
                            placement = BannerPlacement.RECIPE_POS_2,
                            provider = localBannerProvider,
                        )
                        DetailScreenSectionTitle(R.string.instructions)
                        Text(
                            text = (link.recipe as Recipe).strInstructions.orEmpty(),
                            style = MaterialTheme.typography.bodyLarge,
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                }
            }
        } else {
            LaunchedEffect(pagerState) {
                snapshotFlow { pagerState.settledPage }
                    .distinctUntilChanged()
                    .collectLatest { newPage ->
                        onAction(DetailAction.OnPageChanged(newPage))
                    }
            }
            HorizontalPager(
                state = pagerState,
                beyondViewportPageCount = 1,
                modifier = Modifier.fillMaxSize(),
            ) { index ->
                state.recipes[index]?.let { likeableRecipe ->
                    val ingredients =
                        remember(likeableRecipe) {
                            (1..20).mapNotNull { i ->
                                val ingredient =
                                    (likeableRecipe.recipe as? Recipe)
                                        ?.javaClass
                                        ?.getDeclaredField(
                                            "strIngredient$i",
                                        )?.apply { isAccessible = true }
                                        ?.get(likeableRecipe.recipe) as? String
                                val measure =
                                    (likeableRecipe.recipe as Recipe)
                                        .javaClass
                                        .getDeclaredField("strMeasure$i")
                                        .apply { isAccessible = true }
                                        .get(likeableRecipe.recipe) as? String
                                if (!ingredient.isNullOrBlank()) {
                                    ingredient to (measure ?: "")
                                } else {
                                    null
                                }
                            }
                        }
                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(
                                    top = padding.calculateTopPadding() + 12.dp,
                                    bottom = 12.dp,
                                ),
                    ) {
                        DetailVideoScreen(likeableRecipe)
                        Spacer(modifier = Modifier.height(16.dp))
                        Column {
                            DetailScreenMainSectionTitle(likeableRecipe, onToggleFavorite)
                            BannerAd(
                                horizontalPadding = 12.dp,
                                placement = BannerPlacement.RECIPE_POS_1,
                                provider = localBannerProvider,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            DetailScreenSectionTitle(R.string.ingredients)
                            IngredientRow(ingredients)
                            DetailRecipeShareRecipeButton(likeableRecipe, ingredients, context)
                            BannerAd(
                                horizontalPadding = 12.dp,
                                placement = BannerPlacement.RECIPE_POS_2,
                                provider = localBannerProvider,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            DetailScreenSectionTitle(R.string.instructions)
                            Text(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                text = (likeableRecipe.recipe as Recipe).strInstructions.orEmpty(),
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

@Composable
private fun DetailVideoScreen(likeableRecipe: LikeableRecipe) {
    val youtubeUrl = (likeableRecipe.recipe as Recipe).strYoutube
    val videoId = remember(youtubeUrl) { YouTubeUrlParser.extractVideoId(youtubeUrl) }
    if (videoId.isNotBlank()) {
        YouTubeWebViewPlayer(
            videoId = videoId,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
        )
    } else {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            DesignAsyncImage(
                model = likeableRecipe.recipe.strMealThumb,
                width = maxWidth,
                height = 200.dp,
                contentDescription = "Image de ${likeableRecipe.recipe.strMeal}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth(),
            )
        }

    }
}

@Composable
private fun DetailRecipeShareRecipeButton(
    likeableRecipe: LikeableRecipe,
    ingredients: List<Pair<String, String>>,
    context: Context,
) {
    Button(
        onClick = {
            val shoppingListText =
                buildString {
                    appendLine("🛒 Groceries list : ${likeableRecipe.recipe.strMeal}")
                    appendLine()
                    ingredients.forEach { (ingredient, measure) ->
                        appendLine("- $ingredient: $measure")
                    }
                }

            val shareIntent =
                Intent(Intent.ACTION_SEND).apply {
                    setType("text/plain")
                    putExtra(
                        Intent.EXTRA_SUBJECT,
                        "My groceries list for ${likeableRecipe.recipe.strMeal}",
                    )
                    putExtra(Intent.EXTRA_TEXT, shoppingListText)
                }

            context.startActivity(
                Intent.createChooser(
                    shareIntent,
                    "Share the groceries list with",
                ),
            )
        },
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 16.dp),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        shape = RoundedCornerShape(8.dp),
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_share),
            contentDescription = "Share",
            modifier = Modifier.padding(end = 8.dp),
        )
        Text("Share the groceries list")
    }
}

@Composable
fun DetailScreenMainSectionTitle(
    likeableRecipe: LikeableRecipe,
    onToggleFavorite: (LikeableRecipe) -> Unit,
) {
    Row(Modifier.padding(horizontal = 12.dp)) {
        Text(
            text = likeableRecipe.recipe.strMeal,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Bold,
            modifier =
                Modifier
                    .padding(bottom = 8.dp)
                    .weight(1f),
        )
        FavButton(
            modifier = Modifier.padding(8.dp),
            onToggleFavorite = {
                onToggleFavorite(
                    likeableRecipe
                )
            },
            syncState = likeableRecipe.favoriteState
        )
    }
}

@Composable
private fun DetailScreenSectionTitle(
    @StringRes stringRes: Int,
) {
    Text(
        text = stringResource(stringRes),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = 12.dp),
        color = MaterialTheme.colorScheme.secondary,
    )
}

@Composable
private fun IngredientRow(ingredients: List<Pair<String, String>>) {
    ingredients.forEach { (ingredient, measure) ->
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
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
}
