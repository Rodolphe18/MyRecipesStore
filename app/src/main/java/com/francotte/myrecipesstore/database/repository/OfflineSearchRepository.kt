package com.francotte.myrecipesstore.database.repository

import com.francotte.myrecipesstore.domain.model.AbstractCategory
import com.francotte.myrecipesstore.network.api.RecipeApi
import com.francotte.myrecipesstore.network.model.NetworkAbstractCategory
import com.francotte.myrecipesstore.network.model.NetworkArea
import com.francotte.myrecipesstore.network.model.NetworkIngredient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class OfflineSearchRepositoryImpl(private val network:RecipeApi):OfflineSearchRepository {
    override fun getAllIngredients(): Flow<List<NetworkIngredient>> = flow {
       emit(network.getAllIngredients().meals)
    }

    override fun getAllAreas(): Flow<List<NetworkArea>> = flow {
        emit(network.getAllAreas().meals)
    }

    override fun getAllCategories(): Flow<List<NetworkAbstractCategory>> = flow {
        emit(network.getAllCategories().categories)
    }

}


interface OfflineSearchRepository {
    fun getAllIngredients(): Flow<List<NetworkIngredient>>
    fun getAllAreas(): Flow<List<NetworkArea>>
    fun getAllCategories(): Flow<List<NetworkAbstractCategory>>

}