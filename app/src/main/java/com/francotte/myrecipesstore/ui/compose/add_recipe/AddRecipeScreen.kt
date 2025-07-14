package com.francotte.myrecipesstore.ui.compose.add_recipe

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DoNotDisturbAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.francotte.myrecipesstore.R
import com.francotte.myrecipesstore.network.model.Ingredient
import com.francotte.myrecipesstore.ui.theme.LightYellow
import com.francotte.myrecipesstore.ui.theme.Orange
import kotlinx.coroutines.delay
import java.io.ByteArrayOutputStream


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecipeScreen(
    viewModel: AddRecipeViewModel = hiltViewModel<AddRecipeViewModel>(),
    isAuthenticated: Boolean,
    goToLoginScreen: () -> Unit,
    onSubmit: (title: String, ingredients: List<Ingredient>, instructions: String, image: Uri?) -> Unit
) {
    var showAnimation by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    LaunchedEffect(Unit) {
        if (isAuthenticated) {
            showAnimation = true
        }
    }
    LaunchedEffect(viewModel.recipeInstructions) {
        if (viewModel.recipeInstructions.isEmpty()) {
            focusManager.clearFocus()
        }
    }
    if (!isAuthenticated) {
        LoginRedirectScreen { goToLoginScreen() }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = showAnimation,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(600)
            )
        ) {
            val context = LocalContext.current
            val launcherGallery =
                rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) {
                    viewModel.imageUri = it.firstOrNull()
                }
            val launcherCamera =
                rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) {
                    it?.let { bitmap ->
                        val uri = bitmapToUri(context, bitmap)
                        viewModel.imageUri = uri
                    }
                }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .verticalScroll(rememberScrollState())
            ) {

                // Titre
                OutlinedTextField(
                    value = viewModel.recipeTitle,
                    onValueChange = { viewModel.recipeTitle = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Images
                Text(
                    text = "Images",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        modifier = Modifier
                            .height(40.dp)
                            .weight(1f),
                        onClick = {
                            viewModel.imageUri = null
                            launcherGallery.launch("image/*")
                        }
                    ) {
                        Text("Gallery", textAlign = TextAlign.Center)
                    }
                    Button(
                        modifier = Modifier
                            .height(40.dp)
                            .weight(1f),
                        onClick = {
                            viewModel.imageUri = null
                            launcherCamera.launch()
                        }) {
                        Text("Take a picture", textAlign = TextAlign.Center)
                    }
                }
                Spacer(Modifier.height(8.dp))
                viewModel.imageUri?.let {

                    Image(
                        painter = rememberAsyncImagePainter(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(it)
                                .crossfade(true)
                                .diskCachePolicy(CachePolicy.ENABLED)
                                .memoryCachePolicy(CachePolicy.ENABLED)
                                .build()
                        ),
                        contentDescription = "Photo",
                        modifier = Modifier
                            .size(180.dp)
                            .padding(4.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )

                }
                Spacer(modifier = Modifier.height(16.dp))

                // Ingrédients
                Text(
                    text = "Ingredients",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = viewModel.currentIngredient,
                        onValueChange = { viewModel.currentIngredient = it },
                        label = { Text("Ingredient") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = viewModel.currentQuantity,
                        onValueChange = { viewModel.currentQuantity = it },
                        label = { Text("Qty") },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number
                        ),
                        modifier = Modifier.weight(0.4f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Box(
                        modifier = Modifier
                            .height(60.dp)
                            .weight(0.3f)
                            .offset(y = 4.dp)
                            .clip(CircleShape)
                            .background(Orange)
                            .clickable {
                                if (viewModel.currentIngredient.isNotBlank()) {
                                    viewModel.recipeIngredients.add(
                                        Ingredient(
                                            viewModel.currentIngredient,
                                            viewModel.currentQuantity
                                        )
                                    )
                                    viewModel.currentIngredient = ""
                                    viewModel.currentQuantity = ""
                                    focusManager.clearFocus()
                                }
                            }) {
                        Icon(
                            modifier = Modifier
                                .height(40.dp)
                                .align(Alignment.Center),
                            imageVector = Icons.Default.Add,
                            tint = Color.White,
                            contentDescription = "Add"
                        )
                    }
                }

                Column {
                    if (viewModel.recipeIngredients.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text("Ingredients list", color = Color.Black)
                        viewModel.recipeIngredients.forEach { (ingredient, qty) ->
                            Text("- $ingredient: $qty", color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Instructions
                OutlinedTextField(
                    value = viewModel.recipeInstructions,
                    onValueChange = { viewModel.recipeInstructions = it },
                    label = { Text("Instructions") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 5,
                    maxLines = 10,
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        onSubmit(
                            viewModel.recipeTitle,
                            viewModel.recipeIngredients,
                            viewModel.recipeInstructions,
                            viewModel.imageUri
                        )
                        viewModel.onRecipeCreated()
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    enabled = viewModel.recipeTitle.isNotBlank() && viewModel.recipeInstructions.isNotBlank() && viewModel.recipeIngredients.isNotEmpty()
                ) {
                    Text(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        text = "Add recipe",
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}

fun bitmapToUri(context: Context, bitmap: Bitmap): Uri {
    val bytes = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
    val path =
        MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, "RecipeImage", null)
    return path.toUri()
}

@Composable
fun LoginRedirectScreen(
    goToLoginScreen: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFF3E0),
                            Color(0xFFFFE0B2)
                        )
                    )
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(color = Color(0xFFFFCC80), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.DoNotDisturbAlt,
                contentDescription = null,
                tint = Color(0xFF6D4C41),
                modifier = Modifier.size(64.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.add_recipe_access),
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            color = Color(0xFF6D4C41),
            modifier = Modifier.padding(horizontal = 24.dp),
            lineHeight = 30.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .height(56.dp)
                .width(240.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFFFFA726),
                            Color(0xFFFF7043)
                        )
                    )
                )
                .shadow(4.dp, RoundedCornerShape(16.dp))
                .clickable { goToLoginScreen() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Se connecter",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}