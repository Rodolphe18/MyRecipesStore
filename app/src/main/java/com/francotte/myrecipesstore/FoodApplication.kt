package com.francotte.myrecipesstore

import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.hilt.work.HiltWorkerFactory
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Configuration
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.francotte.billing.BillingAppLifecycleObserver
import com.francotte.common.counters.LaunchCounter
import com.francotte.notifications.DailyNotificationWorkManager
import com.google.android.gms.ads.MobileAds
import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging
import dagger.hilt.android.HiltAndroidApp
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class FoodApplication:Application(), Configuration.Provider {

    @Inject
    override lateinit var workManagerConfiguration: Configuration

    @Inject
    lateinit var billingObserver: BillingAppLifecycleObserver

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var launchCounter: LaunchCounter


    override fun onCreate() {
        super.onCreate()
        launchCounter.incrementLaunchCount()
        MobileAds.initialize(this) {}
        Firebase.messaging.subscribeToTopic("daily_meal")
            .addOnSuccessListener { Log.d("FCM", "Subscribed to daily_meal") }
            .addOnFailureListener { e -> Log.e("FCM", "Subscribe failed", e) }
        ProcessLifecycleOwner.get().lifecycle.addObserver(billingObserver)
    }

}
