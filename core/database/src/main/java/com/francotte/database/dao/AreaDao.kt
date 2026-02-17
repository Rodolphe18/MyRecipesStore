package com.francotte.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.francotte.database.model.AreaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AreaDao {


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllAreas(areas:List<AreaEntity>):List<Long>

    @Query("SELECT * FROM area")
    suspend fun getAllAreasOnce(): List<AreaEntity>

    @Query("SELECT * FROM area")
    fun observeALlAreas(): Flow<List<AreaEntity>>


}
