package com.francotte.settings

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsSession


fun launchCustomTab(
    context: Context,
    uri: Uri,
    customTabsSession: CustomTabsSession?,
    buildIntent: CustomTabsIntent.Builder.() -> CustomTabsIntent = { build() },
    fallback: (context: Context, uri: Uri) -> Unit = { ctx, u ->
        ctx.startActivity(Intent(Intent.ACTION_VIEW, u))
    }
) {
    val session = customTabsSession
    Log.d("debug_web_session", session.toString())
    val intent = if (session == null) {
        CustomTabsIntent.Builder()
    } else {
        CustomTabsIntent.Builder(session)
    }.buildIntent()
    var customTabsOk = false
    try {
        intent.launchUrl(context, uri)
        customTabsOk = true
    } catch (expected: ActivityNotFoundException) {
        Log.d("debug_web_error", expected.message.toString())
    }
    if (!customTabsOk) {
        Log.d("debug_web_fallback", uri.toString())
        fallback(context, uri)
    }
}