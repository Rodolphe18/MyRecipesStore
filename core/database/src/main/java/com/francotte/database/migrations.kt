package com.francotte.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {

        db.execSQL(
            """
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
    """.trimIndent()
        )

        db.execSQL(
            """
      INSERT INTO light_recipe_entity_new (
        idMeal, strMeal, strMealThumb, category, area, isLatest, savedTimestamp
      )
      SELECT
        idMeal, strMeal, strMealThumb, category, area, isLatest, savedTimestamp
      FROM light_recipe_entity
    """.trimIndent()
        )

        db.execSQL("DROP TABLE light_recipe_entity")
        db.execSQL("ALTER TABLE light_recipe_entity_new RENAME TO light_recipe_entity")

        // ✅ Index names EXACTEMENT comme Room les génère par défaut
        db.execSQL(
            """
      CREATE INDEX IF NOT EXISTS index_light_recipe_entity_area_savedTimestamp
      ON light_recipe_entity(area, savedTimestamp)
    """.trimIndent()
        )

        db.execSQL(
            """
      CREATE INDEX IF NOT EXISTS index_light_recipe_entity_category_savedTimestamp
      ON light_recipe_entity(category, savedTimestamp)
    """.trimIndent()
        )

        db.execSQL(
            """
      CREATE INDEX IF NOT EXISTS index_light_recipe_entity_isLatest_savedTimestamp
      ON light_recipe_entity(isLatest, savedTimestamp)
    """.trimIndent()
        )
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
      CREATE TABLE IF NOT EXISTS ingredient (
        localId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        name TEXT NOT NULL,
        description TEXT NOT NULL,
        imageUrl TEXT NOT NULL,
        savedTimeStamp INTEGER
      )
    """.trimIndent()
        )

        db.execSQL(
            """
      CREATE UNIQUE INDEX IF NOT EXISTS index_ingredient_name
      ON ingredient(name)
    """.trimIndent()
        )

        db.execSQL(
            """
      CREATE TABLE IF NOT EXISTS area (
        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        name TEXT NOT NULL
      )
    """.trimIndent()
        )

    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {

        // 1) créer la table si besoin (si elle existait déjà, skip)
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS area (
                localId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL
            )
        """.trimIndent()
        )

        // 2) si la table existait déjà sans unique index, il peut y avoir des doublons
        db.execSQL(
            """
            DELETE FROM area
            WHERE localId NOT IN (
                SELECT MIN(localId) FROM area GROUP BY name
            )
        """.trimIndent()
        )

        // 3) index unique
        db.execSQL(
            """
            CREATE UNIQUE INDEX IF NOT EXISTS index_area_name
            ON area(name)
        """.trimIndent()
        )
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE light_category_entity ADD COLUMN savedTimestamp INTEGER")
        db.execSQL("ALTER TABLE full_category_entity ADD COLUMN savedTimestamp INTEGER")
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {

        // ---- 1) Recipe table (new)
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS light_recipe_entity_new (
                idMeal TEXT NOT NULL PRIMARY KEY,
                strMeal TEXT NOT NULL,
                strMealThumb TEXT NOT NULL
            )
        """.trimIndent()
        )

        db.execSQL(
            """
            INSERT OR REPLACE INTO light_recipe_entity_new (idMeal, strMeal, strMealThumb)
            SELECT idMeal, strMeal, strMealThumb
            FROM light_recipe_entity
        """.trimIndent()
        )


        // ---- 2) AREA : refonte (ancienne: areaId + name ; nouvelle: strArea PK + savedTimeStamp)
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS area_new (
                strArea TEXT NOT NULL PRIMARY KEY,
                savedTimeStamp INTEGER
            )
        """.trimIndent()
        )

        // Copie depuis l'ancienne table area (v6) : elle n'a PAS savedTimeStamp => NULL
        db.execSQL(
            """
            INSERT OR IGNORE INTO area_new (strArea, savedTimeStamp)
            SELECT name AS strArea, NULL
            FROM area
            WHERE name IS NOT NULL AND name != ''
        """.trimIndent()
        )

        // Complète depuis l'ancienne light_recipe_entity.area
        db.execSQL(
            """
            INSERT OR IGNORE INTO area_new (strArea, savedTimeStamp)
            SELECT DISTINCT area AS strArea, NULL
            FROM light_recipe_entity
            WHERE area IS NOT NULL AND area != ''
        """.trimIndent()
        )

        db.execSQL("DROP TABLE area")
        db.execSQL("ALTER TABLE area_new RENAME TO area")


        // ---- 2bis) Area xref
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS recipe_area_xref (
                strArea TEXT NOT NULL,
                idMeal TEXT NOT NULL,
                savedTimestamp INTEGER NOT NULL,
                PRIMARY KEY (strArea, idMeal)
            )
        """.trimIndent()
        )

        db.execSQL(
            """
            INSERT OR REPLACE INTO recipe_area_xref (strArea, idMeal, savedTimestamp)
            SELECT
                area AS strArea,
                idMeal,
                COALESCE(savedTimestamp, (strftime('%s','now') * 1000))
            FROM light_recipe_entity
            WHERE area IS NOT NULL AND area != ''
        """.trimIndent()
        )

        db.execSQL("CREATE INDEX IF NOT EXISTS index_recipe_area_xref_idMeal ON recipe_area_xref(idMeal)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_recipe_area_xref_savedTimestamp ON recipe_area_xref(savedTimestamp)")


        // ---- 3) Category : refonte (supprimer savedTimestamp)
        db.execSQL(
            """
    CREATE TABLE IF NOT EXISTS light_category_entity_new (
        strCategory TEXT NOT NULL PRIMARY KEY
    )
