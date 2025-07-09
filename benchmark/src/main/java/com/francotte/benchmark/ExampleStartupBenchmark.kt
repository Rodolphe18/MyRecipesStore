package com.francotte.benchmark


import android.Manifest.permission
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.TIRAMISU
import androidx.annotation.RequiresApi
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.UiObject2Condition
import androidx.test.uiautomator.Until
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class StartupBaselineProfileGenerator {

    @RequiresApi(Build.VERSION_CODES.P)
    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @RequiresApi(Build.VERSION_CODES.P)
    @Test
    fun startupProfile() = baselineProfileRule.collect(
        packageName = "com.francotte.myrecipesstore"
    ) {
        pressHome()
        startActivityAndAllowNotifications()

        device.wait(Until.findObject(By.desc("homeScreenReady")), 20_000)

        device.findObject(By.desc("VideoRecipeItem_0")).click()
        device.wait(
            Until.hasObject(By.res("com.francotte.myrecipesstore", "full_detail_screen")),
            5_000
        )
        device.pressBack()

        device.findObject(By.desc("SectionTitle_American")).click()
        device.wait(
            Until.hasObject(By.res("com.francotte.myrecipesstore", "full_section_screen")),
            5_000
        )
        device.pressBack()

        device.waitForIdle()
        val homeFeedList = device.wait(Until.findObject(By.desc("feed")), 10_000)
        device.flingElementDownUp(homeFeedList)
        device.waitForIdle()
    }
}

fun UiDevice.flingElementDownUp(element: UiObject2) {
    // Set some margin from the sides to prevent triggering system navigation
    element.setGestureMargin(displayWidth / 5)

    element.fling(Direction.DOWN)
    waitForIdle()
    element.fling(Direction.UP)
}

fun UiDevice.waitAndFindObject(selector: BySelector, timeout: Long): UiObject2 {
    if (!wait(Until.hasObject(selector), timeout)) {
        throw AssertionError("Element not found on screen in ${timeout}ms (selector=$selector)")
    }

    return findObject(selector)
}

fun untilHasChildren(
    childCount: Int = 1,
    op: HasChildrenOp = HasChildrenOp.AT_LEAST,
): UiObject2Condition<Boolean> = object : UiObject2Condition<Boolean>() {
    override fun apply(element: UiObject2): Boolean = when (op) {
        HasChildrenOp.AT_LEAST -> element.childCount >= childCount
        HasChildrenOp.EXACTLY -> element.childCount == childCount
        HasChildrenOp.AT_MOST -> element.childCount <= childCount
    }
}

enum class HasChildrenOp {
    AT_LEAST,
    EXACTLY,
    AT_MOST,
}

fun MacrobenchmarkScope.allowNotifications() {
    if (SDK_INT >= TIRAMISU) {
        val command = "pm grant $packageName ${permission.POST_NOTIFICATIONS}"
        device.executeShellCommand(command)
    }
}

fun MacrobenchmarkScope.startActivityAndAllowNotifications() {
    startActivityAndWait()
    allowNotifications()
}

