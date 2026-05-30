package com.francotte.home.delegate

import com.francotte.data.interfaces.UserHomeRepository
import com.francotte.home.RefreshMode
import com.francotte.model.LikeableRecipe
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

interface LatestRecipesDelegate {
    val latestRecipes: StateFlow<LatestRecipes>
    val snackBarEvent: SharedFlow<String>

    suspend fun observeLatestRecipes()
    suspend fun refreshLatestRecipes(mode: RefreshMode)
    fun setLatestRecipesCurrentPage(page: Int)
}

class LatestRecipesDelegateImpl @Inject constructor(
    private val repository: UserHomeRepository
) : LatestRecipesDelegate {

    private val _latestRecipes = MutableStateFlow(LatestRecipes())

    override val latestRecipes: StateFlow<LatestRecipes> = _latestRecipes.asStateFlow()

    private val _snackBarEvent = MutableSharedFlow<String>()
    override val snackBarEvent = _snackBarEvent.asSharedFlow()

    override suspend fun observeLatestRecipes() {
        repository.observeLatestRecipes()
            .collect { recipes ->
                _latestRecipes.update { current ->
                    if (recipes.isEmpty()) {
                        current.copy(loading = false, error = true, recipes = emptyList())
                    } else {
                        current.copy(recipes = recipes, loading = false, error = false)
                    }
                }
            }
    }

    override suspend fun refreshLatestRecipes(mode: RefreshMode) {

        when (mode) {
            RefreshMode.PullToRefresh -> {
                _latestRecipes.update {
                    it.copy(refreshing = true)
                }
            }

            RefreshMode.RetrySection -> {
                _latestRecipes.update {
                    it.copy(loading = true)
                }
            }
        }


        try {
            repository.refreshLatestRecipes(true)?.let { message ->
                _snackBarEvent.emit(message)
            }
        } finally {
            _latestRecipes.update {
                it.copy(refreshing = false, loading = false)
            }
        }
    }


    override fun setLatestRecipesCurrentPage(page: Int) {
        _latestRecipes.update { current ->
            current.copy(currentPage = page)
        }
    }
}


data class LatestRecipes(
    val recipes: List<LikeableRecipe> = emptyList(),
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val error: Boolean = false,
    val currentPage: Int = 0,
) {

    val hasRecipes: Boolean
        get() = recipes.isNotEmpty()
}
