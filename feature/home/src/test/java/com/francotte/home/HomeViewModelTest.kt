package com.francotte.home

import com.francotte.model.LikeableRecipe
import com.francotte.model.TestRecipe
import com.francotte.testing.FakeHomeRepository
import com.francotte.testing.util.MainDispatcherRule
import com.francotte.ui.HomeSyncer
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: FakeHomeRepository

    private lateinit var homeSyncer: HomeSyncer
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        repository = FakeHomeRepository()
        viewModel = HomeViewModel(repository,homeSyncer)
    }

    @Test
    fun stateIsInitiallyLoading() = runTest {
        assertEquals(LatestRecipes.Loading, viewModel.latestRecipes.value)
        assertEquals(AmericanRecipes.Loading, viewModel.americanRecipes.value)
        assertEquals(EnglishRecipes.Loading, viewModel.englishRecipes.value)
        assertEquals(AreasRecipes.Loading, viewModel.areasRecipes.value)
    }

    @Test
    fun latestRecipesIsSuccessWhenRepositoryEmitsSuccess() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.latestRecipes.collect() }

        val sample = listOf(
            LikeableRecipe(recipe = TestRecipe(idMeal = "1", strMeal = "A"), isFavorite = false),
            LikeableRecipe(recipe = TestRecipe(idMeal = "2", strMeal = "B"), isFavorite = true),
        )

        repository.sendLatestRecipes(Result.success(sample))
        advanceUntilIdle()

        assertEquals(LatestRecipes.Success(sample), viewModel.latestRecipes.value)
    }


    @Test
    fun englishRecipesIsErrorWhenRepositoryEmitsFailure() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.englishRecipes.collect() }

        repository.sendEnglishRecipes(Result.failure(Throwable("boom")))
        advanceUntilIdle()

        val state = viewModel.englishRecipes.value
        assertTrue(state is EnglishRecipes.Error)
        assertEquals("Error", (state as EnglishRecipes.Error).toString())
    }

    @Test
    fun americanRecipes_isSuccess_whenRepositoryEmitsSuccess() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.americanRecipes.collect() }

        val sample = listOf(
            LikeableRecipe(recipe = TestRecipe(idMeal = "10", strMeal = "Burger"), isFavorite = false)
        )

        repository.sendAmericanRecipes(Result.success(sample))
        advanceUntilIdle()

        assertEquals(AmericanRecipes.Success(sample), viewModel.americanRecipes.value)
    }

    @Test
    fun areasRecipes_isSuccess_whenRepositoryEmitsSuccessMap() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.areasRecipes.collect() }

        val map = mapOf(
            "French" to listOf(LikeableRecipe(TestRecipe("100", "Ratatouille"), isFavorite = false)),
            "Italian" to listOf(LikeableRecipe(TestRecipe("200", "Pizza"), isFavorite = true)),
        )

        repository.sendAreasSections(Result.success(map))
        advanceUntilIdle()

        assertEquals(AreasRecipes.Success(map), viewModel.areasRecipes.value)
    }

    @Test
    fun reload_togglesIsReloadingBackToFalse_andStillUpdatesStreams() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.latestRecipes.collect() }

        // 1) premier état
        val first = listOf(LikeableRecipe(TestRecipe("1", "A"), false))
        repository.sendLatestRecipes(Result.success(first))
        advanceUntilIdle()
        assertEquals(LatestRecipes.Success(first), viewModel.latestRecipes.value)

        // 2) reload
        viewModel.refresh()
        advanceUntilIdle()

        // isReloading repasse à false à la fin
        assertFalse(viewModel.isReloading.value)

        // 3) nouvelle donnée après reload (le flatMapLatest est relancé)
        val second = listOf(LikeableRecipe(TestRecipe("2", "B"), true))
        repository.sendLatestRecipes(Result.success(second))
        advanceUntilIdle()

        assertEquals(LatestRecipes.Success(second), viewModel.latestRecipes.value)
    }
}