package com.francotte.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

 /**
  * Cette classe sert seulement à afficher une notification si l'application est active.
  *  Si ce n'est pas le cas, FCM affiche la notification via le système Android.
  */

@Singleton
class AndroidNotifier @Inject constructor(@ApplicationContext private val context: Context) :
    Notifier {


    override fun postNotification(title: String, body: String, idMeal: String?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }
        val pendingIntent = idMeal?.let {
            PendingIntent.getActivity(
                context,
                0,
                Intent(Intent.ACTION_VIEW, "myapp://recipe/$it".toUri()).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }
        val notification = NotificationCompat.Builder(context, NotificationChannels.DAILY_MEAL)
            .setSmallIcon(R.mipmap.ic_custom_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(context)
                .notify((System.currentTimeMillis() % Int.MAX_VALUE).toInt(), notification)
        } catch (_: SecurityException) {
        }
    }
}
