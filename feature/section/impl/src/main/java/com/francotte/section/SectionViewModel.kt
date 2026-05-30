package com.francotte.section

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.francotte.data.interfaces.UserHomeRepository
import com.francotte.model.LikeableRecipe
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = SectionViewModel.Factory::class)
class SectionViewModel @AssistedInject constructor(
    private val repository: UserHomeRepository,
    @Assisted val sectionName: String,
) : ViewModel() {


    private val _sectionUiState = MutableStateFlow(SectionUiState())

    val sectionUiState = _sectionUiState.asStateFlow()

    init {
        viewModelScope.launch {
            _sectionUiState.update { it.copy(loading = true) }
            repository
                .observeFoodAreaSection(sectionName)
                .collect { recipes ->
                    _sectionUiState.update { uiState ->
                        if (recipes.isNotEmpty()) {
                            uiState.copy(loading = false, error = null, recipes = recipes)
                        } else {
                            uiState.copy(loading = false, error = "Une erreur est survenue")
                        }
                    }
                }
        }
    }

    fun onAction(action: SectionAction) {
        when (action) {
            SectionAction.Reload -> refresh()
            else -> Unit
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _sectionUiState.update { it.copy(loading = true, error = null) }
            val errorMessage = repository.refreshSpecificFoodAreaSection(sectionName, true)

            if (errorMessage != null) {
                _sectionUiState.update {
                    it.copy(
                        loading = false,
                        error = errorMessage,
                    )
                }
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(title: String): SectionViewModel
    }

}

data class SectionUiState(
    val loading:Boolean=false,
    val error:String?= null,
    val recipes: List<LikeableRecipe> = emptyList(),
)

sealed interface SectionAction {
    data object Reload: SectionAction
    data object BackClick : SectionAction

    data class RecipeClick(
        val recipeIds: List<String>,
        val index: Int,
        val title: String,
    ) : SectionAction

    data class ToggleFavorite(
        val recipe: LikeableRecipe,
    ) : SectionAction
}
