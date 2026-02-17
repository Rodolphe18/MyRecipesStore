package com.francotte.database.dao.fts

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.francotte.database.model.RecipeFtsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeFtsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rows: List<RecipeFtsEntity>)

    @Query("SELECT idMeal FROM recipesFts WHERE recipesFts MATCH :query LIMIT :limit")
    fun searchRecipeIds(query: String, limit: Int): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM recipesFts")
    suspend fun getCountOnce(): Int

    @Query("SELECT COUNT(*) FROM recipesFts")
    fun getCount(): Flow<Int>

    @Query("DELETE FROM recipesFts")
    suspend fun clear()
}
