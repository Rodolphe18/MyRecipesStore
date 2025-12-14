package com.francotte.billing

import android.app.Activity
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

interface BillingController {
    val isPremium: StateFlow<Boolean>
    fun launchPurchase(activity: Activity, offerToken: String)
}

@Singleton
class BillingControllerImpl @Inject constructor(
    private val billingManager: BillingManager
) : BillingController {

    override val isPremium: StateFlow<Boolean> = billingManager.isPremium

    override fun launchPurchase(activity: Activity, offerToken: String) {
        val productDetails = billingManager.productDetails.value ?: return
        billingManager.launchBillingFlow(activity, productDetails, offerToken)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class BillingControllerModule {
    @Binds
    abstract fun bindBillingController(impl: BillingControllerImpl): BillingController
}
