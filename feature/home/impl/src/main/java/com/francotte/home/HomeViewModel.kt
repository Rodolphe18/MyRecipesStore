package com.francotte.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.francotte.data.repository.UserHomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val repository: UserHomeRepository) : ViewModel() {
    private val _currentPage = MutableStateFlow(0)
    val currentPage = _currentPage.asStateFlow()

    private val _isReloading = MutableStateFlow(false)
    val isReloading = _isReloading.asStateFlow()

    val snackBarEvent = MutableSharedFlow<String>()
    private val latestRecipes = repository.observeLatestRecipes().mapToLatestRecipes()
    private val americanRecipes = repository.observeAmericanAreaRecipes().mapToAmericanRecipes()
    private val englishRecipes = repository.observeEnglishAreaRecipes().mapToEnglishRecipes()
    private val areasRecipes = repository.observeFoodAreaSections().mapToAreasRecipes()

    val uiState: StateFlow<HomeUiState> = combine(
        latestRecipes,
        americanRecipes,
        areasRecipes,
        englishRecipes,
    ) { latest, american, areas, english ->
        HomeUiState(
            latest = latest,
            american = american,
            areas = areas,
            english = english,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        HomeUiState()
    )


    fun refreshLatestRecipes() {
        viewModelScope.launch {
            _isReloading.value = true
            runCatching {
                repository.refreshLatestRecipes(true)?.let {
                    snackBarEvent.emit(it)
                }
            }
            _isReloading.value = false
        }
    }

    fun setLatestRecipesCurrentPage(page: Int) {
        _currentPage.value = page
    }
}
