package com.francotte.favorites

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.francotte.common.extension.bitmapToUri
import com.francotte.common.extension.imageRequestBuilder
import com.francotte.designsystem.component.CustomButton
import com.francotte.designsystem.component.TopAppBar
import com.francotte.designsystem.theme.Orange
import com.francotte.ui.CustomTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomRecipeDetailScreen(
    state: CustomRecipeDetailState,
    onAction: (CustomRecipeDetailAction) -> Unit,
) {
    val context = LocalContext.current
    val recipe = state.recipe
    val topAppBarScrollBehavior =
        TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val focusManager = LocalFocusManager.current
    var showImagePickerDialog by remember { mutableStateOf(false) }
    val launcherGallery =
        rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) {
            onAction(CustomRecipeDetailAction.OnImageChange(it.firstOrNull()))
        }
    val launcherCamera =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) {
            it?.let { bitmap ->
                onAction(CustomRecipeDetailAction.OnImageChange(bitmapToUri(context, bitmap)))
            }
        }
    Scaffold(
        modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = recipe?.title ?: "",
                scrollBehavior = topAppBarScrollBehavior,
                navigationIconEnabled = true,
                onNavigationClick = { onAction(CustomRecipeDetailAction.OnBackClick) },
                actionIcon = Icons.Default.Update,
                onActionClick = { onAction(CustomRecipeDetailAction.OnStartEdit) },
            )
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        top = padding.calculateTopPadding() + 12.dp,
                        bottom = 12.dp,
                        start = 16.dp,
                        end = 16.dp,
                    ),
        ) {
            Card(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                        .clickable(enabled = state.isEditing) {
                            showImagePickerDialog = true
                        },
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            ) {
                val painter =
                    if (state.isEditing && state.imageUri != null) {
                        rememberAsyncImagePainter(state.imageUri)
                    } else {
                        rememberAsyncImagePainter(
                            imageRequestBuilder(LocalContext.current, recipe?.imageUrl ?: ""),
                        )
                    }

                Image(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f),
                    painter = painter,
                    contentScale = ContentScale.Crop,
                    contentDescription = null,
                )
            }
            SectionTitle("Title")
            if (state.isEditing) {
                CustomTextField(
                    state.title,
                    { onAction(CustomRecipeDetailAction.OnTitleChange(it)) },
                    label = "Title",
                )
            } else {
                Text(
                    text = recipe?.title ?: "",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                thickness = Dp.Hairline,
                color = Color.LightGray,
            )
            SectionTitle("Ingredients")
            if (state.isEditing) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CustomTextField(
                        state.currentIngredient,
                        { onAction(CustomRecipeDetailAction.OnIngredientChange(it)) },
                        label = "Ingredient",
                        modifier = Modifier.weight(1.2f),
                        verticalPadding = 12.dp,
                    )

                    CustomTextField(
                        state.currentQuantity,
                        { onAction(CustomRecipeDetailAction.OnQuantityChange(it)) },
                        label = "Qty",
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(0.5f),
                        verticalPadding = 12.dp,
                    )
                    Box(
                        modifier =
                            Modifier
                                .height(50.dp)
                                .weight(0.3f)
                                .clip(CircleShape)
                                .background(Orange)
                                .clickable {
                                    onAction(CustomRecipeDetailAction.OnAddIngredient)
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
                                "- $ingredient: $qty $measureType",
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            } else {
                recipe?.ingredients?.forEach { ingredient ->
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = ingredient.name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "x ${ingredient.quantity}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                }
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                thickness = Dp.Hairline,
                color = Color.LightGray,
            )
            SectionTitle("Instructions")
            if (state.isEditing) {
                CustomTextField(
                    state.instructions,
                    { onAction(CustomRecipeDetailAction.OnInstructionsChange(it)) },
                    label = "Instructions",
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
                    minLines = 5,
                    maxLines = 10,
                )
            } else {
                Text(
                    text = recipe?.instructions ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (state.isEditing) {
                CustomButton(
                    onClick = { onAction(CustomRecipeDetailAction.OnSubmit) },
                    enabled = state.canSubmit,
                    contentText = R.string.update_recipe,
                )
            } else {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    shape = RoundedCornerShape(8.dp),
                    onClick = {
                        val shoppingListText =
                            buildString {
                                appendLine("🛒 Groceries list : ${recipe?.title}")
                                appendLine()
                                recipe?.ingredients?.forEach { (ingredient, measure) ->
                                    appendLine("- $ingredient: $measure")
                                }
                            }

                        val shareIntent =
                            Intent(Intent.ACTION_SEND).apply {
                                setType("text/plain")
                                putExtra(
                                    Intent.EXTRA_SUBJECT,
                                    "My groceries list for ${recipe?.title}",
                                )
                                putExtra(Intent.EXTRA_TEXT, shoppingListText)
                            }

                        context.startActivity(
                            Intent.createChooser(
                                shareIntent,
                                "Sharte the groceries list with",
                            ),
                        )
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    Text("Share the groceries list")
                }
            }
            if (showImagePickerDialog) {
                AlertDialog(
                    onDismissRequest = { showImagePickerDialog = false },
                    title = { Text(text = "Add a picture") },
                    text = {
                        Column(
                            Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "From camera",
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            showImagePickerDialog = false
                                            launcherCamera.launch()
                                        }.padding(vertical = 8.dp),
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "From gallery",
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            showImagePickerDialog = false
                                            launcherGallery.launch("image/*")
                                        }.padding(vertical = 8.dp),
                            )
                        }
                    },
                    confirmButton = {},
                    dismissButton = {},
                )
            }
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 8.dp),
        color = MaterialTheme.colorScheme.onSurface,
    )
}
