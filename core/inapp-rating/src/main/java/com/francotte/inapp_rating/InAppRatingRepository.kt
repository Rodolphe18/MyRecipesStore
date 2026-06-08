package com.francotte.inapp_rating

import android.app.Activity

interface InAppRatingRepository {
    suspend fun shouldTryToShowInAppReview(): Boolean
    suspend fun shouldShowCustomRatingBottomSheet(): Boolean
    suspend fun inAppRatingDialogShown()
    suspend fun setHasBeenRatedOrNotAskAgainToTrue()
    fun launchPlayStoreForReview(activity: Activity): Boolean
    suspend fun launchInAppReview(activity: Activity): Boolean
}
