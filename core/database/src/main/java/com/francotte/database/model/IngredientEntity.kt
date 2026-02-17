package com.francotte.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "ingredient",
    indices = [Index(value = ["name"], unique = true)]
)
data class IngredientEntity(
    @PrimaryKey val name: String,
    val description: String,
    val imageUrl: String,
    val savedTimeStamp: Instant? = null
)

@Entity(tableName = "ingredientsFts")
@Fts4
data class IngredientFtsEntity(
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "description") val description: String,
)

fun IngredientEntity.asFtsEntity() = IngredientFtsEntity(
    name = name,
    description = description,
)
