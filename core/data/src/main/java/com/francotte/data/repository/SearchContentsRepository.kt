package com.francotte.data.repository

import com.francotte.model.SearchResult
import kotlinx.coroutines.flow.Flow

interface SearchContentsRepository {
    suspend fun populateFtsData()
    fun searchContents(searchQuery: String): Flow<SearchResult>
    fun searchContentsIsReady(): Flow<Boolean>
    suspend fun ensureFtsReady(minCount: Int)
}
