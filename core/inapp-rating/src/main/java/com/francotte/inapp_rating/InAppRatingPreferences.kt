package com.francotte.inapp_rating

interface InAppRatingPreferences {
    fun hasRatedOrNotAskAgain(): Boolean
    fun setHasRatedOrNotAskAgain(value: Boolean)
    fun lastPromptLaunch(): Int
    fun setLastPromptLaunch(value: Int)
}