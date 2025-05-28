package com.francotte.myrecipesstore.ui.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.francotte.myrecipesstore.model.Meal
import com.francotte.myrecipesstore.model.MealResult
import com.francotte.myrecipesstore.ui.viewmodel.RecipeViewModel
import com.francotte.myrecipesstore.util.Resource

@Composable
fun MealSearchScreen(viewModel: RecipeViewModel) {
    val mealState by viewModel.mealByName.collectAsState()
    val randomMealState by viewModel.randomMeal.collectAsState()
    val recipesByCategory by viewModel.recipesByCategory.collectAsState()

    when (recipesByCategory) {
        is Resource.Loading -> {
            CircularProgressIndicator()
        }
        is Resource.Success -> {
            // Affiche ta liste de repas ici
            when (val meals = (recipesByCategory as Resource.Success).data) {
                is MealResult.Single -> {
                    (meals.meal as? Meal)?.let {
                        MealDetailScreen(it)
                    }
                }
                is MealResult.Multiple -> {
                    MealGrid(meals.meals, {})
                }
                is MealResult.Empty -> Text("La liste est vide")

            }
        }
        is Resource.Error -> {
            val error = (mealState as Resource.Error).throwable
            Text("Erreur: ${error.message}")
        }
    }

    Column {
        Spacer(Modifier.height(42.dp))
        Row {
            Button(modifier = Modifier.width(30.dp), onClick = { viewModel.fetchRandomMeal() }) {
                Text("Rechercher")
            }
            Button(modifier = Modifier.width(30.dp),onClick = { viewModel.fetchRecipesByCategory("seafood") }) {
                Text("Rechercher Koshari")
            }
            Button(modifier = Modifier.width(30.dp),onClick = { viewModel.fetchRandomMeal() }) {
                Text("Rechercher Koshari")
            }
        }

    }

}