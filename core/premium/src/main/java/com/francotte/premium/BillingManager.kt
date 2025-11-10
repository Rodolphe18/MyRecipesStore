package com.francotte.premium

//import android.app.Activity
//import android.content.Context
//import com.android.billingclient.api.BillingClient
//import com.android.billingclient.api.BillingClientStateListener
//import com.android.billingclient.api.BillingFlowParams
//import com.android.billingclient.api.BillingResult
//import com.android.billingclient.api.QueryProductDetailsParams
//import dagger.Module
//import dagger.Provides
//import dagger.hilt.InstallIn
//import dagger.hilt.android.qualifiers.ApplicationContext
//import dagger.hilt.components.SingletonComponent
//import javax.inject.Singleton
//
//@Singleton
//class BillingManager(context: Context) {
//    private val billingClient = BillingClient.newBuilder(context)
//        .setListener { billingResult, purchases ->
//            // Traiter l'achat ici
//        }
//        .enablePendingPurchases()
//        .build()
//
//    fun startConnection(onConnected: () -> Unit) {
//        billingClient.startConnection(object : BillingClientStateListener {
//            override fun onBillingSetupFinished(result: BillingResult) {
//                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
//                    onConnected()
//                }
//            }
//
//            override fun onBillingServiceDisconnected() {}
//        })
//    }
//
//    fun launchPremiumPurchase(activity: Activity) {
//        val params = QueryProductDetailsParams.newBuilder()
//            .setProductList(
//                listOf(
//                    QueryProductDetailsParams.Product.newBuilder()
//                        .setProductId("premium_monthly") // L'ID défini dans Google Play Console
//                        .setProductType(BillingClient.ProductType.SUBS)
//                        .build()
//                )
//            )
//            .build()
//
//        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
//            val productDetails = productDetailsList.firstOrNull() ?: return@queryProductDetailsAsync
//
//            val offerToken = productDetails.subscriptionOfferDetails
//                ?.firstOrNull()
//                ?.offerToken ?: return@queryProductDetailsAsync
//
//            val billingFlowParams = BillingFlowParams.newBuilder()
//                .setProductDetailsParamsList(
//                    listOf(
//                        BillingFlowParams.ProductDetailsParams.newBuilder()
//                            .setProductDetails(productDetails)
//                            .setOfferToken(offerToken)
//                            .build()
//                    )
//                )
//                .build()
//
//            billingClient.launchBillingFlow(activity, billingFlowParams)
//        }
//    }
//}
//
//@Module
//@InstallIn(SingletonComponent::class)
//object BillingModule {
//
//    @Provides
//    @Singleton
//    fun provideBillingManager(@ApplicationContext context: Context): BillingManager = BillingManager(context)
//}
