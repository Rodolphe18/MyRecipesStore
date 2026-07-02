package com.francotte.premium

import com.francotte.common.extension.ApplicationScope
import com.francotte.data.interfaces.UserDataRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PremiumManager @Inject constructor(
    userDataRepository: UserDataRepository,
    @ApplicationScope coroutineScope: CoroutineScope,
) : PremiumRepository {

    override val isPremium: StateFlow<Boolean> = userDataRepository.userData
        .map { it.isPremium }
        .stateIn(coroutineScope, SharingStarted.Eagerly, false)
}
