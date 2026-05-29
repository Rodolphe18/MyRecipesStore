package com.francotte.data.sync

import android.content.Context

interface SyncScheduler {
    fun enqueueForLogin(context: Context)
    fun enqueueForToggle(context: Context)
}
