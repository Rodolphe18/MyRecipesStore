package com.francotte.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.francotte.data.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel @Inject constructor(
    private val repository: HomeRepository
) : ViewModel() {

    private val _currentPage = MutableStateFlow(0)
    val currentPage = _currentPage.asStateFlow()

    private val _isReloading = MutableStateFlow(false)
    val isReloading = _isReloading.asStateFlow()


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
            _isReloading.value = true
            refreshTrigger.emit(Unit)
            _isReloading.value = false
        }
    }

    fun setCurrentPage(page: Int) { _currentPage.value = page }


}
