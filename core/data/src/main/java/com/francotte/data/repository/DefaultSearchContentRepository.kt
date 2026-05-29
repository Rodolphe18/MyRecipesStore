package com.francotte.data.repository

import android.util.Log
import com.francotte.common.extension.Dispatcher
import com.francotte.common.extension.FoodDispatchers
import com.francotte.data.mapper.entity.asExternalModel
import com.francotte.database.dao.AreaDao
import com.francotte.database.dao.FullCategoryDao
import com.francotte.database.dao.IngredientDao
import com.francotte.database.dao.LightRecipeDao
import com.francotte.database.dao.fts.AreaFtsDao
import com.francotte.database.dao.fts.CategoryFtsDao
import com.francotte.database.dao.fts.IngredientFtsDao
import com.francotte.database.dao.fts.RecipeFtsDao
import com.francotte.database.model.asFtsEntity
import com.francotte.model.SearchResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject


class DefaultSearchContentsRepository @Inject constructor(
    private val lightRecipeDao: LightRecipeDao,
    private val recipeFtsDao: RecipeFtsDao,
    private val categoryFtsDao: CategoryFtsDao,
    private val areaFtsDao: AreaFtsDao,
    private val ingredientFtsDao: IngredientFtsDao,
    private val categoriesDao: FullCategoryDao,
    private val areasDao: AreaDao,
    private val ingredientDao: IngredientDao
) : SearchContentsRepository {


    private val warmupMutex = kotlinx.coroutines.sync.Mutex()

    override suspend fun ensureFtsReady(minCount: Int) {
        warmupMutex.withLock {
           val ftsTablesAreReady = searchContentsIsReady().first()
            if (!ftsTablesAreReady) populateFtsData()
        }
    }

    override suspend fun populateFtsData() {
        recipeFtsDao.clear()
        categoryFtsDao.clear()
        areaFtsDao.clear()
        ingredientFtsDao.clear()

        recipeFtsDao.insertAll(lightRecipeDao.getAllOnce().map { it.asFtsEntity() })
        categoryFtsDao.insertAll(categoriesDao.getAllCategoriesOnce().map { it.asFtsEntity() })
        areaFtsDao.insertAll(areasDao.getAllAreasOnce().map { it.asFtsEntity() })
        ingredientFtsDao.insertAll(ingredientDao.getAllIngredientsOnce().map { it.asFtsEntity() })
    }

    override fun searchContents(searchQuery: String): Flow<SearchResult> {
        val ftsQuery = buildFtsQuery(searchQuery)

        // Noms directs (pas besoin de re-fetch)
        val categoriesFlow = categoryFtsDao.searchCategoryNames(ftsQuery, limit = 20)
            .distinctUntilChanged()

        val areasFlow = areaFtsDao.searchAreaNames(ftsQuery, limit = 20)
            .distinctUntilChanged()

        val ingredientsFlow = ingredientFtsDao.searchIngredientNames(ftsQuery, limit = 20)
            .distinctUntilChanged()

        // IDs recipes -> entities -> external model
        val recipeIdsFlow = recipeFtsDao.searchRecipeIds(ftsQuery, limit = 100)

        val recipesFlow = recipeIdsFlow
            .mapLatest { it.toSet() }
            .distinctUntilChanged()
            .flatMapLatest { ids ->
                lightRecipeDao.observeByIds(
                    useFilterIds = ids.isNotEmpty(),
                    filterIds = ids,
                )
            }

        return combine(
            recipesFlow,
            categoriesFlow,
            areasFlow,
            ingredientsFlow
        ) { recipes, cats, areas, ing ->
            Log.d("debug_search_repo_categories", cats.toString())
            Log.d("debug_search_repo_ing", ing.toString())
            Log.d("debug_search_repo_recipes", recipes.toString())

            SearchResult(
                categories = cats,
                areas = areas,
                ingredients = ing,
                lightRecipes = recipes.map { it.asExternalModel() },
            )
        }
    }

    override fun searchContentsIsReady(): Flow<Boolean> =
        combine(
            recipeFtsDao.getCount(),
            categoryFtsDao.getCount(),
            areaFtsDao.getCount(),
            ingredientFtsDao.getCount(),
        ) { recipesCount, categoriesCount, areasCount, ingredientsCount ->
            recipesCount > 0 && categoriesCount > 0 && areasCount > 0 && ingredientsCount > 0
        }

}

private fun buildFtsQuery(raw: String): String {
    val q = raw.trim()
    if (q.isEmpty()) return "\"\"" // MATCH vide => rien (safe)
    // Simple escape pour guillemets
    val escaped = q.replace("\"", "\"\"")
    // Prefix match performant. (tu peux passer à *query* si tu veux)
    return "*$escaped*"
}
