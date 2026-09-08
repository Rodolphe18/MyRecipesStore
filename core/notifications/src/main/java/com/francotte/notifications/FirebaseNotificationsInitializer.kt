package com.francotte.notifications

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal class FirebaseNotificationsInitializer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseMessaging: FirebaseMessaging,
) : NotificationsInitializer {
    override fun initialize() {
        NotificationChannels.createDailyMealChannel(context)
        firebaseMessaging
            .subscribeToTopic(DAILY_MEAL_TOPIC)
            .addOnSuccessListener { Log.d("FCM", "Subscribed to $DAILY_MEAL_TOPIC") }
            .addOnFailureListener { e -> Log.e("FCM", "Subscribe failed", e) }
    }
}

private const val DAILY_MEAL_TOPIC = "daily_meal"
