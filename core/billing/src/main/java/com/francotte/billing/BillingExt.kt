package com.francotte.billing

import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryProductDetailsResult
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

suspend fun BillingClient.queryProductDetailsAwait(
    params: QueryProductDetailsParams
): Pair<BillingResult, List<ProductDetails>> =
    suspendCancellableCoroutine { cont ->
        queryProductDetailsAsync(params) { billingResult, queryResult: QueryProductDetailsResult ->
            val list = queryResult.productDetailsList
            if (cont.isActive) cont.resume(billingResult to list)
        }
    }

suspend fun BillingClient.queryPurchasesAwait(
    params: QueryPurchasesParams
): Pair<BillingResult, List<Purchase>> =
    suspendCancellableCoroutine { cont ->
        queryPurchasesAsync(params) { result, purchasesList ->
            if (cont.isActive) cont.resume(result to purchasesList)
        }
    }

suspend fun BillingClient.acknowledgePurchaseAwait(
    params: AcknowledgePurchaseParams
): BillingResult =
    suspendCancellableCoroutine { cont ->
        acknowledgePurchase(params) { result ->
            if (cont.isActive) cont.resume(result)
        }
    }