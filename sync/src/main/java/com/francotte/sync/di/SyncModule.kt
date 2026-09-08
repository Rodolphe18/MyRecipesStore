package com.francotte.sync.di

import com.francotte.data.sync.SyncManager
import com.francotte.sync.status.WorkManagerSyncManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Registers the sync module's implementations into the app graph.
 *
 * `@InstallIn` means this library wires itself up: no consumer has to know it exists, and the
 * WorkManager types stay internal to this module.
 */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class SyncModule {

    @Binds
    abstract fun bindsSyncManager(syncManager: WorkManagerSyncManager): SyncManager
}
