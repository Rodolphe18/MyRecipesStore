package com.francotte.billing

import android.app.Activity
import com.android.billingclient.api.ProductDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface BillingManager {
    val productDetails: StateFlow<ProductDetails?>
    val events: SharedFlow<String>
    fun startConnection(scope: CoroutineScope)
    fun endConnection()
    fun launchBillingFlow(activity: Activity, productDetails: ProductDetails, offerToken: String)
}
