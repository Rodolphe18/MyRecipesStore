package com.francotte.home

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.francotte.testing.util.HomeTags
import com.francotte.testing.util.TestWindowSizes
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Locale
import java.util.TimeZone

class HomeScreenAndroidTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun stableEnv() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
        Locale.setDefault(Locale.FRANCE)
    }


    @Test
    fun showLoadingScreen() {
        composeRule.setContent {
            HomeScreen(
                latestRecipes= LatestRecipes.Loading,
                americanRecipes = AmericanRecipes.Loading,
                areasRecipes = AreasRecipes.Loading,
                englishRecipes = EnglishRecipes.Loading,
                windowSizeClass = TestWindowSizes.compactPhone(),
                isReloading = false,
                onReload = {},
                onOpenRecipe = {_,_,_-> },
                onOpenSection = {},
                onVideoButtonClick = {},
                onToggleFavorite = {_,_->},
                currentPage = 0,
                onCurrentPageChange = {_->}
            )
        }
        composeRule.onNodeWithTag(HomeTags.LOADING).assertIsDisplayed()
    }
}