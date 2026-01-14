package com.francotte.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.francotte.data.repository.HomeRepository
import com.francotte.ui.HomeSyncer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    repository: HomeRepository,
    private val homeSyncer: HomeSyncer
) : ViewModel() {

    private val _currentPage = MutableStateFlow(0)
    val currentPage = _currentPage.asStateFlow()

    private val _isReloading = MutableStateFlow(false)
    val isReloading = _isReloading.asStateFlow()

    val latestRecipes = repository.observeLatestRecipes()
        .mapToLatestRecipes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LatestRecipes.Loading)

    val americanRecipes = repository.observeAmericanAreaRecipes()
        .mapToAmericanRecipes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AmericanRecipes.Loading)

    val englishRecipes = repository.observeEnglishAreaRecipes()
        .mapToEnglishRecipes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EnglishRecipes.Loading)

    val areasRecipes = repository.observeFoodAreaSections()
        .mapToAreasRecipes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AreasRecipes.Loading)


    fun onPullToRefresh() {
        viewModelScope.launch {
            _isReloading.value = true
            runCatching { homeSyncer.syncLatest(true) }
            _isReloading.value = false
        }
    }

    fun setCurrentPage(page: Int) { _currentPage.value = page }
}
