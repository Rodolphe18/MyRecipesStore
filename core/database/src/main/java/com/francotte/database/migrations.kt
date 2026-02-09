package com.francotte.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {

        db.execSQL("""
      CREATE TABLE IF NOT EXISTS light_recipe_entity_new (
        idMeal TEXT NOT NULL,
        strMeal TEXT NOT NULL,
        strMealThumb TEXT NOT NULL,
        category TEXT,
        area TEXT,
        isLatest INTEGER NOT NULL,
        savedTimestamp INTEGER,
        PRIMARY KEY(idMeal)
      )
    """.trimIndent())

        db.execSQL("""
      INSERT INTO light_recipe_entity_new (
        idMeal, strMeal, strMealThumb, category, area, isLatest, savedTimestamp
      )
      SELECT
        idMeal, strMeal, strMealThumb, category, area, isLatest, savedTimestamp
      FROM light_recipe_entity
    """.trimIndent())

        db.execSQL("DROP TABLE light_recipe_entity")
        db.execSQL("ALTER TABLE light_recipe_entity_new RENAME TO light_recipe_entity")

        // ✅ Index names EXACTEMENT comme Room les génère par défaut
        db.execSQL("""
      CREATE INDEX IF NOT EXISTS index_light_recipe_entity_area_savedTimestamp
      ON light_recipe_entity(area, savedTimestamp)
    """.trimIndent())

        db.execSQL("""
      CREATE INDEX IF NOT EXISTS index_light_recipe_entity_category_savedTimestamp
      ON light_recipe_entity(category, savedTimestamp)
    """.trimIndent())

        db.execSQL("""
      CREATE INDEX IF NOT EXISTS index_light_recipe_entity_isLatest_savedTimestamp
      ON light_recipe_entity(isLatest, savedTimestamp)
    """.trimIndent())
    }
}
