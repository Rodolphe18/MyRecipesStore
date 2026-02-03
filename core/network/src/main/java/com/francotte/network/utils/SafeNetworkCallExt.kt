package com.francotte.network.utils

import com.francotte.common.utils.DataResult
import com.francotte.common.utils.toNetworkError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext


suspend inline fun <T> safeNetworkCall(
    dispatcher: CoroutineDispatcher,
    crossinline call: suspend () -> T,
): DataResult<T> = withContext(dispatcher) {
    try {
        DataResult.Success(call())
    } catch (t: Throwable) {
        DataResult.Failure(t.toNetworkError())
    }
}
