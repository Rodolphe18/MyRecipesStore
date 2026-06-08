package com.francotte.myrecipesstore

import android.app.Application
import android.util.Log
import com.francotte.notifications.NotificationChannels
import com.francotte.ui.CategoriesSyncScheduler
import com.francotte.ui.HomeSyncScheduler
import com.francotte.ui.SearchIndexScheduler
import com.francotte.ui.AreasAndIngredientsSyncScheduler
import com.google.android.gms.ads.MobileAds
import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class FoodApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        HomeSyncScheduler.enqueueOneShot(this)
        AreasAndIngredientsSyncScheduler.enqueueOneShot(this)
        CategoriesSyncScheduler.enqueueOneShot(this)
        SearchIndexScheduler.enqueueIndexChain(this)
        MobileAds.initialize(this) {}
        NotificationChannels.createDailyMealChannel(this)
        Firebase.messaging
            .subscribeToTopic("daily_meal")
            .addOnSuccessListener { Log.d("FCM", "Subscribed to daily_meal") }
            .addOnFailureListener { e -> Log.e("FCM", "Subscribe failed", e) }
    }
}
