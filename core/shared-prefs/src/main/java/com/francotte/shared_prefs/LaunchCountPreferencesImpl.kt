package com.francotte.shared_prefs

import android.content.Context
import androidx.core.content.edit
import com.francotte.common.counters.LaunchCountPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class LaunchCountPreferencesImpl @Inject constructor(
    @ApplicationContext context: Context
) : LaunchCountPreferences {

    private val prefs = context.getSharedPreferences("launcher_count_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LAUNCH_COUNT = "launch_count"
    }

    override fun launchCount(): Int = prefs.getInt(KEY_LAUNCH_COUNT, 0)

    override fun setLaunchCount(value: Int) {
        prefs.edit { putInt(KEY_LAUNCH_COUNT, value) }
    }
}