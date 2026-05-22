package com.francotte.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.francotte.home.delegate.AreasRecipes
import com.francotte.home.delegate.AreasRecipesDelegate
import com.francotte.home.delegate.EnglishRecipes
import com.francotte.home.delegate.EnglishRecipesDelegate
import com.francotte.home.delegate.JapaneseRecipes
import com.francotte.home.delegate.JapaneseRecipesDelegate
import com.francotte.home.delegate.LatestRecipes
import com.francotte.home.delegate.LatestRecipesDelegate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val latest: LatestRecipes = LatestRecipes(),
    val japanese: JapaneseRecipes = JapaneseRecipes(),
    val areas: AreasRecipes = AreasRecipes(),
    val english: EnglishRecipes = EnglishRecipes(),
) {
    val isRefreshing: Boolean
        get() = latest.refreshing ||
            japanese.refreshing ||
            areas.refreshing ||
            english.refreshing
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val latestRecipesDelegate: LatestRecipesDelegate,
    private val japaneseRecipesDelegate: JapaneseRecipesDelegate,
    private val areasRecipesDelegate: AreasRecipesDelegate,
    private val englishRecipesDelegate: EnglishRecipesDelegate
) : ViewModel(),
    LatestRecipesDelegate by latestRecipesDelegate,
    JapaneseRecipesDelegate by japaneseRecipesDelegate,
    AreasRecipesDelegate by areasRecipesDelegate,
    EnglishRecipesDelegate by englishRecipesDelegate {

    val uiState: StateFlow<HomeUiState> = combine(
        latestRecipes,
        japaneseRecipes,
        areasRecipes,
        englishRecipes,
    ) { latest, american, areas, english ->
        HomeUiState(
            latest = latest,
            japanese = american,
            areas = areas,
            english = english,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        HomeUiState()
    )

    init {
        viewModelScope.launch {
            latestRecipesDelegate.observeLatestRecipes()
        }
        viewModelScope.launch {
            japaneseRecipesDelegate.observeJapaneseRecipes()
        }
        viewModelScope.launch {
            englishRecipesDelegate.observeEnglishRecipes()
        }
        viewModelScope.launch {
            areasRecipesDelegate.observeAreasRecipes()
        }
    }

    fun refreshAll() {
        viewModelScope.launch {
            launch { latestRecipesDelegate.refreshLatestRecipes(RefreshMode.PullToRefresh) }
            launch { japaneseRecipesDelegate.refreshJapaneseRecipes(RefreshMode.PullToRefresh) }
            launch { areasRecipesDelegate.refreshAreasRecipes(RefreshMode.PullToRefresh) }
            launch { englishRecipesDelegate.refreshEnglishRecipes(RefreshMode.PullToRefresh) }
        }
    }

    fun retryLatestRecipes() {
        viewModelScope.launch {
            Log.d("debug_home_VM_latest", "")
            latestRecipesDelegate.refreshLatestRecipes(RefreshMode.RetrySection)
        }
    }

    fun retryJapaneseRecipes() {
        viewModelScope.launch {
            Log.d("debug_home_VM_japanese", "")
            japaneseRecipesDelegate.refreshJapaneseRecipes(RefreshMode.RetrySection)
        }
    }

    fun retryEnglishRecipes() {
        viewModelScope.launch {
            Log.d("debug_home_VM_english", "")
            englishRecipesDelegate.refreshEnglishRecipes(RefreshMode.RetrySection)
        }
    }

    fun retryAreasRecipes() {
        viewModelScope.launch {
            Log.d("debug_home_VM_areas", "")
            areasRecipesDelegate.refreshAreasRecipes(RefreshMode.RetrySection)
        }
    }

}
