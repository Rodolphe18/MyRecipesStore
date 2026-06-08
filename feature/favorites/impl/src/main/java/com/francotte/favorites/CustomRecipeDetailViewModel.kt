package com.francotte.favorites

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.francotte.data.interfaces.FavoritesRepository
import com.francotte.model.CustomIngredient
import com.francotte.model.CustomRecipe
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomRecipeDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val favoritesRepository: FavoritesRepository,
) : ViewModel() {

    val recipeId: String? = savedStateHandle["recipeId"]

    private val _recipe = MutableStateFlow<CustomRecipe?>(null)
    val recipe: StateFlow<CustomRecipe?> = _recipe.asStateFlow()

    var imageUri by mutableStateOf<Uri?>(null)
    var recipeTitle by mutableStateOf("")
    var recipeInstructions by mutableStateOf("")
    var currentIngredient by mutableStateOf("")
    var currentQuantity by mutableStateOf("")
    var quantityType by mutableStateOf("")
    var recipeIngredients = mutableStateListOf<CustomIngredient>()

    val hasBeenUpdated = MutableStateFlow(false)

    private val _snackBarMessage = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val snackBarMessage: SharedFlow<String> = _snackBarMessage.asSharedFlow()

    fun onRecipeUpdated() {
        imageUri = null
        recipeTitle = ""
        recipeInstructions = ""
        currentIngredient = ""
        currentQuantity = ""
        quantityType = ""
    }

    init {
        recipeId?.let { getCustomRecipe() }
    }

    fun getCustomRecipe() {
        viewModelScope.launch {
            favoritesRepository.observeUserCustomRecipe(recipeId!!).collect { result ->
                _recipe.update { result.getOrNull() }
            }
        }
    }

    fun onSubmit(
        recipeId: String,
        title: String,
        ingredients: List<CustomIngredient>,
        instructions: String,
        image: Uri?,
    ) {
        viewModelScope.launch {
            val result = favoritesRepository.updateCustomRecipe(recipeId, title, ingredients, instructions, image)
            if (result.isSuccess) {
                hasBeenUpdated.value = true
                _snackBarMessage.emit("Your recipe has been updated successfully!")
            } else {
                _snackBarMessage.emit("An error occurred. Please try again.")
            }
        }
    }
}
