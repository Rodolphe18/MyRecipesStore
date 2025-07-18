package com.francotte.myrecipesstore.ui.compose.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.francotte.myrecipesstore.domain.model.LikeableRecipe
import com.francotte.myrecipesstore.repository.FullRecipeRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailRecipeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val detailRecipeRepository: FullRecipeRepositoryImpl
) : ViewModel() {

    private val detailRoute: DetailRecipeRoute? = runCatching {
        savedStateHandle.toRoute<DetailRecipeRoute>()
    }.getOrNull()

    private val ids = detailRoute?.ids?.map { it.toLong() } ?: emptyList()
    val index = detailRoute?.index

    val pageCount by mutableIntStateOf(ids.size)

    var currentPage by mutableIntStateOf(index ?: 0)

    private val _recipe = mutableStateMapOf<Int, LikeableRecipe>()
    val recipe: SnapshotStateMap<Int, LikeableRecipe> = _recipe

    private val _deeplinkRecipe: MutableStateFlow<LikeableRecipe?> = MutableStateFlow(null)
    val deeplinkRecipe: StateFlow<LikeableRecipe?> = _deeplinkRecipe

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title


    init {
        if (ids.isNotEmpty()) {
            getRecipes()
        }
    }

    fun getRecipes() {
        viewModelScope.launch {
            val result = detailRecipeRepository.observeFullRecipe(ids[currentPage]).first()
            if (result.isSuccess) {
                _recipe[currentPage] = result.getOrNull() as LikeableRecipe
                _title.value = _recipe[currentPage]?.recipe?.strMeal ?: ""
            }
        }
    }

    fun loadRecipeById(id: String) {
        viewModelScope.launch {
            val result = detailRecipeRepository.observeFullRecipe(id.toLong()).first()
            if (result.isSuccess) {
                _deeplinkRecipe.value = result.getOrNull()
                _title.value = result.getOrNull()?.recipe?.strMeal ?: ""
            }
        }
    }

}
