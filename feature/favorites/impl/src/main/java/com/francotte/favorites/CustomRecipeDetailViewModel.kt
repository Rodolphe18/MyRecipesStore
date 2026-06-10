package com.francotte.favorites

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.francotte.data.interfaces.FavoritesRepository
import com.francotte.model.CustomIngredient
import com.francotte.model.CustomRecipe
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = CustomRecipeDetailViewModel.Factory::class)
class CustomRecipeDetailViewModel @AssistedInject constructor(
    @Assisted private val recipeId: String?,
    private val favoritesRepository: FavoritesRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(CustomRecipeDetailState())
    val state = _state.asStateFlow()

    private val _events = Channel<CustomRecipeDetailEvent>()
    val events = _events.receiveAsFlow()

    init {
        recipeId?.let { id ->
            favoritesRepository.observeUserCustomRecipe(id)
                .onEach { result -> _state.update { it.copy(recipe = result.getOrNull()) } }
                .launchIn(viewModelScope)
        }
    }

    fun onAction(action: CustomRecipeDetailAction) {
        when (action) {
            CustomRecipeDetailAction.OnStartEdit -> _state.update {
                // Seed the edit form with the loaded recipe so editing starts from its current values.
                it.copy(
                    isEditing = true,
                    title = it.recipe?.title.orEmpty(),
                    instructions = it.recipe?.instructions.orEmpty(),
                    ingredients = it.recipe?.ingredients ?: emptyList(),
                )
            }
            is CustomRecipeDetailAction.OnTitleChange -> _state.update { it.copy(title = action.title) }
            is CustomRecipeDetailAction.OnInstructionsChange -> _state.update { it.copy(instructions = action.instructions) }
            is CustomRecipeDetailAction.OnImageChange -> _state.update { it.copy(imageUri = action.uri) }
            is CustomRecipeDetailAction.OnIngredientChange -> _state.update { it.copy(currentIngredient = action.ingredient) }
            is CustomRecipeDetailAction.OnQuantityChange -> _state.update { it.copy(currentQuantity = action.quantity) }
            CustomRecipeDetailAction.OnAddIngredient -> addIngredient()
            CustomRecipeDetailAction.OnSubmit -> submit()
            // Pure navigation, handled by CustomRecipeDetailRoute.
            CustomRecipeDetailAction.OnBackClick -> Unit
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
            val result = favoritesRepository.updateCustomRecipe(
                recipeId.orEmpty(),
                form.title,
                form.ingredients,
                form.instructions,
                form.imageUri,
            )
            if (result.isSuccess) {
                _events.send(CustomRecipeDetailEvent.ShowSnackbar("Your recipe has been updated successfully!"))
                _state.update {
                    it.copy(
                        isEditing = false,
                        imageUri = null,
                        title = "",
                        instructions = "",
                        currentIngredient = "",
                        currentQuantity = "",
                        quantityType = "",
                        ingredients = emptyList(),
                    )
                }
            } else {
                _events.send(CustomRecipeDetailEvent.ShowSnackbar("An error occurred. Please try again."))
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(recipeId: String?): CustomRecipeDetailViewModel
    }
}

data class CustomRecipeDetailState(
    val recipe: CustomRecipe? = null,
    val isEditing: Boolean = false,
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

sealed interface CustomRecipeDetailAction {
    data object OnStartEdit : CustomRecipeDetailAction
    data class OnTitleChange(val title: String) : CustomRecipeDetailAction
    data class OnInstructionsChange(val instructions: String) : CustomRecipeDetailAction
    data class OnImageChange(val uri: Uri?) : CustomRecipeDetailAction
    data class OnIngredientChange(val ingredient: String) : CustomRecipeDetailAction
    data class OnQuantityChange(val quantity: String) : CustomRecipeDetailAction
    data object OnAddIngredient : CustomRecipeDetailAction
    data object OnSubmit : CustomRecipeDetailAction
    data object OnBackClick : CustomRecipeDetailAction
}

sealed interface CustomRecipeDetailEvent {
    data class ShowSnackbar(val message: String) : CustomRecipeDetailEvent
}
