package com.francotte.cmp

sealed interface ConsentState {
    data object Idle : ConsentState
    data object Loading : ConsentState

    data class Ready(val canRequestAds: Boolean) : ConsentState
}
