package com.francotte.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.francotte.data.repository.LikeableLightRecipesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel @Inject constructor(
    private val repository: LikeableLightRecipesRepository
) : ViewModel() {

    var currentPage by mutableIntStateOf(0)
        private set

    var isReloading by mutableStateOf(false)
        private set

    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1)


    val latestRecipes = refreshTrigger
        .flatMapLatest { repository.observeLatestRecipes() }
        .mapToLatestRecipes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LatestRecipes.Loading
        )

    val americanRecipes = refreshTrigger
        .flatMapLatest { repository.observeAmericanAreaRecipes() }
        .mapToAmericanRecipes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AmericanRecipes.Loading
        )

    val englishRecipes = refreshTrigger
        .flatMapLatest { repository.observeEnglishAreaRecipes() }
        .mapToEnglishRecipes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = EnglishRecipes.Loading)


    val areasRecipes = refreshTrigger
        .flatMapLatest { repository.observeFoodAreaSections() }
        .mapToAreasRecipes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AreasRecipes.Loading
        )

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            refreshTrigger.emit(Unit)
        }
    }

    fun reload() {
        viewModelScope.launch {
            isReloading = true
            refreshTrigger.emit(Unit)
            isReloading = false
        }
    }
}
