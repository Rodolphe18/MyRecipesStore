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
        val data = remoteMessage.data

        val title = data["title"] ?: "Recette du jour 🍽️"
        val body = data["body"]
            ?: data["strMeal"]
            ?: "Nouvelle recette disponible"
        val idMeal = data["idMeal"]
        notifier.postNotification(title, body, idMeal)
    }
}
