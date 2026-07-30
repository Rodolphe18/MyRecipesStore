package com.francotte.common

import android.net.Uri
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoRecorderResultBus @Inject constructor() {
    private val _results = MutableSharedFlow<Uri>(extraBufferCapacity = 1)
    val results: SharedFlow<Uri> = _results.asSharedFlow()
    suspend fun emit(uri: Uri) {
        _results.emit(uri)
    }
}
