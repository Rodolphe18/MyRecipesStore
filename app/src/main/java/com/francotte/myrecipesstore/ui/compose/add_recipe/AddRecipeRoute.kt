package com.francotte.myrecipesstore.ui.compose.add_recipe

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.francotte.myrecipesstore.network.model.Ingredient
import com.francotte.myrecipesstore.util.ScreenCounter

const val ADD_ROUTE = "add_route"

fun NavController.navigateToAddRecipeScreen(navOptions: NavOptions? = null) {
    this.navigate(ADD_ROUTE, navOptions)
}

fun NavGraphBuilder.addRecipeScreen(onSubmit: (title: String, ingredients: List<Ingredient>, instructions: String, images: List<Uri>) -> Unit) {
    composable(route = ADD_ROUTE) {
        AddRoute(onSubmit)
    }
}

@Composable
fun AddRoute(onSubmit: (title: String, ingredients: List<Ingredient>, instructions: String, images: List<Uri>) -> Unit) {
    AddRecipeScreen(onSubmit)
    ScreenCounter.increment()
}