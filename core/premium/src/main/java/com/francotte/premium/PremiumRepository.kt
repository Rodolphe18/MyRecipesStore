package com.francotte.premium

import kotlinx.coroutines.flow.StateFlow

interface PremiumRepository {
    val isPremium: StateFlow<Boolean>
}
