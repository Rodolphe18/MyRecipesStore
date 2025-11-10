package com.francotte.common

import android.content.Context
import androidx.core.content.edit
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Singleton
class LaunchCounterManager(context: Context) {

    private val prefs = context.getSharedPreferences("launcher_count_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LAUNCH_COUNT = "launch_count"
    }

    fun incrementLaunchCount(): Int {
        val currentCount = prefs.getInt(KEY_LAUNCH_COUNT, 0)
        val newCount = currentCount + 1
        prefs.edit { putInt(KEY_LAUNCH_COUNT, newCount) }
        return newCount
    }

    fun getLaunchCount(): Int {
        return prefs.getInt(KEY_LAUNCH_COUNT, 0)
    }

    fun reset() {
        prefs.edit { putInt(KEY_LAUNCH_COUNT, 0) }
    }
}

@Module
@InstallIn(SingletonComponent::class)
object LaunchCounterModule {

    @Provides
    @Singleton
    fun provideLaunchCounter(@ApplicationContext context: Context): LaunchCounterManager = LaunchCounterManager(context)
}

