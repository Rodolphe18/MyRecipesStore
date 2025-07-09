package com.francotte.myrecipesstore.ui.compose.add_recipe

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.rememberAsyncImagePainter
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.francotte.myrecipesstore.network.model.Ingredient
import java.io.ByteArrayOutputStream


@Composable
fun AddRecipeScreen(onSubmit: (title: String, ingredients: List<Ingredient>, instructions: String, images: List<Uri>) -> Unit
) {
    val context = LocalContext.current
    val imageUris = remember { mutableStateListOf<Uri>() }
    val launcherGallery = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) {
        imageUris.addAll(it)
    }
    val launcherCamera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) {
        it?.let { bitmap ->
            val uri = bitmapToUri(context, bitmap)
            imageUris.add(uri)
        }
    }

    var title by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }
    val ingredients = remember { mutableStateListOf<Ingredient>() }

    var currentIngredient by remember { mutableStateOf("") }
    var currentQuantity by remember { mutableStateOf("") }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
        .verticalScroll(rememberScrollState())
    ) {
        Text(text = "Add a recipe", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(16.dp))

        // Titre
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Titre") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Images
        Text(text = "Images", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(modifier = Modifier.height(60.dp).weight(1f), onClick = { launcherGallery.launch("image/*") }) {
                Text("Gallery", textAlign = TextAlign.Center)
            }
            Button(modifier = Modifier.height(60.dp).weight(1f), onClick = { launcherCamera.launch() }) {
                Text("Take a picture", textAlign = TextAlign.Center)
            }
        }
        Spacer(Modifier.height(16.dp))
        LazyRow {
            items(imageUris) { uri ->
                Image(
                    painter = rememberAsyncImagePainter(model = ImageRequest.Builder(LocalContext.current)
                        .data(uri)
                        .crossfade(true)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .build()),
                    contentDescription = "Photo",
                    modifier = Modifier
                        .size(180.dp)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Ingrédients
        Text(text = "Ingredients", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = currentIngredient,
                onValueChange = { currentIngredient = it },
                label = { Text("Ingredient") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            )
            OutlinedTextField(
                value = currentQuantity,
                onValueChange = { currentQuantity = it },
                label = { Text("Quantity") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            )
            Button(shape = CircleShape, onClick = {
                if (currentIngredient.isNotBlank()) {
                    ingredients.add(Ingredient(currentIngredient, currentQuantity))
                    currentIngredient = ""
                    currentQuantity = ""
                }
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }

        Column {
            ingredients.forEach { (ingredient, qty) ->
                Text("- $ingredient: $qty", color = MaterialTheme.colorScheme.onSurface)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Instructions
        OutlinedTextField(
            value = instructions,
            onValueChange = { instructions = it },
            label = { Text("Instructions") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 5,
            maxLines = 10,
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                onSubmit(title, ingredients, instructions, imageUris)
            },
            modifier = Modifier.align(Alignment.CenterHorizontally),
            enabled = title.isNotBlank() && instructions.isNotBlank() && ingredients.isNotEmpty() && imageUris.isNotEmpty()
        ) {
            Text("Add the recipe")
        }
    }
}

fun bitmapToUri(context: Context, bitmap: Bitmap): Uri {
    val bytes = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
    val path = MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, "RecipeImage", null)
    return path.toUri()
}