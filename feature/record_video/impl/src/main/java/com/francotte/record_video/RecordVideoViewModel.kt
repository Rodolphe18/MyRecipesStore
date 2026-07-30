package com.francotte.record_video

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.francotte.common.VideoRecorderResultBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecordVideoViewModel @Inject constructor(
    private val resultBus: VideoRecorderResultBus,
) : ViewModel() {

    private val _done = Channel<Unit>()
    val done = _done.receiveAsFlow()

    fun onVideoConfirmed(uri: Uri) = viewModelScope.launch {
        resultBus.emit(uri)
        _done.send(Unit)
    }
}
