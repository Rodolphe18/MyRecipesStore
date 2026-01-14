package com.francotte.data.sync

import com.francotte.data.repository.OfflineFirstHomeRepository
import com.francotte.ui.HomeSyncer
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeSyncerImpl @Inject constructor(
    private val repo: OfflineFirstHomeRepository,
) : HomeSyncer {
    override suspend fun syncLatest(force: Boolean) {
        repo.refreshLatestRecipes(force)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class HomeSyncModule {
    @Binds
    @Singleton
    abstract fun bindHomeSyncer(impl: HomeSyncerImpl): HomeSyncer
}