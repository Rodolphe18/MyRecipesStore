package com.francotte.datastore.migration

import android.content.Context
import androidx.datastore.core.DataMigration
import com.francotte.datastore_proto.UserPreferences
import com.francotte.datastore_proto.copy

class SharedPrefsToDataStoreMigration(private val context: Context) : DataMigration<UserPreferences> {

    override suspend fun shouldMigrate(currentData: UserPreferences): Boolean =
        context.getSharedPreferences("launcher_count_prefs", Context.MODE_PRIVATE)
            .contains("launch_count")

    override suspend fun migrate(currentData: UserPreferences): UserPreferences {
        val launchCountPrefs =
            context.getSharedPreferences("launcher_count_prefs", Context.MODE_PRIVATE)
        val ratingPrefs = context.getSharedPreferences("rating_prefs", Context.MODE_PRIVATE)
        return currentData.copy {
            launchCount = launchCountPrefs.getInt("launch_count", 0)
            hasRated = ratingPrefs.getBoolean("has_rated", false)
            lastPromptLaunch = ratingPrefs.getInt("last_prompt_launch", 0)
        }
    }

    override suspend fun cleanUp() {
        context.getSharedPreferences("launcher_count_prefs", Context.MODE_PRIVATE).edit().clear()
            .apply()
        context.getSharedPreferences("rating_prefs", Context.MODE_PRIVATE).edit().clear().apply()
    }
}
