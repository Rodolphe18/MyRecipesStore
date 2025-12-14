package com.francotte.billing

import android.app.Activity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingFlowLauncher @Inject constructor(
    private val billingManager: BillingManager
) {
    fun launch(activity: Activity, offerToken: String) {
        val productDetails = billingManager.productDetails.value ?: return
        billingManager.launchBillingFlow(
            activity = activity,
            productDetails = productDetails,
            offerToken = offerToken
        )
    }
}