package com.francotte.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.francotte.data.interfaces.SearchContentsRepository
import com.francotte.domain.GetSearchContentsUseCase
import com.francotte.model.UserSearchResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
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

    val searchQuery = savedStateHandle.getStateFlow(key = SEARCH_QUERY, initialValue = "")

    val searchResultUiState: StateFlow<SearchResultUiState> =
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

    fun onSearchQueryChanged(query: String) {
        savedStateHandle[SEARCH_QUERY] = query
    }

    fun ensureFtsReady() {
        viewModelScope.launch {
            searchContentsRepository.ensureFtsReady(SEARCH_MIN_FTS_ENTITY_COUNT)
        }
    }

}



/** Minimum length where search query is considered as [SearchResultUiState.EmptyQuery] */
private const val SEARCH_QUERY_MIN_LENGTH = 3

/** Minimum number of the fts table's entity count where it's considered as search is not ready */
private const val SEARCH_MIN_FTS_ENTITY_COUNT = 1
private const val SEARCH_QUERY = "searchQuery"
