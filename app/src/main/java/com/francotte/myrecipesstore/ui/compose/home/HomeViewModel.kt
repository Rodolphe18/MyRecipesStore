package com.francotte.myrecipesstore.ui.compose.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.francotte.myrecipesstore.domain.model.LikeableRecipe
import com.francotte.myrecipesstore.repository.LikeableLightRecipesRepository
import com.francotte.myrecipesstore.util.restartableWhileSubscribed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(repository: LikeableLightRecipesRepository) : ViewModel() {

    var currentPage by mutableIntStateOf(0)

    var isReloading by mutableStateOf(false)

    val latestRecipes = repository.observeLatestRecipes()
        .mapToLatestRecipes()
        .stateIn(viewModelScope, restartableWhileSubscribed, LatestRecipes.Loading)

    val americanRecipes = repository.observeAmericanAreaRecipes()
        .mapToAmericanRecipes()
        .stateIn(viewModelScope, restartableWhileSubscribed, AmericanRecipes.Loading)

    val englishRecipes = repository.observeEnglishAreaRecipes()
        .mapToEnglishRecipes()
        .stateIn(viewModelScope, restartableWhileSubscribed, EnglishRecipes.Loading)

    val areasRecipes = repository.observeFoodAreaSections()
        .mapToAreasRecipes()
        .stateIn(viewModelScope, restartableWhileSubscribed, AreasRecipes.Loading)


    fun reload() {
        viewModelScope.launch {
            isReloading = true
            delay(1000)
            restartableWhileSubscribed.restart()
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

private fun Flow<Result<List<LikeableRecipe>>>.mapToLatestRecipes() = map {
    if (it.isSuccess) LatestRecipes.Success(it.getOrDefault(emptyList()))
    else LatestRecipes.Error
}

private fun Flow<Result<List<LikeableRecipe>>>.mapToAmericanRecipes() = map {
    if (it.isSuccess) AmericanRecipes.Success(it.getOrDefault(emptyList()))
    else AmericanRecipes.Error
}

private fun Flow<Result<List<LikeableRecipe>>>.mapToEnglishRecipes() = map {
    if (it.isSuccess) EnglishRecipes.Success(it.getOrDefault(emptyList()))
    else EnglishRecipes.Error
}

private fun Flow<Result<Map<String, List<LikeableRecipe>>>>.mapToAreasRecipes() = map {
    if (it.isSuccess) AreasRecipes.Success(it.getOrDefault(emptyMap()))
    else AreasRecipes.Error
}