""".trimIndent()
        )

// Copie depuis l'ancienne table light_category_entity si elle existe
        db.execSQL(
            """
    INSERT OR IGNORE INTO light_category_entity_new (strCategory)
    SELECT strCategory
    FROM light_category_entity
    WHERE strCategory IS NOT NULL AND strCategory != ''
""".trimIndent()
        )

// Complète depuis l'ancienne light_recipe_entity.category (au cas où)
        db.execSQL(
            """
    INSERT OR IGNORE INTO light_category_entity_new (strCategory)
    SELECT DISTINCT category
    FROM light_recipe_entity
    WHERE category IS NOT NULL AND category != ''
""".trimIndent()
        )

        db.execSQL("DROP TABLE light_category_entity")
        db.execSQL("ALTER TABLE light_category_entity_new RENAME TO light_category_entity")

// ---- Category xref (inchangé)
        db.execSQL(
            """
    CREATE TABLE IF NOT EXISTS recipe_category_xref (
        strCategory TEXT NOT NULL,
        idMeal TEXT NOT NULL,
        savedTimestamp INTEGER NOT NULL,
        PRIMARY KEY (strCategory, idMeal)
    )
""".trimIndent()
        )

        db.execSQL(
            """
    INSERT OR REPLACE INTO recipe_category_xref (strCategory, idMeal, savedTimestamp)
    SELECT
        category AS strCategory,
        idMeal,
        COALESCE(savedTimestamp, (strftime('%s','now') * 1000))
    FROM light_recipe_entity
    WHERE category IS NOT NULL AND category != ''
""".trimIndent()
        )

        db.execSQL("CREATE INDEX IF NOT EXISTS index_recipe_category_xref_idMeal ON recipe_category_xref(idMeal)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_recipe_category_xref_savedTimestamp ON recipe_category_xref(savedTimestamp)")

        // ---- 4) Drop old + rename recipe table
        db.execSQL("DROP TABLE light_recipe_entity")
        db.execSQL("ALTER TABLE light_recipe_entity_new RENAME TO light_recipe_entity")


        // ---- INGREDIENT : refonte (PK localId -> PK name)
        db.execSQL("""
    CREATE TABLE IF NOT EXISTS ingredient_new (
        name TEXT NOT NULL PRIMARY KEY,
        description TEXT NOT NULL,
        imageUrl TEXT NOT NULL,
        savedTimeStamp INTEGER
    )
