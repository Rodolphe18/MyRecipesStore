package com.francotte.inapp_rating

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.core.net.toUri
import javax.inject.Inject

interface PlayStoreOpener {

    fun launchPlayStore(activity: Activity): Boolean
}

class PlayStoreOpenerImpl @Inject constructor() : PlayStoreOpener {

    override fun launchPlayStore(activity: Activity): Boolean {
        val packageName = activity.packageName

        // 1) Essaye d'abord l'app Play Store (market://)
        val marketIntent = Intent(
            Intent.ACTION_VIEW,
            "market://details?id=$packageName".toUri()
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        }

        try {
            activity.startActivity(marketIntent)
            return true
        } catch (_: ActivityNotFoundException) {
            // Play Store pas dispo -> fallback web
        } catch (_: Throwable) {
            // Autre souci (très rare)
        }

        // 2) Fallback web
        val webIntent = Intent(
            Intent.ACTION_VIEW,
            "https://play.google.com/store/apps/details?id=$packageName".toUri()
        )

        return try {
            activity.startActivity(webIntent)
            true
        } catch (_: Throwable) {
            false
        }
    }
}