package com.francotte.add_recipe

import android.net.Uri
import com.francotte.common.VideoRecorderResultBus
import com.francotte.data.interfaces.FavoritesRepository
import com.francotte.data.interfaces.UserDataRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddRecipeBusCollectionTest {

    private val dispatcher = StandardTestDispatcher()

    @Before fun setup() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `bus emission sets videoUri`() = runTest(dispatcher.scheduler) {
        val bus = VideoRecorderResultBus()
        val repo = mockk<FavoritesRepository>(relaxed = true)
        val userRepo = mockk<UserDataRepository>(relaxed = true)
        every { userRepo.userData } returns emptyFlow()

        val vm = AddRecipeViewModel(repo, userRepo, bus)
        val uri = mockk<Uri>()
        advanceUntilIdle()
        bus.emit(uri)
        advanceUntilIdle()

        assertEquals(uri, vm.state.value.videoUri)
    }
}
