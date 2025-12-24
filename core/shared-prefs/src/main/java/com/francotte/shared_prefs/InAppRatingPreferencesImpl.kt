package com.francotte.shared_prefs

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import com.francotte.inapp_rating.InAppRatingPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class InAppRatingPreferencesImpl @Inject constructor(
    @ApplicationContext context: Context
) : InAppRatingPreferences {

    private val prefs = context.getSharedPreferences("rating_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_HAS_RATED = "has_rated"
        private const val KEY_LAST_PROMPT_LAUNCH = "last_prompt_launch"
    }

    override fun hasRatedOrNotAskAgain(): Boolean {
        Log.d("debug_has_rated", prefs.getBoolean(KEY_HAS_RATED, false).toString())
        return prefs.getBoolean(KEY_HAS_RATED, false)
    }

    override fun setHasRatedOrNotAskAgain(value: Boolean) {
        prefs.edit { putBoolean(KEY_HAS_RATED, value) }
    }

    override fun lastPromptLaunch(): Int =
        prefs.getInt(KEY_LAST_PROMPT_LAUNCH, 0)

    override fun setLastPromptLaunch(value: Int) {
        prefs.edit { putInt(KEY_LAST_PROMPT_LAUNCH, value) }
    }
}
