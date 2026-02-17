package com.francotte.myrecipesstore

import android.app.Application
import android.util.Log
import androidx.lifecycle.ProcessLifecycleOwner
import com.francotte.billing.BillingAppLifecycleObserver
import com.francotte.common.counters.LaunchCounter
import com.francotte.ui.CategoriesSyncScheduler
import com.francotte.ui.HomeSyncScheduler
import com.francotte.ui.SearchIndexScheduler
import com.francotte.ui.AreasAndIngredientsSyncScheduler
import com.google.android.gms.ads.MobileAds
import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class FoodApplication : Application() {
    @Inject
    lateinit var billingObserver: BillingAppLifecycleObserver

    @Inject
    lateinit var launchCounter: LaunchCounter

    override fun onCreate() {
        super.onCreate()
        HomeSyncScheduler.enqueueOneShot(this)
        AreasAndIngredientsSyncScheduler.enqueueOneShot(this)
        CategoriesSyncScheduler.enqueueOneShot(this)
        SearchIndexScheduler.enqueueIndexChain(this)
        launchCounter.incrementLaunchCount()
        MobileAds.initialize(this) {}
        Firebase.messaging
            .subscribeToTopic("daily_meal")
            .addOnSuccessListener { Log.d("FCM", "Subscribed to daily_meal") }
            .addOnFailureListener { e -> Log.e("FCM", "Subscribe failed", e) }
        ProcessLifecycleOwner.get().lifecycle.addObserver(billingObserver)
    }

}
