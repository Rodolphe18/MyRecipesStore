package com.francotte.inapp_rating

import android.app.Activity
import android.util.Log
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume


class InAppReviewImpl @Inject constructor() : InAppReview {

    override suspend fun launchInAppReview(activity: Activity): Boolean {
        val manager = ReviewManagerFactory.create(activity)

        val reviewInfo: ReviewInfo = suspendCancellableCoroutine { cont ->
            manager.requestReviewFlow()
                .addOnCompleteListener { task ->
                    Log.d("debug_request_review_flow", task.result.toString())
                    if (task.isSuccessful) cont.resume(task.result)
                    else return@addOnCompleteListener
                }
        } ?: return false


        return suspendCancellableCoroutine { cont ->
            manager.launchReviewFlow(activity, reviewInfo)
                .addOnCompleteListener {
                    cont.resume(true)
                }
        }
    }
}

interface InAppReview {

    suspend fun launchInAppReview(activity: Activity): Boolean

}
