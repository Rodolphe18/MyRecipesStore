package com.francotte.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.francotte.common.restartableWhileSubscribed
import com.francotte.data.repository.LikeableLightRecipesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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

