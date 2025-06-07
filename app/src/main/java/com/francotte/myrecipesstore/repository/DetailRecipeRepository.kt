package com.francotte.myrecipesstore.repository

import com.francotte.myrecipesstore.model.LikeableRecipe
import com.francotte.myrecipesstore.model.mapToLikeableRecipes
import com.francotte.myrecipesstore.network.api.RecipeApi
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
class DetailRecipeRepositoryImpl @Inject constructor(
    private val api: RecipeApi,
    private val userDataSource: UserDataSource
): DetailRecipeRepository {

   override fun getMealDetail(id: Long): Flow<Result<List<LikeableRecipe>>> =
       userDataSource.userData.flatMapLatest { userData ->
           flow {
               val result = api.getMealDetail(id)
               val likeable = result.meals.mapToLikeableRecipes(userData)
               emit(Result.success(likeable))
           }.catch { e ->
               emit(Result.failure(e))
           }
       }
}

interface DetailRecipeRepository {
    fun getMealDetail(id: Long): Flow<Result<List<LikeableRecipe>>>
}

