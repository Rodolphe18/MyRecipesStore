package com.francotte.database.dao.fts

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.francotte.database.model.SearchIndexCategoryStateEntity
import java.time.Instant

@Dao
interface SearchIndexStateDao {

    @Query("""
        SELECT c.strCategory
        FROM full_category_entity c
        LEFT JOIN search_index_category_state s
            ON s.strCategory = c.strCategory
        WHERE s.lastIndexedAt IS NULL OR s.lastIndexedAt < :staleBefore
        ORDER BY COALESCE(s.lastIndexedAt, 0) ASC, c.strCategory ASC
        LIMIT :limit
    """)
    suspend fun getCategoriesToIndex(staleBefore: Instant, limit: Int): List<String>

    @Query("""
        SELECT COUNT(*)
        FROM full_category_entity c
        LEFT JOIN search_index_category_state s
            ON s.strCategory = c.strCategory
        WHERE s.lastIndexedAt IS NULL OR s.lastIndexedAt < :staleBefore
    """)
    suspend fun getRemainingToIndexCount(staleBefore: Instant): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStates(states: List<SearchIndexCategoryStateEntity>)
}
