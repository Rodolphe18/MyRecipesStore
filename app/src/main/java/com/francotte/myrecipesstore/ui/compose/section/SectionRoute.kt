package com.francotte.myrecipesstore.ui.compose.section

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import com.francotte.myrecipesstore.domain.model.LikeableRecipe
import com.francotte.myrecipesstore.util.ScreenCounter
import kotlinx.serialization.Serializable

@Serializable
data class SectionRoute(val sectionName: String)

fun NavController.navigateToSection(
    sectionType: String,
    navOptions: NavOptionsBuilder.() -> Unit = {}
) {
    navigate(route = SectionRoute(sectionType)) {
        navOptions()
    }
}

fun NavGraphBuilder.sectionScreen(
    onBackClick: () -> Unit,
    windowSizeClass: WindowSizeClass,
    onRecipeClick: (List<String>,Int,String) -> Unit,
    onToggleFavorite: (LikeableRecipe, Boolean) -> Unit
) {
    composable<SectionRoute> {
        SectionRoute(
            onToggleFavorite = onToggleFavorite,
            windowSizeClass = windowSizeClass,
            onOpenRecipe = { ids, index, title -> onRecipeClick(ids, index, title) },
            onBackClick = onBackClick
        )
    }
}

@Composable
fun SectionRoute(sectionViewModel: SectionViewModel= hiltViewModel(),windowSizeClass: WindowSizeClass, onToggleFavorite:(LikeableRecipe, Boolean)-> Unit, onOpenRecipe: (List<String>,Int,String) -> Unit, onBackClick:()->Unit) {

    val uiState by sectionViewModel.sectionUiState.collectAsStateWithLifecycle()
    val sectionTitle by sectionViewModel.section.collectAsStateWithLifecycle()
    SectionScreen(uiState, windowSizeClass, sectionTitle,{}, onToggleFavorite, onOpenRecipe, onBackClick)
    LaunchedEffect(Unit) {
        ScreenCounter.increment()
    }

}


