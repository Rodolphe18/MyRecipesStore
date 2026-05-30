package com.francotte.domain

import android.util.Log
import com.francotte.data.interfaces.SearchContentsRepository
import com.francotte.datastore.UserDataRepository
import com.francotte.model.SearchResult
import com.francotte.model.UserData
import com.francotte.model.UserSearchResult
import com.francotte.model.mapToLikeableLightRecipes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * A use case which returns the searched contents matched with the search query.
 */
class GetSearchContentsUseCase @Inject constructor(
    private val searchContentsRepository: SearchContentsRepository,
    private val userDataRepository: UserDataRepository,
) {

    operator fun invoke(
        searchQuery: String,
    ): Flow<UserSearchResult> =
        searchContentsRepository.searchContents(searchQuery)
            .mapToUserSearchResult(userDataRepository.userData)
}

private fun Flow<SearchResult>.mapToUserSearchResult(userDataStream: Flow<UserData>): Flow<UserSearchResult> =
    combine(userDataStream) { searchResult, userData ->
        Log.d("debug_search_result_categories", searchResult.categories.toString())
        Log.d("debug_search_result_ing", searchResult.ingredients.toString())
        Log.d("debug_search_result_recipes", searchResult.lightRecipes.toString())
        UserSearchResult(
            categories = searchResult.categories,
            ingredients = searchResult.ingredients,
            areas = searchResult.areas,
            likeableRecipes = searchResult.lightRecipes.mapToLikeableLightRecipes(userData),
        )
    }
