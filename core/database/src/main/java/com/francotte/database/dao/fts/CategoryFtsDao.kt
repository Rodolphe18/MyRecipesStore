package com.francotte.database.dao.fts

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.francotte.database.model.CategoryFtsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryFtsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rows: List<CategoryFtsEntity>)

    // Comme tu affiches juste des strings, on renvoie directement le nom
    @Query("SELECT strCategory FROM categoriesFts WHERE categoriesFts MATCH :query LIMIT :limit")
    fun searchCategoryNames(query: String, limit: Int): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM categoriesFts")
    suspend fun getCountOnce(): Int

    @Query("SELECT COUNT(*) FROM categoriesFts")
    fun getCount(): Flow<Int>

    @Query("DELETE FROM categoriesFts")
    suspend fun clear()
}
