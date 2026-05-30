package com.francotte.home.delegate

import com.francotte.data.interfaces.UserHomeRepository
import com.francotte.home.RefreshMode
import com.francotte.model.LikeableRecipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

interface JapaneseRecipesDelegate {
    val japaneseRecipes: StateFlow<JapaneseRecipes>

    suspend fun observeJapaneseRecipes()

    suspend fun refreshJapaneseRecipes(mode: RefreshMode)
}

class JapaneseRecipesDelegateImpl @Inject constructor(
    private val repository: UserHomeRepository
) : JapaneseRecipesDelegate {

    private val _japaneseRecipes: MutableStateFlow<JapaneseRecipes> =
        MutableStateFlow(JapaneseRecipes())

    override val japaneseRecipes: StateFlow<JapaneseRecipes> = _japaneseRecipes.asStateFlow()

    override suspend fun observeJapaneseRecipes() {
        repository.observeJapaneseAreaRecipes().collect { recipes ->
            _japaneseRecipes.update { current ->
                if (recipes.isEmpty()) {
                    current.copy(loading = false, error = true, recipes = emptyList())
                } else {
                    current.copy(recipes = recipes, loading = false, error = false)
                }
            }
        }
    }


    override suspend fun refreshJapaneseRecipes(mode: RefreshMode) {
        when (mode) {
            RefreshMode.PullToRefresh -> {
                _japaneseRecipes.update {
                    it.copy(refreshing = true)
                }
            }

            RefreshMode.RetrySection -> {
                _japaneseRecipes.update {
                    it.copy(
                        loading = true,
                        error = false
                    )
                }
            }
        }

        try {
            repository.refreshSpecificFoodAreaSection("Japanese", true)
        } finally {
            _japaneseRecipes.update {
                it.copy(
                    refreshing = false,
                    loading = false
                )
            }
        }
    }

}

data class JapaneseRecipes(
    val loading: Boolean = true,
    val refreshing:Boolean = false,
    val error: Boolean = false,
    val recipes: List<LikeableRecipe> = emptyList()
) {

    val hasRecipes: Boolean
        get() = recipes.isNotEmpty()
}

