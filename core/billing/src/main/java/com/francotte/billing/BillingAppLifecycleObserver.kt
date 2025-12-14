package com.francotte.billing

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingAppLifecycleObserver @Inject constructor(
    private val billingManager: BillingManager
) : DefaultLifecycleObserver {

    override fun onStart(owner: LifecycleOwner) {
        billingManager.startConnection()
    }

    override fun onStop(owner: LifecycleOwner) {
        billingManager.endConnection()
    }
}