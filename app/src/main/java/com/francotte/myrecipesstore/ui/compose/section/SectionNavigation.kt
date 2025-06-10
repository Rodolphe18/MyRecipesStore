package com.francotte.myrecipesstore.ui.compose.section

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import com.francotte.myrecipesstore.domain.model.LikeableRecipe
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
    onRecipeClick: (String,String) -> Unit,
    onToggleFavorite: (LikeableRecipe, Boolean) -> Unit,
    recipeDetailDestination: NavGraphBuilder.() -> Unit
) {
    composable<SectionRoute> {
        SectionRoute(
            onToggleFavorite = onToggleFavorite,
            onOpenRecipe = { onRecipeClick(it.recipe.idMeal, it.recipe.strMeal) },
            onBackClick = onBackClick
        )
    }
    recipeDetailDestination()
}

@Composable
fun SectionRoute(sectionViewModel: SectionViewModel= hiltViewModel(), onToggleFavorite:(LikeableRecipe, Boolean)-> Unit, onOpenRecipe: (LikeableRecipe)->Unit, onBackClick:()->Unit) {

    val uiState by sectionViewModel.sectionUiState.collectAsStateWithLifecycle()
    val sectionTitle by sectionViewModel.section.collectAsStateWithLifecycle()
    SectionScreen(uiState, sectionTitle,{}, onToggleFavorite, onOpenRecipe, onBackClick)

}


