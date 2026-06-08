package com.francotte.inapp_update

sealed interface InAppUpdateDomainState {
    data object NotAvailable : InAppUpdateDomainState
    data object InProgress : InAppUpdateDomainState
    data object Downloaded : InAppUpdateDomainState
    data class Available(val type: UpdateType) : InAppUpdateDomainState
}
