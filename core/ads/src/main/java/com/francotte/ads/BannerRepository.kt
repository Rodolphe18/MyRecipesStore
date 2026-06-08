package com.francotte.ads

import kotlinx.coroutines.flow.StateFlow

interface BannerRepository {
    val shouldShowBanners: StateFlow<Boolean>
}
