package com.francotte.designsystem.component

import androidx.compose.runtime.staticCompositionLocalOf
import com.francotte.billing.BillingController

val LocalBillingController = staticCompositionLocalOf<BillingController> {
        error("PremiumController not provided")
    }