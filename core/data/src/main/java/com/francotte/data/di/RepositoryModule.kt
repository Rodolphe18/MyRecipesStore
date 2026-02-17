package com.francotte.data.di

import com.francotte.common.extension.Dispatcher
import com.francotte.common.extension.FoodDispatchers
import com.francotte.data.repository.CategoriesRepository
import com.francotte.data.repository.CompositeUserFullRecipeRepository
import com.francotte.data.repository.CompositeUserHomeRepository
import com.francotte.data.repository.DefaultSearchContentsRepository
import com.francotte.data.repository.OfflineFirstCategoriesRepositoryImpl
import com.francotte.data.repository.OfflineFirstFavoritesRepository
import com.francotte.data.repository.OfflineFirstFavoritesRepositoryImpl
import com.francotte.data.repository.OfflineFirstFullRecipeRepository
import com.francotte.data.repository.OfflineFirstFullRecipeRepositoryImpl
import com.francotte.data.repository.OfflineFirstHomeRepository
import com.francotte.data.repository.OfflineFirstIngredientsAndAreasRepositoryImpl
import com.francotte.data.repository.RecipesRepository
import com.francotte.data.repository.IngredientsAndAreasRepository
import com.francotte.data.repository.SearchContentsRepository
import com.francotte.data.repository.UserFullRecipeRepository
import com.francotte.data.repository.UserHomeRepository
import com.francotte.database.dao.AreaDao
import com.francotte.database.dao.FullCategoryDao
import com.francotte.database.dao.FullRecipeDao
import com.francotte.database.dao.IngredientDao
import com.francotte.database.dao.LightRecipeDao
import com.francotte.database.dao.fts.AreaFtsDao
import com.francotte.database.dao.fts.CategoryFtsDao
import com.francotte.database.dao.fts.IngredientFtsDao
import com.francotte.database.dao.fts.RecipeFtsDao
import com.francotte.datastore.UserDataRepository
import com.francotte.network.api.FavoriteApi
import com.francotte.network.api.RecipeApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Singleton
    @Provides
    fun provideIngredientsAndAreasRepository(
        homeRepository: OfflineFirstHomeRepository,
        api: RecipeApi,
        ingredientDao: IngredientDao,
        areaDao: AreaDao,
        userDataRepository: UserDataRepository,
    ): IngredientsAndAreasRepository = OfflineFirstIngredientsAndAreasRepositoryImpl(homeRepository, api, ingredientDao,areaDao,userDataRepository)


    @Singleton
    @Provides
    fun provideSearchContentsRepository(
        lightRecipeDao: LightRecipeDao,
        recipeFtsDao: RecipeFtsDao,
        categoryFtsDao: CategoryFtsDao,
        areaFtsDao: AreaFtsDao,
        ingredientFtsDao: IngredientFtsDao,
        categoriesDao: FullCategoryDao,
        areasDao: AreaDao,
        ingredientDao: IngredientDao,
    ) : SearchContentsRepository = DefaultSearchContentsRepository(lightRecipeDao,recipeFtsDao,categoryFtsDao,areaFtsDao,ingredientFtsDao,categoriesDao,areasDao,ingredientDao)

    @Singleton
    @Provides
    fun provideLikeableFullRecipeRepository(
        offlineFullRecipeData: OfflineFirstFullRecipeRepository,
        userDataRepository: UserDataRepository,
    ): UserFullRecipeRepository = CompositeUserFullRecipeRepository(offlineFullRecipeData, userDataRepository)

    @Singleton
    @Provides
    fun provideOfflineFirstFullRecipeRepository(
        api: RecipeApi,
        dao: FullRecipeDao,
    ): OfflineFirstFullRecipeRepository = OfflineFirstFullRecipeRepositoryImpl(api, dao)

    @Singleton
    @Provides
    fun provideOfflineFirstFavoritesRepository(
        dao: FullRecipeDao,
        favoriteApi: FavoriteApi,
        recipeApi: RecipeApi,
        userDataRepository: UserDataRepository,
    ): OfflineFirstFavoritesRepository = OfflineFirstFavoritesRepositoryImpl(dao, favoriteApi,recipeApi, userDataRepository)

    @Singleton
    @Provides
    fun provideOfflineFirstHomeRepository(
        api: RecipeApi,
        lightRecipeDao: LightRecipeDao,
        fullRecipeDao: FullRecipeDao,
    ): RecipesRepository = OfflineFirstHomeRepository(lightRecipeDao, fullRecipeDao, api)

    @Singleton
    @Provides
    fun provideHomeRepository(
        homeRepository: OfflineFirstHomeRepository,
        userDataRepository: UserDataRepository,
    ): UserHomeRepository = CompositeUserHomeRepository(homeRepository, userDataRepository)

    @Singleton
    @Provides
    fun provideCategoriesRepository(
        api: RecipeApi,
        dao: FullCategoryDao,
    ): CategoriesRepository = OfflineFirstCategoriesRepositoryImpl(api, dao)


}
