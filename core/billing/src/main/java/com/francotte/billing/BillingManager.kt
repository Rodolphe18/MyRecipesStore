package com.francotte.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingManager @Inject constructor(@ApplicationContext context: Context) :
    PurchasesUpdatedListener {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val billingClient: BillingClient = BillingClient
        .newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .build()

    private val _productDetails = MutableStateFlow<ProductDetails?>(null)
    val productDetails: StateFlow<ProductDetails?> = _productDetails

    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium

    private val _events = MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<String> = _events

    private val productQueryLock = Mutex()
    private val purchasesQueryLock = Mutex()

    private val handlePurchaseLock = Mutex()

    private var loadJob: Job? = null

    private val isConnecting = AtomicBoolean(false)


    fun startConnection() {
        if (billingClient.isReady) {
            loadBillingData(); return
        }

        if (!isConnecting.compareAndSet(false, true)) return

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                isConnecting.set(false)
                if (result.responseCode == BillingClient.BillingResponseCode.OK) loadBillingData()
                else _events.tryEmit("Erreur Billing setup : ${result.debugMessage}")
            }

            override fun onBillingServiceDisconnected() {
                isConnecting.set(false)
            }
        })
    }

    fun endConnection() {
        loadJob?.cancel()
        loadJob = null
        billingClient.endConnection()
    }

    private fun loadBillingData() {
        if (loadJob?.isActive == true) return

        loadJob = scope.launch {
            queryProducts()
            queryExistingPurchases()
        }
    }


    private suspend fun queryProducts() {
        productQueryLock.withLock {

            if (_productDetails.value != null) return

            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(
                    listOf(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId("premium_id")
                            .setProductType(BillingClient.ProductType.SUBS)
                            .build()
                    )
                )
                .build()

            val (result, details) = billingClient.queryProductDetailsAwait(params)

            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                _productDetails.value = details.firstOrNull()
                if (details.isEmpty()) {
                    _events.tryEmit("Aucun abonnement trouvé (premium_id).")
                }
            } else {
                _events.tryEmit("Erreur chargement produits : ${result.debugMessage}")
            }
        }
    }

    private suspend fun queryExistingPurchases() {
        purchasesQueryLock.withLock {
            val params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()

            val (result, purchases) = billingClient.queryPurchasesAwait(params)

            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                handlePurchases(purchases)
            } else {
                _events.tryEmit("Erreur query purchases : ${result.debugMessage}")
            }
        }
    }

    fun launchBillingFlow(
        activity: Activity,
        productDetails: ProductDetails,
        offerToken: String
    ) {
        val productParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
            .setOfferToken(offerToken)
            .build()

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productParams))
            .build()

        val result = billingClient.launchBillingFlow(activity, flowParams)
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            _events.tryEmit("Impossible de lancer l’achat : ${result.debugMessage}")
        }
    }


    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: List<Purchase?>?
    ) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                scope.launch {
                    handlePurchases(purchases.orEmpty().filterNotNull())
                }
            }

            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _events.tryEmit("Achat annulé")
            }

            else -> {
                _events.tryEmit("Erreur achat : ${billingResult.debugMessage}")
            }
        }
    }

    private suspend fun handlePurchases(purchases: List<Purchase>) {
        // Phase 1 : section critique courte
        val toAcknowledge: List<String> = handlePurchaseLock.withLock {
            val premium = purchases.any { it.purchaseState == Purchase.PurchaseState.PURCHASED }
            _isPremium.value = premium

            purchases.asSequence()
                .filter { it.purchaseState == Purchase.PurchaseState.PURCHASED && !it.isAcknowledged }
                .map { it.purchaseToken }
                .toList()
        }

        // Phase 2 : appels réseau hors lock
        for (token in toAcknowledge) {
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(token)
                .build()

            val result = billingClient.acknowledgePurchaseAwait(params)
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                _events.tryEmit("Erreur acknowledgment : ${result.debugMessage}")
            }
        }
    }

}