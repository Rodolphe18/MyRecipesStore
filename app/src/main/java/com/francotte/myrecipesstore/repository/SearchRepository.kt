package com.francotte.myrecipesstore.repository

import com.francotte.myrecipesstore.database.repository.OfflineFirstHomeRepository
import com.francotte.myrecipesstore.datastore.UserDataSource
import com.francotte.myrecipesstore.domain.model.LikeableRecipe
import com.francotte.myrecipesstore.domain.model.mapToLikeableLightRecipes
import com.francotte.myrecipesstore.network.api.RecipeApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
//
//class SearchRepository(
//    private val api: RecipeApi,
//    private val userDataSource: UserDataSource
//) {
//    fun observeMealByName(name: String): Flow<Result<List<LikeableRecipe>>> = flow {
//        combine(
//            userDataSource.userData,
//            api.getMealByName(name).meals
//        ) { userData, topRecipes ->
//            try {
//                val likeable = topRecipes.mapToLikeableLightRecipes(userData)
//                Result.success(likeable)
//            } catch (e: Exception) {
//                Result.failure(e)
//            }
//        }
//    }
//}