package com.francotte.network.utils

import com.francotte.common.utils.AppError
import com.francotte.common.utils.DataResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.coroutines.cancellation.CancellationException


suspend inline fun <T> safeNetworkCall(
    dispatcher: CoroutineDispatcher,
    crossinline call: suspend () -> T,
): DataResult<T> = withContext(dispatcher) {
    try {
        DataResult.Success(call())
    } catch (e: Exception) {
        DataResult.Failure(e.toNetworkError())
    }
}

fun Throwable.toNetworkError(): AppError = when (this) {
    is CancellationException -> throw this
    is UnknownHostException -> AppError.NoInternet(this)
    is ConnectException -> AppError.NoInternet(this)
    is SocketTimeoutException -> AppError.Timeout(this)
    is SerializationException -> AppError.Serialization(this)
    is HttpException -> {
        val code = code()
        when (code) {
            401 -> AppError.Unauthorized(this)
            else -> AppError.Http(code = code, cause = this)
        }
    }
    is IOException -> AppError.NetworkIO(this)
    else -> AppError.Unknown(this)
}
