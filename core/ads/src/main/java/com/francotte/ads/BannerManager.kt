package com.francotte.ads

import com.francotte.common.extension.ApplicationScope
import com.francotte.premium.PremiumRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BannerManager @Inject constructor(
    premiumRepository: PremiumRepository,
    @ApplicationScope coroutineScope: CoroutineScope,
) : BannerRepository {

    override val shouldShowBanners: StateFlow<Boolean> = premiumRepository.isPremium
        .map { !it }
        .stateIn(coroutineScope, SharingStarted.Eagerly, true)
}
