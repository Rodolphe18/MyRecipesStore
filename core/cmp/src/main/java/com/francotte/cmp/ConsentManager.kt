package com.francotte.cmp

import android.app.Activity
import com.google.android.gms.ads.AdRequest

interface ConsentManager {
    suspend fun ensureConsent(activity: Activity): Boolean
    fun buildAdRequest(): AdRequest
}
