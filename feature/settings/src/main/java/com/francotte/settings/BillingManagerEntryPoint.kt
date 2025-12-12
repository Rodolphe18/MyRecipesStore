package com.francotte.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.francotte.billing.BillingManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface BillingEntryPoint {
    fun billingManager(): BillingManager
}

@Composable
fun rememberBillingManager(): BillingManager {
    val context = LocalContext.current.applicationContext
    return remember {
        EntryPointAccessors.fromApplication(context, BillingEntryPoint::class.java)
            .billingManager()
    }
}