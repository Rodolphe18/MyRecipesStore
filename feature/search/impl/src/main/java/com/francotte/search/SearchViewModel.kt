package com.francotte.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.francotte.data.interfaces.SearchContentsRepository
import com.francotte.domain.GetSearchContentsUseCase
import com.francotte.feature.search.api.SearchMode
import com.francotte.model.LikeableRecipe
import com.francotte.model.UserSearchResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    getSearchContentsUseCase: GetSearchContentsUseCase,
    private val searchContentsRepository: SearchContentsRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val searchQuery = savedStateHandle.getStateFlow(key = SEARCH_QUERY, initialValue = "")

    private val searchResultUiState: StateFlow<SearchResultUiState> =
        searchContentsRepository.searchContentsIsReady()
            .flatMapLatest { ftsTableAreReady ->
                if (!ftsTableAreReady) {
                    flowOf(SearchResultUiState.SearchNotReady)
                } else {
                    searchQuery.flatMapLatest { query ->
                        if (query.trim().length < SEARCH_QUERY_MIN_LENGTH) {
                            flowOf(SearchResultUiState.EmptyQuery)
                        } else {
                            getSearchContentsUseCase(query)
                                .map<UserSearchResult, SearchResultUiState> { data ->
                                    SearchResultUiState.Success(
                                        categories = data.categories,
                                        areas = data.areas,
                                        ingredients = data.ingredients,
                                        likeableRecipes = data.likeableRecipes
                                    )
                                }
                                .catch { emit(SearchResultUiState.LoadFailed) }
                        }
                    }
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = SearchResultUiState.Loading,
            )

    val state: StateFlow<SearchState> =
        combine(searchQuery, searchResultUiState) { query, result ->
            SearchState(query = query, result = result)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SearchState(),
        )

    private val _events = Channel<SearchEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: SearchAction) {
        when (action) {
            is SearchAction.OnQueryChange -> savedStateHandle[SEARCH_QUERY] = action.query
            is SearchAction.OnSearchModeClick -> viewModelScope.launch {
                _events.send(SearchEvent.NavigateToSearchMode(action.mode))
            }
            is SearchAction.OnSearchTypeClick -> viewModelScope.launch {
                _events.send(SearchEvent.NavigateToSearchRecipes(action.item, action.mode))
            }
            is SearchAction.OnRecipeClick -> {
                val recipes = (state.value.result as? SearchResultUiState.Success)?.likeableRecipes ?: return
                val clicked = recipes.getOrNull(action.index) ?: return
                viewModelScope.launch {
                    _events.send(
                        SearchEvent.NavigateToRecipe(
                            ids = recipes.map { it.recipe.idMeal },
                            index = action.index,
                            title = clicked.recipe.strMeal,
                        )
                    )
                }
            }
            // Favorite toggling is handled outside the VM (FavoriteManager decoupling).
            is SearchAction.OnToggleFavorite -> Unit
        }
    }

    fun ensureFtsReady() {
        viewModelScope.launch {
            searchContentsRepository.ensureFtsReady(SEARCH_MIN_FTS_ENTITY_COUNT)
        }
    }
}

data class SearchState(
    val query: String = "",
    val result: SearchResultUiState = SearchResultUiState.Loading,
)

sealed interface SearchAction {
    data class OnQueryChange(val query: String) : SearchAction
    data class OnSearchModeClick(val mode: SearchMode) : SearchAction
    data class OnSearchTypeClick(val item: String, val mode: SearchMode) : SearchAction
    data class OnRecipeClick(val index: Int) : SearchAction
    data class OnToggleFavorite(val recipe: LikeableRecipe) : SearchAction
}

sealed interface SearchEvent {
    data class NavigateToSearchMode(val mode: SearchMode) : SearchEvent
    data class NavigateToSearchRecipes(val item: String, val mode: SearchMode) : SearchEvent
    data class NavigateToRecipe(val ids: List<String>, val index: Int, val title: String) : SearchEvent
}


/** Minimum length where search query is considered as [SearchResultUiState.EmptyQuery] */
private const val SEARCH_QUERY_MIN_LENGTH = 3

/** Minimum number of the fts table's entity count where it's considered as search is not ready */
private const val SEARCH_MIN_FTS_ENTITY_COUNT = 1
private const val SEARCH_QUERY = "searchQuery"
