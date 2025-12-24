package com.francotte.testing

import android.app.Activity
import com.francotte.billing.BillingController
import kotlinx.coroutines.flow.MutableStateFlow

class FakeBillingController : BillingController {
    override val isPremium = MutableStateFlow(true)
    override fun launchPurchase(activity: Activity, offerToken: String) {

    }
}