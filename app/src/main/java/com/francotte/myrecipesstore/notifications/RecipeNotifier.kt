/*
 * Copyright 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.francotte.myrecipesstore.notifications

import android.Manifest.permission
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.core.app.ActivityCompat.checkSelfPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import com.francotte.myrecipesstore.MainActivity
import com.francotte.myrecipesstore.R
import com.francotte.myrecipesstore.domain.model.Recipe
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class RecipeNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
) : Notifier {

    companion object {
        private const val CHANNEL_ID = "recipe_notification_channel"
        private const val GROUP_KEY_RECIPES = "DAILY_RECIPE_NOTIFICATIONS"
        private const val REQUEST_CODE = 100
    }


    override fun postRecipeNotification(recipe: Recipe) = with(context) {
        if (checkSelfPermission(this, permission.POST_NOTIFICATIONS) != PERMISSION_GRANTED) {
            return
        }

        val pendingIntent = context.createPendingIntent(recipe.idMeal)

        val notification = createNotification {
            setContentTitle("Recette du jour")
            setContentText("Découvrez la recette du jour : ${recipe.strMeal}")
            setSmallIcon(R.mipmap.ic_launcher)
            setStyle(NotificationCompat.InboxStyle().addLine(recipe.strMeal))
            setGroup(GROUP_KEY_RECIPES)
            setContentIntent(pendingIntent)
            setAutoCancel(true)
        }

        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(recipe.idMeal.hashCode(), notification)
    }

    private fun Context.createNotification(block: NotificationCompat.Builder.() -> Unit): Notification {
        ensureChannelExists()
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .apply(block)
            .build()
    }

    private fun Context.ensureChannelExists() {
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Recettes quotidiennes",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notification pour une recette aléatoire chaque jour"
            }
            NotificationManagerCompat.from(this).createNotificationChannel(channel)
        }
    }

    private fun Context.createPendingIntent(recipeId: String): PendingIntent? {
        val intent = Intent(this, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = "myapp://recipe/$recipeId".toUri()
            `package` = packageName
        }
        return PendingIntent.getActivity(
            this,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}

interface Notifier {
    fun postRecipeNotification(recipe: Recipe)
}

@Module
@InstallIn(SingletonComponent::class)
internal abstract class NotificationsModule {
    @Binds
    abstract fun bindNotifier(
        notifier: RecipeNotifier,
    ): Notifier
}