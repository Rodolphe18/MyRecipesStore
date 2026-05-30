package com.francotte.add_recipe

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.francotte.data.interfaces.FavoritesRepository
import com.francotte.model.CustomIngredient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddRecipeViewModel @Inject constructor(
    private val favoritesRepository: FavoritesRepository,
) : ViewModel() {
    var imageUri by mutableStateOf<Uri?>(null)
    var recipeTitle by mutableStateOf("")
    var recipeInstructions by mutableStateOf("")
    var currentIngredient by mutableStateOf("")
    var currentQuantity by mutableStateOf("")
    var quantityType by mutableStateOf("")
    var recipeIngredients = mutableStateListOf<CustomIngredient>()

    private val _snackBarMessage = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val snackBarMessage = _snackBarMessage.asSharedFlow()

    fun onRecipeCreated() {
        imageUri = null
        recipeTitle = ""
        recipeInstructions = ""
        currentIngredient = ""
        quantityType = ""
        currentQuantity = ""
    }

    fun onSubmit(
        title: String,
        ingredients: List<CustomIngredient>,
        instructions: String,
        image: Uri?,
    ) {
        viewModelScope.launch {
            val result = favoritesRepository.addCustomRecipe(title, ingredients, instructions, image)
            if (result.isSuccess) {
                _snackBarMessage.emit("Your recipe has been created successfully!")
                onRecipeCreated()
            } else {
                _snackBarMessage.emit("An error occurred. Please try again.")
            }
        }
    }
}