""".trimIndent())

// Copie depuis l'ancienne table ingredient (v6)
        db.execSQL("""
    INSERT OR REPLACE INTO ingredient_new (name, description, imageUrl, savedTimeStamp)
    SELECT name, description, imageUrl, savedTimeStamp
    FROM ingredient
    WHERE name IS NOT NULL AND name != ''
""".trimIndent())

// Remplace l'ancienne table
        db.execSQL("DROP TABLE ingredient")
        db.execSQL("ALTER TABLE ingredient_new RENAME TO ingredient")

// Recrée l'index unique attendu par ton @Entity(indices = [Index(value=["name"], unique=true)])
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_ingredient_name ON ingredient(name)")

        // ---- RECIPE_INGREDIENT_XREF : recréation propre
        db.execSQL("DROP TABLE IF EXISTS recipe_ingredient_xref")

        db.execSQL("""
    CREATE TABLE recipe_ingredient_xref (
        ingredientName TEXT NOT NULL,
        idMeal TEXT NOT NULL,
        savedTimestamp INTEGER NOT NULL,
        PRIMARY KEY (ingredientName, idMeal)
    )
""".trimIndent())

        db.execSQL("CREATE INDEX IF NOT EXISTS index_recipe_ingredient_xref_idMeal ON recipe_ingredient_xref(idMeal)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_recipe_ingredient_xref_savedTimestamp ON recipe_ingredient_xref(savedTimestamp)")
    }
}


val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {

        // Si tu as déjà eu des essais de FTS avec un schéma différent, décommente :
        // db.execSQL("DROP TABLE IF EXISTS recipesFts")
        // db.execSQL("DROP TABLE IF EXISTS categoriesFts")
        // db.execSQL("DROP TABLE IF EXISTS areasFts")
        // db.execSQL("DROP TABLE IF EXISTS ingredientsFts")

        // -------------------------
        // 1) FTS tables (VIRTUAL)
        // -------------------------
        db.execSQL("""
            CREATE VIRTUAL TABLE IF NOT EXISTS recipesFts
            USING fts4(
                idMeal TEXT NOT NULL,
                name TEXT NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE VIRTUAL TABLE IF NOT EXISTS categoriesFts
            USING fts4(
                strCategory TEXT NOT NULL,
                name TEXT NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE VIRTUAL TABLE IF NOT EXISTS areasFts
            USING fts4(
                strArea TEXT NOT NULL,
                name TEXT NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE VIRTUAL TABLE IF NOT EXISTS ingredientsFts
            USING fts4(
                name TEXT NOT NULL,
                description TEXT NOT NULL
            )
        """.trimIndent())


        // -------------------------
        // 2) Index state table
        // -------------------------
        // Instant? -> INTEGER nullable (epoch millis) via ton converter
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS search_index_category_state (
                strCategory TEXT NOT NULL PRIMARY KEY,
                lastIndexedAt INTEGER
            )
        """.trimIndent())

        // Optionnel (mais utile pour tes ORDER BY / WHERE sur lastIndexedAt)
        db.execSQL("""
            CREATE INDEX IF NOT EXISTS index_search_index_category_state_lastIndexedAt
            ON search_index_category_state(lastIndexedAt)
        """.trimIndent())


        // -------------------------
        // 3) Pré-remplir les FTS depuis les tables existantes
        // -------------------------
        // IMPORTANT : éviter les doublons si jamais migration rejouée
        db.execSQL("INSERT OR REPLACE INTO recipesFts (idMeal, name) SELECT idMeal, strMeal FROM light_recipe_entity")

        db.execSQL("INSERT OR REPLACE INTO categoriesFts (strCategory, name) SELECT strCategory, strCategory FROM light_category_entity")

        db.execSQL("INSERT OR REPLACE INTO areasFts (strArea, name) SELECT strArea, strArea FROM area")

        db.execSQL("INSERT OR REPLACE INTO ingredientsFts (name, description) SELECT name, description FROM ingredient")
    }
}

