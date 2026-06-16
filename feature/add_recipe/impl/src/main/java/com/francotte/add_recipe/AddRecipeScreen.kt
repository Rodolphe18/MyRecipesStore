package com.francotte.add_recipe

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.francotte.common.extension.bitmapToUri
import com.francotte.designsystem.component.CustomButton
import com.francotte.designsystem.theme.Orange
import com.francotte.ui.CustomTextField
import com.francotte.ui.MeasurementUnitDropDownMenu

@Composable
fun AddRecipeScreen(
    state: AddRecipeState,
    onAction: (AddRecipeAction) -> Unit,
) {
    var showAnimation by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    LaunchedEffect(state.isAuthenticated) {
        if (state.isAuthenticated) {
            showAnimation = true
        }
    }
    LaunchedEffect(state.instructions) {
        if (state.instructions.isEmpty()) {
            focusManager.clearFocus()
        }
    }
    if (!state.isAuthenticated) {
        LoginRedirectScreen { onAction(AddRecipeAction.OnGoToLogin) }
    } else {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .verticalScroll(rememberScrollState()),
        ) {
            AnimatedVisibility(
                visible = showAnimation,
                enter =
                    slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(600),
                    ),
            ) {
                val context = LocalContext.current
                val launcherGallery =
                    rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
                        onAction(AddRecipeAction.OnImageChange(it))
                    }
                val launcherCamera =
                    rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) {
                        it?.let { bitmap ->
                            onAction(AddRecipeAction.OnImageChange(bitmapToUri(context, bitmap)))
                        }
                    }
                Column(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    CustomTextField(
                        state.title,
                        { onAction(AddRecipeAction.OnTitleChange(it)) },
                        label = "Title",
                        verticalPadding = 12.dp,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Images",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            modifier =
                                Modifier
                                    .height(40.dp)
                                    .weight(1f),
                            onClick = {
                                onAction(AddRecipeAction.OnImageChange(null))
                                launcherGallery.launch("image/*")
                            },
                        ) {
                            Icon(Icons.Default.Photo, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Gallery", textAlign = TextAlign.Center, fontSize = 12.sp)
                        }
                        Button(
                            modifier =
                                Modifier
                                    .height(40.dp)
                                    .weight(1f),
                            onClick = {
                                onAction(AddRecipeAction.OnImageChange(null))
                                launcherCamera.launch()
                            },
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Take a picture",
                                textAlign = TextAlign.Center,
                                fontSize = 12.sp,
                            )
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                    state.imageUri?.let {
                        Image(
                            painter =
                                rememberAsyncImagePainter(
                                    model =
                                        ImageRequest
                                            .Builder(LocalContext.current)
                                            .data(it)
                                            .crossfade(true)
                                            .diskCachePolicy(CachePolicy.ENABLED)
                                            .memoryCachePolicy(CachePolicy.ENABLED)
                                            .build(),
                                ),
                            contentDescription = "Photo",
                            modifier =
                                Modifier
                                    .size(180.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    // Ingrédients
                    Text(
                        text = "Ingredients",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        CustomTextField(
                            state.currentIngredient,
                            { onAction(AddRecipeAction.OnIngredientChange(it)) },
                            label = "Ingredient",
                            modifier = Modifier.weight(0.6f),
                            verticalPadding = 12.dp,
                            maxLines = 3,
                        )

                        CustomTextField(
                            state.currentQuantity,
                            { onAction(AddRecipeAction.OnQuantityChange(it)) },
                            label = "Qty",
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(0.3f),
                            verticalPadding = 12.dp,
                        )
                        MeasurementUnitDropDownMenu(Modifier.weight(0.45f)) {
                            onAction(AddRecipeAction.OnQuantityTypeChange(it.displayName))
                        }
                        Box(
                            modifier =
                                Modifier
                                    .height(50.dp)
                                    .weight(0.2f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Orange)
                                    .clickable {
                                        onAction(AddRecipeAction.OnAddIngredient)
                                        focusManager.clearFocus()
                                    },
                        ) {
                            Icon(
                                modifier =
                                    Modifier
                                        .height(40.dp)
                                        .align(Alignment.Center),
                                imageVector = Icons.Default.Add,
                                tint = Color.White,
                                contentDescription = "Add",
                            )
                        }
                    }

                    Column {
                        if (state.ingredients.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            state.ingredients.forEach { (ingredient, qty, measureType) ->
                                Text(
                                    "• $ingredient: $qty $measureType",
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Instructions
                    CustomTextField(
                        state.instructions,
                        { onAction(AddRecipeAction.OnInstructionsChange(it)) },
                        label = "Instructions",
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
                        minLines = 5,
                        maxLines = 50,
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    CustomButton(
                        onClick = { onAction(AddRecipeAction.OnSubmit) },
                        enabled = state.canSubmit,
                        contentText = R.string.add_recipe,
                    )
                }
            }
        }
    }
}

@Composable
fun LoginRedirectScreen(goToLoginScreen: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.smile),
            contentDescription = null,
            modifier = Modifier.size(180.dp),
        )
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.add_recipe_access),
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 24.dp),
            lineHeight = 30.sp,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier =
                Modifier
                    .height(56.dp)
                    .width(240.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Orange)
                    .clickable { goToLoginScreen() },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Go to login screen",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
            )
        }
    }
}
