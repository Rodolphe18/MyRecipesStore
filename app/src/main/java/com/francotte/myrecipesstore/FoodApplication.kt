package com.francotte.myrecipesstore

import android.app.Application
import com.francotte.notifications.NotificationsInitializer
import com.francotte.sync.initializers.Sync
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class FoodApplication : Application() {

    @Inject
    lateinit var notificationsInitializer: NotificationsInitializer

    override fun onCreate() {
        super.onCreate()
        // Initialize sync; the system responsible for keeping the app's data up to date.
        Sync.initialize(context = this)
        MobileAds.initialize(this) {}
        notificationsInitializer.initialize()
    }
}
