package com.francotte.myrecipesstore.ui.compose.user_recipes

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.francotte.myrecipesstore.manager.FavoriteManager
import com.francotte.myrecipesstore.network.model.CustomRecipe
import com.francotte.myrecipesstore.network.model.Ingredient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject



@HiltViewModel
class CustomRecipeDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val favoriteManager: FavoriteManager
) : ViewModel() {

    private val detailRoute: CustomRecipeDetailRoute? = runCatching {
        savedStateHandle.toRoute<CustomRecipeDetailRoute>()
    }.getOrNull()

    val recipeId = detailRoute?.id

    private val _recipe: MutableStateFlow<CustomRecipe?> = MutableStateFlow(null)
    val recipe = _recipe.asStateFlow()

    var imageUri by mutableStateOf<Uri?>(null)
    var recipeTitle by mutableStateOf("")
    var recipeInstructions by mutableStateOf("")
    var currentIngredient by mutableStateOf("")
    var currentQuantity by mutableStateOf("")
    var quantityType by mutableStateOf("")
    var recipeIngredients = mutableStateListOf<Ingredient>()

    val hasBeenUpdated = MutableStateFlow(false)

    fun onRecipeUpdated() {
        imageUri = null
        recipeTitle = ""
        recipeInstructions = ""
        currentIngredient =""
        currentQuantity = ""
        quantityType = ""
    }

    init {
        recipeId?.let { getCustomRecipe() }
    }

    fun getCustomRecipe() {
        viewModelScope.launch {
            val result = favoriteManager.getUserCustomRecipe(recipeId!!)
            result?.let {
                _recipe.update { result }
            }
        }
    }


}
