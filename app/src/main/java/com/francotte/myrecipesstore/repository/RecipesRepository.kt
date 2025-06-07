package com.francotte.myrecipesstore.repository

import com.francotte.myrecipesstore.model.LikeableRecipe
import com.francotte.myrecipesstore.network.api.RecipeApi
import com.francotte.myrecipesstore.model.mapToLikeableRecipes
import com.francotte.myrecipesstore.user.UserDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class RecipesRepositoryImpl @Inject constructor(
    private val api: RecipeApi,
    private val userDataSource: UserDataSource
) : RecipesRepository {


    override fun observeLatestRecipes(): Flow<Result<List<LikeableRecipe>>> =
        userDataSource.userData.flatMapLatest { userData ->
            flow {
                val result = api.getLatestMeals()
                val likeable = result.meals.mapToLikeableRecipes(userData)
                emit(Result.success(likeable))
            }.catch { e ->
                emit(Result.failure(e))
            }
        }

    override fun observeTopRecipes(): Flow<Result<List<LikeableRecipe>>> =
        userDataSource.userData.flatMapLatest { userData ->
            flow {
                val result = api.getRandomMealsSelection()
                val likeable = result.meals.mapToLikeableRecipes(userData)
                emit(Result.success(likeable))
            }.catch { e ->
                emit(Result.failure(e))
            }
        }

    override fun getRecipesListByCategory(category: String): Flow<Result<List<LikeableRecipe>>> =
        userDataSource.userData.flatMapLatest { userData ->
            flow {
                val result = api.getRecipesListByCategory(category)
                val likeable = result.meals.mapToLikeableRecipes(userData)
                emit(Result.success(likeable))
            }.catch { e ->
                emit(Result.failure(e))
            }
        }

    override fun getMealByName(name: String): Flow<Result<List<LikeableRecipe>>> =
        userDataSource.userData.flatMapLatest { userData ->
            flow {
                val result = api.getMealByName(name)
                val likeable = result.meals.mapToLikeableRecipes(userData)
                emit(Result.success(likeable))
            }.catch { e ->
                emit(Result.failure(e))
            }
        }


    override fun getRecipesListByMainIngredient(ingredient: String): Flow<Result<List<LikeableRecipe>>> =
        userDataSource.userData.flatMapLatest { userData ->
            flow {
                val result = api.getRecipesListByMainIngredient(ingredient)
                val likeable = result.meals.mapToLikeableRecipes(userData)
                emit(Result.success(likeable))
            }.catch { e ->
                emit(Result.failure(e))
            }
        }



    override fun getRecipesListByArea(area: String): Flow<Result<List<LikeableRecipe>>> =
        userDataSource.userData.flatMapLatest { userData ->
            flow {
                val result = api.getRecipesListByArea(area)
                val likeable = result.meals.mapToLikeableRecipes(userData)
                emit(Result.success(likeable))
            }.catch { e ->
                emit(Result.failure(e))
            }
        }

}


interface RecipesRepository {
    fun observeLatestRecipes(): Flow<Result<List<LikeableRecipe>>>
    fun observeTopRecipes(): Flow<Result<List<LikeableRecipe>>>
    fun getMealByName(name: String): Flow<Result<List<LikeableRecipe>>>
    fun getRecipesListByMainIngredient(ingredient: String): Flow<Result<List<LikeableRecipe>>>
    fun getRecipesListByCategory(category: String): Flow<Result<List<LikeableRecipe>>>
    fun getRecipesListByArea(area: String): Flow<Result<List<LikeableRecipe>>>
}
