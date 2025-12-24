package com.francotte.home


import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.francotte.designsystem.component.LocalBillingController
import com.francotte.screenshot_testing.Phone480
import com.francotte.screenshot_testing.RobolectricTestWindowSizes
import com.francotte.screenshot_testing.captureForDevice
import com.francotte.testing.FakeBillingController
import com.francotte.testing.util.HomeTags
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.LooperMode
import java.util.*
import kotlin.intArrayOf

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
@LooperMode(LooperMode.Mode.PAUSED)
class VideosScreenRobolectricTests {

    @get:Rule
    val composeRule: AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity> =
        createAndroidComposeRule()

    private val fakeBillingController = FakeBillingController()

    @Before
    fun stableEnv() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
        Locale.setDefault(Locale.FRANCE)
    }

    @Test
    fun showLoadingScreen_andScreenshot() {

        val homeScreen: @Composable () -> Unit = {
            HomeScreen(
                latestRecipes = LatestRecipes.Loading,
                americanRecipes = AmericanRecipes.Loading,
                areasRecipes = AreasRecipes.Loading,
                englishRecipes = EnglishRecipes.Loading,
                windowSizeClass = RobolectricTestWindowSizes.compactPhone(),
                isReloading = false,
                onReload = {},
                onOpenRecipe = { _, _, _ -> },
                onOpenSection = {},
                onVideoButtonClick = {},
                onToggleFavorite = { _, _ -> },
                currentPage = 0,
                onCurrentPageChange = { _ -> }
            )
        }

        composeRule.setContent {
            CompositionLocalProvider(LocalBillingController provides fakeBillingController) {
                homeScreen()
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(HomeTags.LOADING).assertIsDisplayed()

        composeRule.captureForDevice(
            device = Phone480,
            fileName = "HomeScreen_Loading"
        ) {
            homeScreen()
        }
    }


}