package com.francotte.myrecipesstore

import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.francotte.myrecipesstore.notifications.DailyNotificationWorkManager

import dagger.hilt.android.HiltAndroidApp
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class FoodApplication:Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        scheduleDailyRecipeNotification(this)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

}

@RequiresApi(Build.VERSION_CODES.O)
fun scheduleDailyRecipeNotification(context: Context) {
    val dailyRequest = OneTimeWorkRequestBuilder<DailyNotificationWorkManager>()
        .setInitialDelay(2, TimeUnit.SECONDS) // petite attente pour test
        .build()

       // PeriodicWorkRequestBuilder<DailyNotificationWorkManager>(1, TimeUnit.DAYS)
       // .setInitialDelay(500, TimeUnit.MILLISECONDS)
       // .build()

    WorkManager.getInstance(context).enqueue(dailyRequest)
    Log.d("DailyNotification", "WorkManager: Daily worker scheduled")
}

@RequiresApi(Build.VERSION_CODES.O)
fun calculateInitialDelay(): Long {
    val now = LocalDateTime.now()
    val target = now.withHour(18).withMinute(0).withSecond(0)
    val adjustedTarget = if (now >= target) target.plusDays(1) else target
    return ChronoUnit.MILLIS.between(now, adjustedTarget)
}