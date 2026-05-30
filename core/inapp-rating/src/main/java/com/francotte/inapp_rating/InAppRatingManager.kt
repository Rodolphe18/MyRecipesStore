package com.francotte.inapp_rating

import android.app.Activity
import com.francotte.common.counters.LaunchCounter
import javax.inject.Inject

class InAppRatingManager @Inject constructor(
        private val inAppRatingPreferences: InAppRatingPreferences,
        private val inAppReview: InAppReview,
        private val playStoreOpener: PlayStoreOpener,
        private val launchCounter: LaunchCounter,
    ) {
        val count = launchCounter.getLaunchCount()
        val hasRated = inAppRatingPreferences.hasRatedOrNotAskAgain()
        val lastPrompt = inAppRatingPreferences.lastPromptLaunch()

        fun shouldTryToShowInAppReview(): Boolean = !hasRated && count % 3 == 0

        fun shouldShowCustomRatingBottomSheet(): Boolean = !hasRated && count % 10 == 0 && count != lastPrompt

        fun inAppRatingDialogShown() {
            inAppRatingPreferences.setLastPromptLaunch(launchCounter.getLaunchCount())
        }

        fun setHasBeenRatedOrNotAskAgainToTrue() {
            inAppRatingPreferences.setHasRatedOrNotAskAgain(true)
        }

        suspend fun requestReviewOnPlayStore(activity: Activity): Boolean = playStoreOpener.launchPlayStore(activity)

        suspend fun requestInAppReview(activity: Activity): Boolean = inAppReview.launchInAppReview(activity)
    }
