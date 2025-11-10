package com.francotte.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.francotte.data.repository.FullRecipeRepositoryImpl
import com.francotte.model.LikeableRecipe
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
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

    private val _recipesMap = mutableStateMapOf<Int, LikeableRecipe>()
    val recipesMap: SnapshotStateMap<Int, LikeableRecipe> = _recipesMap

    private val _deeplinkRecipe: MutableStateFlow<LikeableRecipe?> = MutableStateFlow(null)
    val deeplinkRecipe: StateFlow<LikeableRecipe?> = _deeplinkRecipe

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title

    init {
        if (ids.isNotEmpty()) {
            getRecipe()
        }
    }

    fun getRecipe() {
        viewModelScope.launch {
            detailRecipeRepository.observeFullRecipe(ids[currentPage]).collectLatest { result ->
                if (result.isSuccess) {
                    _recipesMap[currentPage] = result.getOrNull() as LikeableRecipe
                    _title.value = _recipesMap[currentPage]?.recipe?.strMeal ?: ""
                }
            }
        }
    }

    fun loadRecipeById(id: String) {
        viewModelScope.launch {
            detailRecipeRepository.observeFullRecipe(id.toLong()).collectLatest { result ->
                if (result.isSuccess) {
                    val likeableRecipe = result.getOrNull() as LikeableRecipe
                    _deeplinkRecipe.value = likeableRecipe

                    _title.value = likeableRecipe.recipe.strMeal
                }
            }
        }
    }
}
