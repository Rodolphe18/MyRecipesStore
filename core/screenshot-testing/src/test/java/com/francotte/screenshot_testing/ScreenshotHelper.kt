
@file:Suppress("TestFunctionName")

package com.francotte.screenshot_testing

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.captureRoboImage
import org.robolectric.RuntimeEnvironment

data class TestDevice(val name: String, val widthDp: Int, val heightDp: Int, val dpi: Int)
val Phone480 = TestDevice("phone", 640, 360, 480)
val PhoneLandscape480 = TestDevice("phone_land", 960, 540, 480)

val DefaultRoborazziOptions = RoborazziOptions()

fun <A : ComponentActivity> AndroidComposeTestRule<ActivityScenarioRule<A>, A>.captureForDevice(
    device: TestDevice,
    fileName: String,
    content: @Composable () -> Unit
) {
    RuntimeEnvironment.setQualifiers("w${device.widthDp}dp-h${device.heightDp}dp-${device.dpi}dpi")
    setContent {
        CompositionLocalProvider(LocalInspectionMode provides true) {
            content()
        }
    }
    onRoot().captureRoboImage(
        "src/test/screenshots/${fileName}_${device.name}.png",
        roborazziOptions = DefaultRoborazziOptions
    )
}
