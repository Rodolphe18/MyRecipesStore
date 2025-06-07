package com.francotte.myrecipesstore.ui.compose.section

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import com.francotte.myrecipesstore.model.AbstractRecipe
import com.francotte.myrecipesstore.model.LikeableRecipe
import kotlinx.serialization.Serializable

@Serializable
data class SectionRoute(val sectionType: SectionType)

fun NavController.navigateToSection(
    sectionType: SectionType,
    navOptions: NavOptionsBuilder.() -> Unit = {}
) {
    navigate(route = SectionRoute(sectionType)) {
        navOptions()
    }
}

fun NavGraphBuilder.sectionScreen(
    onBackClick: () -> Unit,
    onRecipeClick: (String) -> Unit,
    onToggleFavorite: (LikeableRecipe,Boolean) -> Unit,
    recipeDetailDestination: NavGraphBuilder.() -> Unit
) {
    composable<SectionRoute> {
        SectionRoute(
            onToggleFavorite = onToggleFavorite,
            onOpenRecipe ={ onRecipeClick(it.idMeal) },
            onBackClick = onBackClick
        )
    }
    recipeDetailDestination()
}

@Composable
fun SectionRoute(sectionViewModel: SectionViewModel= hiltViewModel(), onToggleFavorite:(LikeableRecipe,Boolean)-> Unit, onOpenRecipe: (AbstractRecipe)->Unit, onBackClick:()->Unit) {

    val uiState by sectionViewModel.sectionUiState.collectAsStateWithLifecycle()
    val sectionTitle by sectionViewModel.section.collectAsStateWithLifecycle()
    SectionScreen(uiState, sectionTitle.titleRes,{}, onToggleFavorite, onOpenRecipe, onBackClick)

}


