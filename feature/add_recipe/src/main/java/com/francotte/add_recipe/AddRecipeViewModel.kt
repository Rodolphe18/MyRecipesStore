package com.francotte.add_recipe

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.francotte.domain.FavoriteManager
import com.francotte.network.model.Ingredient
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AddRecipeViewModel @Inject constructor(private val favoriteManager: FavoriteManager) :
    ViewModel() {

    var imageUri by mutableStateOf<Uri?>(null)
    var recipeTitle by mutableStateOf("")
    var recipeInstructions by mutableStateOf("")
    var currentIngredient by mutableStateOf("")
    var currentQuantity by mutableStateOf("")
    var quantityType by mutableStateOf("")
    var recipeIngredients = mutableStateListOf<Ingredient>()

    fun onRecipeCreated() {
        imageUri = null
        recipeTitle = ""
        recipeInstructions = ""
        currentIngredient =""
        quantityType = ""
        currentQuantity = ""
    }

}