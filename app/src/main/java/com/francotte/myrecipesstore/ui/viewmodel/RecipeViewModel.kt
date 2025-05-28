package com.francotte.myrecipesstore.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.francotte.myrecipesstore.model.MealResult
import com.francotte.myrecipesstore.repository.MealRepository
import com.francotte.myrecipesstore.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeViewModel @Inject constructor(
    private val repository: MealRepository
) : ViewModel() {

    private val _mealByName = MutableStateFlow<Resource<MealResult>>(Resource.Loading())
    val mealByName: StateFlow<Resource<MealResult>> = _mealByName.asStateFlow()

    private val _mealByFirstLetter = MutableStateFlow<Resource<MealResult>>(Resource.Loading())
    val mealByFirstLetter: StateFlow<Resource<MealResult>> = _mealByFirstLetter.asStateFlow()

    private val _mealDetail = MutableStateFlow<Resource<MealResult>>(Resource.Loading())
    val mealDetail: StateFlow<Resource<MealResult>> = _mealDetail.asStateFlow()

    private val _randomMeal = MutableStateFlow<Resource<MealResult>>(Resource.Loading())
    val randomMeal: StateFlow<Resource<MealResult>> = _randomMeal.asStateFlow()

    private val _allMealCategories = MutableStateFlow<Resource<MealResult>>(Resource.Loading())
    val allMealCategories: StateFlow<Resource<MealResult>> = _allMealCategories.asStateFlow()

    private val _allCategories = MutableStateFlow<Resource<MealResult>>(Resource.Loading())
    val allCategories: StateFlow<Resource<MealResult>> = _allCategories.asStateFlow()

    private val _allAreas = MutableStateFlow<Resource<MealResult>>(Resource.Loading())
    val allAreas: StateFlow<Resource<MealResult>> = _allAreas.asStateFlow()

    private val _allIngredients = MutableStateFlow<Resource<MealResult>>(Resource.Loading())
    val allIngredients: StateFlow<Resource<MealResult>> = _allIngredients.asStateFlow()

    private val _recipesByIngredient = MutableStateFlow<Resource<MealResult>>(Resource.Loading())
    val recipesByIngredient: StateFlow<Resource<MealResult>> = _recipesByIngredient.asStateFlow()

    private val _recipesByCategory = MutableStateFlow<Resource<MealResult>>(Resource.Loading())
    val recipesByCategory: StateFlow<Resource<MealResult>> = _recipesByCategory.asStateFlow()

    private val _recipesByArea = MutableStateFlow<Resource<MealResult>>(Resource.Loading())
    val recipesByArea: StateFlow<Resource<MealResult>> = _recipesByArea.asStateFlow()

    // Fonctions publiques pour déclencher les appels

    fun fetchMealByName(name: String) {
        viewModelScope.launch {
            repository.getMealByName(name).collect {
                _mealByName.value = it
            }
        }
    }

    fun fetchMealByFirstLetter(firstLetter: String) {
        viewModelScope.launch {
            repository.getMealByFirstLetter(firstLetter).collect {
                _mealByFirstLetter.value = it
            }
        }
    }

    fun fetchMealDetail(id: Long) {
        viewModelScope.launch {
            repository.getMealDetail(id).collect {
                _mealDetail.value = it
            }
        }
    }

    fun fetchRandomMeal() {
        viewModelScope.launch {
            repository.getRandomMeal().collect {
                _randomMeal.value = it
            }
        }
    }

    fun fetchAllMealCategories() {
        viewModelScope.launch {
            repository.getAllMealCategories().collect {
                _allMealCategories.value = it
            }
        }
    }

    fun fetchAllCategories() {
        viewModelScope.launch {
            repository.getAllCategories().collect {
                _allCategories.value = it
            }
        }
    }

    fun fetchAllAreas() {
        viewModelScope.launch {
            repository.getAllAreas().collect {
                _allAreas.value = it
            }
        }
    }

    fun fetchAllIngredients() {
        viewModelScope.launch {
            repository.getAllIngredients().collect {
                _allIngredients.value = it
            }
        }
    }

    fun fetchRecipesByMainIngredient(ingredient: String) {
        viewModelScope.launch {
            repository.getRecipesListByMainIngredient(ingredient).collect {
                _recipesByIngredient.value = it
            }
        }
    }

    fun fetchRecipesByCategory(category: String) {
        viewModelScope.launch {
            repository.getRecipesListByCategory(category).collect {
                _recipesByCategory.value = it
            }
        }
    }

    fun fetchRecipesByArea(area: String) {
        viewModelScope.launch {
            repository.getRecipesListByArea(area).collect {
                _recipesByArea.value = it
            }
        }
    }
}
