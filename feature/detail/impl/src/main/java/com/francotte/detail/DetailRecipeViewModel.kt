package com.francotte.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.francotte.data.repository.CompositeUserFullRecipeRepository
import com.francotte.model.LikeableRecipe
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = DetailRecipeViewModel.Factory::class)
class DetailRecipeViewModel @AssistedInject constructor(
    private val detailRecipeRepository: CompositeUserFullRecipeRepository,
    @Assisted val ids:List<String>?,
    @Assisted val index:Int?,
    @Assisted val recipeTitle:String?
) : ViewModel() {

    private val longIds = ids?.map { it.toLong() } ?: emptyList()

    val pageCount by mutableIntStateOf(longIds.size)

    var currentPage by mutableIntStateOf(index ?: 0)

    private val _recipesMap = mutableStateMapOf<Int, LikeableRecipe>()
    val recipesMap: SnapshotStateMap<Int, LikeableRecipe> = _recipesMap

    private val _deeplinkRecipe = MutableStateFlow<LikeableRecipe?>(null)
    val deeplinkRecipe: StateFlow<LikeableRecipe?> = _deeplinkRecipe.asStateFlow()

    private val _title = MutableStateFlow(recipeTitle ?: "")
    val title: StateFlow<String> = _title.asStateFlow()

    init {
        if (longIds.isNotEmpty()) {
            getRecipe()
        }
    }

    fun getRecipe() {
        viewModelScope.launch {
            detailRecipeRepository.observeFullRecipe(longIds[currentPage]).collectLatest { result ->
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

    @AssistedFactory
    interface Factory {
        fun create(ids: List<String>?, index: Int?, recipeTitle: String?):
            DetailRecipeViewModel
    }
}
