package com.francotte.home

import androidx.lifecycle.ViewModel
import com.francotte.common.LaunchCounterManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class LaunchCounterViewModel @Inject constructor(launchCounterManager: LaunchCounterManager) : ViewModel() {

    private val _launchCount = MutableStateFlow(launchCounterManager.getLaunchCount())
    val launchCount = _launchCount.asStateFlow()

}