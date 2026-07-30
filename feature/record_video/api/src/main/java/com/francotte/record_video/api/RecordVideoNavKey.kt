package com.francotte.record_video.api

import androidx.navigation3.runtime.NavKey
import com.francotte.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
object RecordVideoNavKey : NavKey

fun Navigator.navigateToRecordVideo() {
    navigate(RecordVideoNavKey)
}
