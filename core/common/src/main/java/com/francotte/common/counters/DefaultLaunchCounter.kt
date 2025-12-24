package com.francotte.common.counters

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class DefaultLaunchCounter @Inject constructor(
    private val prefs: LaunchCountPreferences
) : LaunchCounter {

    override fun getLaunchCount(): Int {
        Log.d("debug_lauch_count", prefs.launchCount().toString())
        return prefs.launchCount()
    }

    override fun incrementLaunchCount(): Int {
        val newCount = prefs.launchCount() + 1
        prefs.setLaunchCount(newCount)
        return newCount
    }
}


interface LaunchCounter {
    fun getLaunchCount(): Int
    fun incrementLaunchCount(): Int
}

interface LaunchCountPreferences {
    fun launchCount(): Int
    fun setLaunchCount(value: Int)
}
