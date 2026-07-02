package com.francotte.add_recipe

import android.net.Uri
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.francotte.data.interfaces.FavoritesRepository
import com.francotte.data.interfaces.UserDataRepository
import com.francotte.model.CustomIngredient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddRecipeViewModel @Inject constructor(
    private val favoritesRepository: FavoritesRepository,
    userDataRepository: UserDataRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AddRecipeState())
    val state = _state.asStateFlow()

    private val _events = Channel<AddRecipeEvent>()
    val events = _events.receiveAsFlow()

    init {
        userDataRepository.userData
            .map { it.isConnected && it.token?.isNotBlank() == true }
            .onEach { authenticated -> _state.update { it.copy(isAuthenticated = authenticated) } }
            .launchIn(viewModelScope)
    }

    fun onAction(action: AddRecipeAction) {
        when (action) {
            is AddRecipeAction.OnTitleChange -> _state.update { it.copy(title = action.title) }
            is AddRecipeAction.OnInstructionsChange -> _state.update { it.copy(instructions = action.instructions) }
            is AddRecipeAction.OnImageChange -> _state.update { it.copy(imageUri = action.uri) }
            is AddRecipeAction.OnIngredientChange -> _state.update { it.copy(currentIngredient = action.ingredient) }
            is AddRecipeAction.OnQuantityChange -> _state.update { it.copy(currentQuantity = action.quantity) }
            is AddRecipeAction.OnQuantityTypeChange -> _state.update { it.copy(quantityType = action.quantityType) }
            AddRecipeAction.OnAddIngredient -> addIngredient()
            AddRecipeAction.OnSubmit -> submit()
            // Pure navigation, handled by AddRoute.
            AddRecipeAction.OnGoToLogin -> Unit
        }
    }

    private fun addIngredient() {
        _state.update { current ->
            if (current.currentIngredient.isBlank()) return@update current
            current.copy(
                ingredients = current.ingredients + CustomIngredient(
                    current.currentIngredient,
                    current.currentQuantity,
                    current.quantityType,
                ),
                currentIngredient = "",
                currentQuantity = "",
                quantityType = "",
            )
        }
    }

    private fun submit() {
        val form = state.value
        if (!form.canSubmit) return
        viewModelScope.launch {
            val result = favoritesRepository.addCustomRecipe(
                form.title,
                form.ingredients,
                form.instructions,
                form.imageUri,
            )
            if (result.isSuccess) {
                _events.send(AddRecipeEvent.ShowSnackbar("Your recipe has been created successfully!"))
                resetForm()
            } else {
                _events.send(AddRecipeEvent.ShowSnackbar("An error occurred. Please try again."))
            }
        }
    }

    private fun resetForm() {
        _state.update {
            it.copy(
                imageUri = null,
                title = "",
                instructions = "",
                currentIngredient = "",
                currentQuantity = "",
                quantityType = "",
                ingredients = emptyList(),
            )
        }
    }
}

@Immutable
data class AddRecipeState(
    val isAuthenticated: Boolean = false,
    val imageUri: Uri? = null,
    val title: String = "",
    val instructions: String = "",
    val currentIngredient: String = "",
    val currentQuantity: String = "",
    val quantityType: String = "",
    val ingredients: List<CustomIngredient> = emptyList(),
) {
    val canSubmit: Boolean
        get() = title.isNotBlank() && instructions.isNotBlank() && ingredients.isNotEmpty()
}

sealed interface AddRecipeAction {
    data class OnTitleChange(val title: String) : AddRecipeAction
    data class OnInstructionsChange(val instructions: String) : AddRecipeAction
    data class OnImageChange(val uri: Uri?) : AddRecipeAction
    data class OnIngredientChange(val ingredient: String) : AddRecipeAction
    data class OnQuantityChange(val quantity: String) : AddRecipeAction
    data class OnQuantityTypeChange(val quantityType: String) : AddRecipeAction
    data object OnAddIngredient : AddRecipeAction
    data object OnSubmit : AddRecipeAction
    data object OnGoToLogin : AddRecipeAction
}

sealed interface AddRecipeEvent {
    data class ShowSnackbar(val message: String) : AddRecipeEvent
}
