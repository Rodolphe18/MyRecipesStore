package com.francotte.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.ProductDetails
import com.francotte.billing.BillingManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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
    val title: String,
    val formattedPrice: String,
    val offerToken: String?
)

data class PremiumUiState(
    val isLoading: Boolean = true,
    val isPremium: Boolean = false,
    val offers: List<PremiumOfferUi> = emptyList(),
    val message: String? = null
)

sealed interface PremiumEffect {
    data class LaunchPurchase(val offerToken: String) : PremiumEffect
}

@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val billingManager: BillingManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PremiumUiState())
    val uiState: StateFlow<PremiumUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<PremiumEffect>(extraBufferCapacity = 1)
    val effects = _effects.asSharedFlow()

    init {

        viewModelScope.launch {
            combine(
                billingManager.productDetails,
                billingManager.isPremium
            ) { productDetails, isPremium ->
                buildUiState(productDetails, isPremium)
            }.collect { _uiState.value = it }
        }

        viewModelScope.launch {
            billingManager.events.collect { msg ->
                _uiState.update { it.copy(message = msg) }
            }
        }
    }

    fun onOfferClicked(plan: PremiumPlan) {
        val productDetails = billingManager.productDetails.value ?: run {
            _uiState.update { it.copy(message = "Produit non chargé") }
            return
        }

        val offerDetail = productDetails.subscriptionOfferDetails
            ?.firstOrNull { it.basePlanId == plan.basePlanId }

        val token = offerDetail?.offerToken ?: run {
            _uiState.update { it.copy(message = "Offre indisponible pour ce plan") }
            return
        }

        _effects.tryEmit(PremiumEffect.LaunchPurchase(token))
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
                PremiumPlan.Monthly   -> "${price ?: "—"} par mois"
                PremiumPlan.Quarterly -> "${price ?: "—"} par trimestre"
                PremiumPlan.Yearly    -> "${price ?: "—"} par an"
            }

            PremiumOfferUi(
                plan = plan,
                title = label,
                formattedPrice = price ?: "—",
                offerToken = offerDetail?.offerToken
            )
        }

        return PremiumUiState(
            isLoading = false,
            isPremium = isPremium,
            offers = offers
        )
    }
}