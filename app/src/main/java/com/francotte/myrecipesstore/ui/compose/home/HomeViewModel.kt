package com.francotte.myrecipesstore.ui.compose.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.francotte.myrecipesstore.domain.model.LikeableRecipe
import com.francotte.myrecipesstore.repository.LikeableLightRecipesRepository
import com.francotte.myrecipesstore.ui.compose.categories.CategoriesUiState
import com.francotte.myrecipesstore.util.restartableWhileSubscribed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: LikeableLightRecipesRepository
) : ViewModel() {

    var currentPage by mutableIntStateOf(0)

    var isReloading by mutableStateOf(false)

    private val _latestRecipes = MutableStateFlow<LatestRecipes>(LatestRecipes.Loading)
    val latestRecipes: StateFlow<LatestRecipes> = _latestRecipes.asStateFlow()

    private val _americanRecipes = MutableStateFlow<AmericanRecipes>(AmericanRecipes.Loading)
    val americanRecipes:StateFlow<AmericanRecipes> = _americanRecipes.asStateFlow()

    private val _englishRecipes = MutableStateFlow<EnglishRecipes>(EnglishRecipes.Loading)
    val englishRecipes: StateFlow<EnglishRecipes> = _englishRecipes

    private val _areasRecipes = MutableStateFlow<AreasRecipes>(AreasRecipes.Loading)
    val areasRecipes: StateFlow<AreasRecipes> = _areasRecipes

    init {
        loadInitialData()
    }

   private fun loadInitialData() {
        viewModelScope.launch {
            repository
                .observeAmericanAreaRecipes()
                .map { result ->
                    if (result.isSuccess) {
                        AmericanRecipes.Success(result.getOrDefault(emptyList()))
                    } else {
                        AmericanRecipes.Error
                    }
                }
                .collect {
                    _americanRecipes.value = it
                }
        }
            viewModelScope.launch {
                repository
                    .observeLatestRecipes()
                    .map { result ->
                        if (result.isSuccess) {
                            LatestRecipes.Success(result.getOrDefault(emptyList()))
                        } else {
                            LatestRecipes.Error
                        }
                    }
                    .collect {
                        _latestRecipes.value = it
                    }
            }
    }

    fun loadMore() {
        viewModelScope.launch {
            repository.observeEnglishAreaRecipes()
                .map { result ->
                    if (result.isSuccess) {
                        EnglishRecipes.Success(result.getOrDefault(emptyList()))
                    } else {
                        EnglishRecipes.Error
                    }
                }
                .collect { state ->
                    _englishRecipes.value = state
                }
        }
        viewModelScope.launch {
            repository.observeFoodAreaSections()
                .map { result ->
                    if (result.isSuccess) {
                        AreasRecipes.Success(result.getOrDefault(emptyMap()))
                    } else {
                        AreasRecipes.Error
                    }
                }
                .collect { state ->
                    _areasRecipes.value = state
                }
        }
    }


    fun reload() {
        viewModelScope.launch {
            isReloading = true
            _latestRecipes.value = LatestRecipes.Loading
            _americanRecipes.value = AmericanRecipes.Loading
            _areasRecipes.value = AreasRecipes.Loading
            _englishRecipes.value = EnglishRecipes.Loading
            loadInitialData()
            isReloading = false
        }
    }


}

sealed interface LatestRecipes {
    data object Loading : LatestRecipes
    data object Error : LatestRecipes
    data class Success(val latestRecipes: List<LikeableRecipe>) : LatestRecipes
}

sealed interface AmericanRecipes {
    data object Loading : AmericanRecipes
    data object Error : AmericanRecipes
    data class Success(val americanRecipes: List<LikeableRecipe>) : AmericanRecipes
}

sealed interface AreasRecipes {
    data object Loading : AreasRecipes
    data object Error : AreasRecipes
    data class Success(val areasRecipes: Map<String, List<LikeableRecipe>>) : AreasRecipes
}

sealed interface EnglishRecipes {
    data object Loading : EnglishRecipes
    data object Error : EnglishRecipes
    data class Success(val englishRecipes: List<LikeableRecipe>) : EnglishRecipes
}

