package com.francotte.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

object NotificationChannels {

    const val DAILY_MEAL = "daily_meal"

    /**
     * Crée le canal de notification "recette du jour". Idempotent : ré-appeler avec le même id
     * ne fait que mettre à jour le nom/la description. À appeler au démarrage de l'app pour que
     * le canal existe quel que soit le chemin (push premier plan OU affichage système en arrière-plan).
     */
    fun createDailyMealChannel(context: Context) {
        val channel = NotificationChannel(
            DAILY_MEAL,
            "Recette du jour 🍽️",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Notifications quotidiennes avec la dernière recette"
        }
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(channel)
    }
}
