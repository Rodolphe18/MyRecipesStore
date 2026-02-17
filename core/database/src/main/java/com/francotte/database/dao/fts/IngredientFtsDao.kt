package com.francotte.database.dao.fts

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.francotte.database.model.IngredientFtsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientFtsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rows: List<IngredientFtsEntity>)

    @Query("SELECT name FROM ingredientsFts WHERE ingredientsFts MATCH :query LIMIT :limit")
    fun searchIngredientNames(query: String, limit: Int): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM ingredientsFts")
    suspend fun getCountOnce(): Int

    @Query("SELECT COUNT(*) FROM ingredientsFts")
    fun getCount(): Flow<Int>

    @Query("DELETE FROM ingredientsFts")
    suspend fun clear()
}
