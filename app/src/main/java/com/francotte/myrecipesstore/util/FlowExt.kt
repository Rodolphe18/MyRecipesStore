package com.francotte.myrecipesstore.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

inline fun <T> CoroutineScope.startCollection(flow: Flow<T>, crossinline block: suspend (T) -> Unit): Job {
    return launch {
        flow.collect { block(it) }
    }
}