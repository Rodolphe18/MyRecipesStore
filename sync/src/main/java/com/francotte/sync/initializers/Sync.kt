package com.francotte.sync.initializers

import android.content.Context
import androidx.work.WorkManager
import com.francotte.data.sync.SyncKind

object Sync {
    /**
     * Initializes sync, the process that keeps the app's data current.
     *
     * Called from the app module's `Application.onCreate()`. `onCreate` runs on every process
     * start, not only when the user opens the app, so every request below uses a unique work name
     * with [androidx.work.ExistingWorkPolicy.KEEP]: a restart never duplicates work, and never
     * cancels a sync that is already in flight along with its retry count.
     */
    fun initialize(context: Context) {
        WorkManager.getInstance(context).apply {
            enqueueSync(SyncKind.Home)
            enqueueSync(SyncKind.Categories)
            enqueueSync(SyncKind.AreasAndIngredients)
            enqueueSync(SyncKind.SearchIndex)
        }
    }
}
