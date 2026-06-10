package com.francotte.home

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
import com.francotte.model.LikeableRecipe
import com.francotte.model.Recipe
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeState(
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

    val state: StateFlow<HomeState> = combine(
        latestRecipes,
        japaneseRecipes,
        areasRecipes,
        englishRecipes,
    ) { latest, american, areas, english ->
        HomeState(
            latest = latest,
            japanese = american,
            areas = areas,
            english = english,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        HomeState()
    )

    private val _events = Channel<HomeEvent>()
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch { latestRecipesDelegate.observeLatestRecipes() }
        viewModelScope.launch { japaneseRecipesDelegate.observeJapaneseRecipes() }
        viewModelScope.launch { englishRecipesDelegate.observeEnglishRecipes() }
        viewModelScope.launch { areasRecipesDelegate.observeAreasRecipes() }
        // Bridge the latest delegate's snackbar messages into the unified event stream.
        viewModelScope.launch {
            snackBarEvent.collect { _events.send(HomeEvent.ShowSnackbar(it)) }
        }
    }

    fun onAction(action: HomeAction) {
        when (action) {
            HomeAction.OnRefreshAll -> refreshAll()
            HomeAction.OnRetryLatest -> retryLatestRecipes()
            HomeAction.OnRetryJapanese -> retryJapaneseRecipes()
            HomeAction.OnRetryAreas -> retryAreasRecipes()
            HomeAction.OnRetryEnglish -> retryEnglishRecipes()
            is HomeAction.OnCurrentPageChange -> setLatestRecipesCurrentPage(action.page)
            is HomeAction.OnOpenSection -> viewModelScope.launch {
                _events.send(HomeEvent.NavigateToSection(action.sectionName))
            }
            is HomeAction.OnRecipeClick -> openRecipe(action.source, action.index)
            is HomeAction.OnVideoClick -> openVideo(action.index)
            // Favorite toggling is handled outside the VM (FavoriteManager decoupling).
            is HomeAction.OnToggleFavorite -> Unit
        }
    }

    /** Derives the navigation payload from `state` so the Screen never computes ids. */
    private fun openRecipe(source: HomeRecipeSource, index: Int) {
        val recipes = recipesFor(source)
        val clicked = recipes.getOrNull(index) ?: return
        viewModelScope.launch {
            _events.send(
                HomeEvent.NavigateToRecipe(
                    ids = recipes.map { it.recipe.idMeal },
                    index = index,
                    title = clicked.recipe.strMeal,
                )
            )
        }
    }

    private fun openVideo(index: Int) {
        val recipe = state.value.latest.recipes.getOrNull(index)?.recipe as? Recipe ?: return
        viewModelScope.launch {
            _events.send(HomeEvent.NavigateToVideo(recipe.strYoutube))
        }
    }

    private fun recipesFor(source: HomeRecipeSource): List<LikeableRecipe> = when (source) {
        HomeRecipeSource.Latest -> state.value.latest.recipes
        HomeRecipeSource.Japanese -> state.value.japanese.recipes
        HomeRecipeSource.English -> state.value.english.recipes
        is HomeRecipeSource.Area -> state.value.areas.recipes[source.areaName].orEmpty()
    }

    private fun refreshAll() {
        viewModelScope.launch {
            launch { latestRecipesDelegate.refreshLatestRecipes(RefreshMode.PullToRefresh) }
            launch { japaneseRecipesDelegate.refreshJapaneseRecipes(RefreshMode.PullToRefresh) }
            launch { areasRecipesDelegate.refreshAreasRecipes(RefreshMode.PullToRefresh) }
            launch { englishRecipesDelegate.refreshEnglishRecipes(RefreshMode.PullToRefresh) }
        }
    }

    private fun retryLatestRecipes() {
        viewModelScope.launch { latestRecipesDelegate.refreshLatestRecipes(RefreshMode.RetrySection) }
    }

    private fun retryJapaneseRecipes() {
        viewModelScope.launch { japaneseRecipesDelegate.refreshJapaneseRecipes(RefreshMode.RetrySection) }
    }

    private fun retryEnglishRecipes() {
        viewModelScope.launch { englishRecipesDelegate.refreshEnglishRecipes(RefreshMode.RetrySection) }
    }

    private fun retryAreasRecipes() {
        viewModelScope.launch { areasRecipesDelegate.refreshAreasRecipes(RefreshMode.RetrySection) }
    }
}

sealed interface HomeRecipeSource {
    data object Latest : HomeRecipeSource
    data object Japanese : HomeRecipeSource
    data object English : HomeRecipeSource
    data class Area(val areaName: String) : HomeRecipeSource
}

sealed interface HomeAction {
    data object OnRefreshAll : HomeAction
    data object OnRetryLatest : HomeAction
    data object OnRetryJapanese : HomeAction
    data object OnRetryAreas : HomeAction
    data object OnRetryEnglish : HomeAction
    data class OnCurrentPageChange(val page: Int) : HomeAction
    data class OnRecipeClick(val source: HomeRecipeSource, val index: Int) : HomeAction
    data class OnVideoClick(val index: Int) : HomeAction
    data class OnOpenSection(val sectionName: String) : HomeAction
    data class OnToggleFavorite(val recipe: LikeableRecipe) : HomeAction
}

sealed interface HomeEvent {
    data class NavigateToRecipe(val ids: List<String>, val index: Int, val title: String) : HomeEvent
    data class NavigateToVideo(val youtubeUrl: String) : HomeEvent
    data class NavigateToSection(val sectionName: String) : HomeEvent
    data class ShowSnackbar(val message: String) : HomeEvent
}
