package com.francotte.inapp_rating

import android.app.Activity
import com.francotte.data.interfaces.UserDataRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class InAppRatingManager @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val inAppReviewLauncher: InAppReviewLauncher,
    private val playStoreLauncher: PlayStoreLauncher,
) : InAppRatingRepository {

    override suspend fun shouldTryToShowInAppReview(): Boolean {
        val data = userDataRepository.userData.first()
        return !data.hasRated && data.launchCount % 3 == 0
    }

    override suspend fun shouldShowCustomRatingBottomSheet(): Boolean {
        val data = userDataRepository.userData.first()
        return !data.hasRated && data.launchCount % 6 == 0 && data.launchCount != data.lastPromptLaunch
    }

    override suspend fun inAppRatingDialogShown() {
        userDataRepository.setLastPromptLaunch(userDataRepository.userData.first().launchCount)
    }

    override suspend fun setHasBeenRatedOrNotAskAgainToTrue() {
        userDataRepository.setHasRated(true)
    }

    override fun launchPlayStoreForReview(activity: Activity): Boolean = playStoreLauncher.launchPlayStore(activity)

    override suspend fun launchInAppReview(activity: Activity): Boolean = inAppReviewLauncher.launchInAppReview(activity)
}
