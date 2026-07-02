package com.francotte.notifications

import android.annotation.SuppressLint
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var notifier: Notifier

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val title = remoteMessage.notification?.title ?: "Recette du jour 🍽️"
        val body = remoteMessage.notification?.body ?: (remoteMessage.data["strMeal"] ?: "Nouvelle recette disponible")
        val idMeal = remoteMessage.data["idMeal"]
        notifier.postNotification(title, body, idMeal)
    }
}
