package com.francotte.database.dao.fts

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.francotte.database.model.AreaFtsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AreaFtsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rows: List<AreaFtsEntity>)

    @Query("SELECT strArea FROM areasFts WHERE areasFts MATCH :query LIMIT :limit")
    fun searchAreaNames(query: String, limit: Int): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM areasFts")
    suspend fun getCountOnce(): Int

    @Query("SELECT COUNT(*) FROM areasFts")
    fun getCount(): Flow<Int>

    @Query("DELETE FROM areasFts")
    suspend fun clear()

}
