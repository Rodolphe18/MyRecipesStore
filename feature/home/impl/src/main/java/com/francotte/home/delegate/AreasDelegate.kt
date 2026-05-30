package com.francotte.home.delegate

import android.util.Log
import com.francotte.data.interfaces.UserHomeRepository
import com.francotte.home.RefreshMode
import com.francotte.model.LikeableRecipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

interface AreasRecipesDelegate {
    val areasRecipes: StateFlow<AreasRecipes>

    suspend fun observeAreasRecipes()

    suspend fun refreshAreasRecipes(mode: RefreshMode)
}

class AreasRecipesDelegateImpl @Inject constructor(private val repository: UserHomeRepository):AreasRecipesDelegate {

    private val _areasRecipes = MutableStateFlow(AreasRecipes())

    override val areasRecipes: StateFlow<AreasRecipes> = _areasRecipes.asStateFlow()


    override suspend fun observeAreasRecipes() {
        repository.observeFoodAreaSections().collect { areas ->
            Log.d("debug_areas", areas.toString())
            _areasRecipes.update { current ->
                if (areas.values.isEmpty()) {
                    current.copy(loading = false, error = true, recipes = emptyMap())
                } else {
                    current.copy(recipes = areas, loading = false, error = false)
                }
            }
        }
    }

    override suspend fun refreshAreasRecipes(mode: RefreshMode) {
        when (mode) {
            RefreshMode.PullToRefresh -> _areasRecipes.update { it.copy(refreshing = true) }
            RefreshMode.RetrySection -> _areasRecipes.update { it.copy(loading = true) }
        }

        try {
            repository.refreshMultipleFoodAreaSection(true)
        } finally {
            _areasRecipes.update { it.copy(loading = false, refreshing = false) }
        }


    }

}



data class AreasRecipes (
    val loading:Boolean = true,
    val refreshing: Boolean = false,
    val error : Boolean=false,
    val recipes: Map<String, List<LikeableRecipe>> = emptyMap()
) {

    val hasRecipes: Boolean
        get() = recipes.isNotEmpty()
}
