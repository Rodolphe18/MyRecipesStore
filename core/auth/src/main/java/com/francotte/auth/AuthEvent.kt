package com.francotte.auth

sealed interface AuthEvent {
    data class LoginSuccess(val username: String) : AuthEvent
    data class RegisterSuccess(val username: String) : AuthEvent
    data object UpdateSuccess : AuthEvent
    data object PayloadTooLarge : AuthEvent
    data object UserAlreadyExists : AuthEvent
    data object LoginFailed : AuthEvent
    data object RegisterFailed : AuthEvent
    data object AccountDeleted : AuthEvent
    data class Disconnected(val wasConnected: Boolean) : AuthEvent
}
