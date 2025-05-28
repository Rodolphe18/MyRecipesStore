package com.francotte.myrecipesstore.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow


inline fun <T> safeApiCall(crossinline call: suspend () -> T): Flow<Resource<T>> = flow {
    emit(Resource.Loading())
    emit(Resource.Success(call()))
}.catch { e ->
    emit(Resource.Error(e))
}