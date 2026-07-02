package com.francotte.home.delegate

import androidx.compose.runtime.Immutable
import com.francotte.data.interfaces.UserHomeRepository
import com.francotte.home.RefreshMode
import com.francotte.model.LikeableRecipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

interface EnglishRecipesDelegate {
    val englishRecipes: StateFlow<EnglishRecipes>

    suspend fun observeEnglishRecipes()

    suspend fun refreshEnglishRecipes(mode: RefreshMode)
}

class EnglishRecipesDelegateImpl @Inject constructor(
    private val repository: UserHomeRepository
) : EnglishRecipesDelegate {

    private val _englishRecipes = MutableStateFlow(EnglishRecipes())
    override val englishRecipes: StateFlow<EnglishRecipes> = _englishRecipes.asStateFlow()

    override suspend fun observeEnglishRecipes() {
        repository.observeEnglishAreaRecipes().collect { recipes ->
            _englishRecipes.update { current ->
                if (recipes.isEmpty()) {
                    current.copy(loading = false, error = true, recipes = emptyList())
                } else {
                    current.copy(recipes = recipes, loading = false, error = false)
                }
            }
        }
    }

    override suspend fun refreshEnglishRecipes(mode: RefreshMode) {
        when (mode) {
            RefreshMode.PullToRefresh -> {
                _englishRecipes.update { it.copy(refreshing = true) }
            }
            RefreshMode.RetrySection -> {
                _englishRecipes.update { it.copy(loading = true) }
            }
        }

        try {
            repository.refreshSpecificFoodAreaSection("British", true)
        } finally {
            _englishRecipes.update { it.copy(refreshing = false, loading = false) }
        }
    }
}

@Immutable
data class EnglishRecipes(
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val error: Boolean = false,
    val recipes: List<LikeableRecipe> = emptyList(),
) {
    val hasRecipes: Boolean
        get() = recipes.isNotEmpty()
}
