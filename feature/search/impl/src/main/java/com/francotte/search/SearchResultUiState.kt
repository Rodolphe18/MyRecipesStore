package com.francotte.search

import com.francotte.model.LikeableRecipe

sealed interface SearchResultUiState {
    data object Loading : SearchResultUiState

    /**
     * The state query is empty or too short. To distinguish the state between the
     * (initial state or when the search query is cleared) vs the state where no search
     * result is returned, explicitly define the empty query state.
     */
    data object EmptyQuery : SearchResultUiState

    data object LoadFailed : SearchResultUiState

    data class Success(
        val categories: List<String> = emptyList(),
        val areas: List<String> = emptyList(),
        val ingredients: List<String>,
        val likeableRecipes:List<LikeableRecipe>
    ) : SearchResultUiState {

        fun isEmpty(): Boolean = categories.isEmpty() && areas.isEmpty() && ingredients.isEmpty() && likeableRecipes.isEmpty()

    }

    /**
     * A state where the search contents are not ready. This happens when the *Fts tables are not
     * populated yet.
     */
    data object SearchNotReady : SearchResultUiState
}
