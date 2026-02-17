package com.francotte.common.utils


sealed class AppError(open val cause: Throwable? = null) {

    data class NoInternet(override val cause: Throwable? = null) : AppError(cause)
    data class Timeout(override val cause: Throwable? = null) : AppError(cause)
    data class NetworkIO(override val cause: Throwable? = null) : AppError(cause)
    data class Unauthorized(override val cause: Throwable? = null) : AppError(cause)
    data class Http(val code: Int, val serverMessage: String? = null, override val cause: Throwable? = null) : AppError(cause)
    data class Serialization(override val cause: Throwable? = null) : AppError(cause)
    data class Unknown(override val cause: Throwable? = null) : AppError(cause)
}


fun AppError.userMessage(): String = when (this) {
    is AppError.NoInternet -> "No internet connection."
    is AppError.Timeout -> "The server is taking too long to respond."
    is AppError.NetworkIO -> "Network error. Please try again."
    is AppError.Unauthorized -> "Session expired. Please sign in again."
    is AppError.Http -> serverMessage ?: when {
        code in 500..599 -> "The server is currently unavailable."
        else -> "Request failed (${code})."
    }
    is AppError.Serialization -> "Error processing data."
    is AppError.Unknown -> "Something went wrong."
}


