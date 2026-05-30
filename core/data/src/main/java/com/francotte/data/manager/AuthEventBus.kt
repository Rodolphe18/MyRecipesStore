package com.francotte.data.manager

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthEventBus @Inject constructor() {
    private val _snackBarMessage = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val snackBarMessage: SharedFlow<String> = _snackBarMessage
    fun emit(message: String) { _snackBarMessage.tryEmit(message) }
}
