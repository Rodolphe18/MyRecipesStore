package com.francotte.billing

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.ProductDetails
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PremiumPlan(val basePlanId: String) {
    Monthly("premium-monthly"),
    Quarterly("premium-quarterly"),
    Yearly("premium-yearly")
}

data class PremiumOfferUi(
    val plan: PremiumPlan,
    val title: String,          // ex. "1,99 € par mois"
    val formattedPrice: String, // prix renvoyé par Play
    val offerToken: String?     // pour lancer l’achat
)

data class PremiumUiState(
    val isLoading: Boolean = true,
    val isPremium: Boolean = false,
    val offers: List<PremiumOfferUi> = emptyList(),
    val message: String? = null
)

@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val premiumManager: PremiumManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PremiumUiState())
    val uiState: StateFlow<PremiumUiState> = _uiState.asStateFlow()

    init {
        premiumManager.startConnection()

        viewModelScope.launch {
            combine(
                premiumManager.productDetails,
                premiumManager.isPremium
            ) { productDetails, isPremium ->
                buildUiState(productDetails, isPremium)
            }.collect { _uiState.value = it }
        }

        viewModelScope.launch {
            premiumManager.events.collect { msg ->
                _uiState.update { it.copy(message = msg) }
            }
        }
    }

    private fun buildUiState(
        productDetails: ProductDetails?,
        isPremium: Boolean
    ): PremiumUiState {
        if (productDetails == null) {
            return PremiumUiState(
                isLoading = true,
                isPremium = isPremium,
                offers = emptyList()
            )
        }

        val offers = PremiumPlan.entries.map { plan ->
            val offerDetail = productDetails.subscriptionOfferDetails
                ?.firstOrNull { it.basePlanId == plan.basePlanId }

            val price = offerDetail
                ?.pricingPhases
                ?.pricingPhaseList
                ?.firstOrNull()
                ?.formattedPrice

            val label = when (plan) {
                PremiumPlan.Monthly   -> "${price ?: "1,99 €"} par mois"
                PremiumPlan.Quarterly -> "${price ?: "4,99 €"} par trimestre"
                PremiumPlan.Yearly    -> "${price ?: "9,99 €"} par an. Économiser 58 %"
            }

            PremiumOfferUi(
                plan = plan,
                title = label,
                formattedPrice = price ?: label,
                offerToken = offerDetail?.offerToken
            )
        }

        return PremiumUiState(
            isLoading = false,
            isPremium = isPremium,
            offers = offers
        )
    }

    fun onOfferClicked(activity: Activity, plan: PremiumPlan) {
        val productDetails = premiumManager.productDetails.value
        if (productDetails == null) {
            _uiState.update { it.copy(message = "Produit non chargé") }
            return
        }

        val offerDetail = productDetails.subscriptionOfferDetails
            ?.firstOrNull { it.basePlanId == plan.basePlanId }

        val token = offerDetail?.offerToken
        if (token == null) {
            _uiState.update { it.copy(message = "Offre indisponible pour ce plan") }
            return
        }

        premiumManager.launchBillingFlow(activity, productDetails, token)
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    override fun onCleared() {
        super.onCleared()
        premiumManager.endConnection()
    }
}