package com.francotte.ui

import android.content.Context
import com.francotte.data.sync.SyncScheduler
import javax.inject.Inject

class WorkManagerSyncScheduler @Inject constructor() : SyncScheduler {
    override fun enqueueForLogin(context: Context) =
        FavoritesSyncScheduler.enqueueForLogin(context)

    override fun enqueueForToggle(context: Context) =
        FavoritesSyncScheduler.enqueueForToggle(context)
}
