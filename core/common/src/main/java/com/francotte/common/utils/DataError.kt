package com.francotte.common.utils

import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

sealed class AppError(
    open val cause: Throwable? = null,
    open val debugMessage: String? = null,
    open val retryable: Boolean = false,
) {
    // Network
    data class NoInternet(override val cause: Throwable? = null, override val debugMessage: String? = null)
        : AppError(cause, debugMessage, retryable = true)

    data class Timeout(override val cause: Throwable? = null, override val debugMessage: String? = null)
        : AppError(cause, debugMessage, retryable = true)

    data class NetworkIO(override val cause: Throwable? = null, override val debugMessage: String? = null)
        : AppError(cause, debugMessage, retryable = true)

    // HTTP
    data class Unauthorized(override val cause: Throwable? = null, override val debugMessage: String? = null)
        : AppError(cause, debugMessage, retryable = false)

    data class Http(
        val code: Int,
        val serverMessage: String? = null,
        override val cause: Throwable? = null,
        override val debugMessage: String? = null,
        override val retryable: Boolean = code >= 500,
    ) : AppError(cause, debugMessage, retryable)

    // Parsing
    data class Serialization(override val cause: Throwable? = null, override val debugMessage: String? = null)
        : AppError(cause, debugMessage, retryable = false)

    // DB
    data class Database(override val cause: Throwable? = null, override val debugMessage: String? = null)
        : AppError(cause, debugMessage, retryable = true)

    // Fallback
    data class Unknown(override val cause: Throwable? = null, override val debugMessage: String? = null)
        : AppError(cause, debugMessage, retryable = false)
}



fun AppError.userMessage(): String = when (this) {
    is AppError.NoInternet -> "Pas de connexion Internet."
    is AppError.Timeout -> "Le serveur met trop de temps à répondre."
    is AppError.NetworkIO -> "Problème réseau. Réessaie."
    is AppError.Unauthorized -> "Session expirée. Merci de te reconnecter."
    is AppError.Http -> when {
        code in 500..599 -> "Serveur indisponible pour le moment."
        else -> "Requête impossible (${code})."
    }
    is AppError.Serialization -> "Erreur de traitement des données."
    is AppError.Database -> "Erreur locale. Réessaie."
    is AppError.Unknown -> "Une erreur est survenue."
}

fun Throwable.toNetworkError(): AppError = when (this) {
    is UnknownHostException -> AppError.NoInternet(this, message)
    is SocketTimeoutException -> AppError.Timeout(this, message)
    is IOException -> AppError.NetworkIO(this, message)
    is HttpException -> {
        val code = code()
        when (code) {
            401, 403 -> AppError.Unauthorized(this, message)
            else -> AppError.Http(code = code, cause = this, debugMessage = message)
        }
    }
    is SerializationException -> AppError.Serialization(this, message)
    else -> AppError.Unknown(this, message)
}
