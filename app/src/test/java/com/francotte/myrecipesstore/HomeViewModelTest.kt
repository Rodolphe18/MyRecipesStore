package com.francotte.myrecipesstore

import com.francotte.myrecipesstore.domain.model.LightRecipe
import com.francotte.myrecipesstore.domain.model.LikeableRecipe
import com.francotte.myrecipesstore.domain.model.Recipe
import com.francotte.myrecipesstore.repository.LikeableLightRecipesRepository
import com.francotte.myrecipesstore.ui.compose.home.AmericanRecipes
import com.francotte.myrecipesstore.ui.compose.home.AreasRecipes
import com.francotte.myrecipesstore.ui.compose.home.EnglishRecipes
import com.francotte.myrecipesstore.ui.compose.home.HomeViewModel
import com.francotte.myrecipesstore.ui.compose.home.LatestRecipes
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class HomeViewModelTest {

        @get:Rule
        val mainDispatcherRule = MainDispatcherRule()

        private lateinit var repository: LikeableLightRecipesRepository
        private lateinit var viewModel: HomeViewModel

        private val fakeRecipe = LikeableRecipe(
            recipe = LightRecipe(
                idMeal = "123",
                strMeal = "Test Meal",
                strMealThumb = "http://image.url",
            ),
            isFavorite = false
        )

        @Test
        fun `latestRecipes emits Loading then Success`() = runTest {
            repository = mockk {
                every { observeLatestRecipes() } returns flowOf(Result.success(listOf(fakeRecipe)))
                every { observeAmericanAreaRecipes() } returns flowOf(Result.success(emptyList()))
            }

            viewModel = HomeViewModel(repository)

            val result = viewModel.latestRecipes.first()
            assert(result is LatestRecipes.Success)
            assertEquals("Test Meal", (result as LatestRecipes.Success).latestRecipes.first().recipe.strMeal)
        }

        @Test
        fun `latestRecipes emits Error when repository returns failure`() = runTest {
            repository = mockk {
                every { observeLatestRecipes() } returns flowOf(Result.failure(Exception("Network error")))
                every { observeAmericanAreaRecipes() } returns flowOf(Result.success(emptyList()))
            }

            viewModel = HomeViewModel(repository)

            val result = viewModel.latestRecipes.first()
            assert(result is LatestRecipes.Error)
        }

        @Test
        fun `americanRecipes emits Success`() = runTest {
            repository = mockk {
                every { observeAmericanAreaRecipes() } returns flowOf(Result.success(listOf(fakeRecipe)))
                // Required because ViewModel also calls this
                every { observeLatestRecipes() } returns flowOf(Result.success(emptyList()))
            }

            viewModel = HomeViewModel(repository)

            val result = viewModel.americanRecipes.first()
            assert(result is AmericanRecipes.Success)
            assertEquals("Test Meal", (result as AmericanRecipes.Success).americanRecipes.first().recipe.strMeal)
        }

        @Test
        fun `americanRecipes emits Error`() = runTest {
            repository = mockk {
                every { observeAmericanAreaRecipes() } returns flowOf(Result.failure(Exception("Error")))
                // Required because ViewModel also calls this
                every { observeLatestRecipes() } returns flowOf(Result.success(emptyList()))
            }

            viewModel = HomeViewModel(repository)

            val result = viewModel.americanRecipes.first()
            assert(result is AmericanRecipes.Error)
        }

        @Test
        fun `loadMore emits English and Areas Success`() = runTest {
            repository = mockk {
                every { observeLatestRecipes() } returns flowOf(Result.success(emptyList()))
                every { observeAmericanAreaRecipes() } returns flowOf(Result.success(emptyList()))
                every { observeEnglishAreaRecipes() } returns flowOf(Result.success(listOf(fakeRecipe)))
                every { observeFoodAreaSections() } returns flowOf(Result.success(mapOf("Europe" to listOf(fakeRecipe))))
            }

            viewModel = HomeViewModel(repository)
            viewModel.loadMore()

            assert(viewModel.englishRecipes.value is EnglishRecipes.Success)
            assert((viewModel.englishRecipes.value as EnglishRecipes.Success).englishRecipes.first().recipe.strMeal == "Test Meal")

            assert(viewModel.areasRecipes.value is AreasRecipes.Success)
            assert((viewModel.areasRecipes.value as AreasRecipes.Success).areasRecipes["Europe"]!!.first().recipe.strMeal == "Test Meal")
        }

        @Test
        fun `loadMore emits English and Areas Error`() = runTest {
            repository = mockk {
                every { observeLatestRecipes() } returns flowOf(Result.success(emptyList()))
                every { observeAmericanAreaRecipes() } returns flowOf(Result.success(emptyList()))
                every { observeEnglishAreaRecipes() } returns flowOf(Result.failure(Exception("error")))
                every { observeFoodAreaSections() } returns flowOf(Result.failure(Exception("error")))
            }

            viewModel = HomeViewModel(repository)
            viewModel.loadMore()

            assert(viewModel.englishRecipes.value is EnglishRecipes.Error)
            assert(viewModel.areasRecipes.value is AreasRecipes.Error)
        }
    }
