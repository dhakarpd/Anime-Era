package com.github.dhakarpd.animeera.presentation.animeList

import android.util.Log
import androidx.paging.PagingData
import androidx.paging.PagingDataEvent
import androidx.paging.PagingDataPresenter
import com.github.dhakarpd.animeera.data.local.entity.AnimeEntity
import com.github.dhakarpd.animeera.domain.model.Anime
import com.github.dhakarpd.animeera.domain.model.SyncStatus
import com.github.dhakarpd.animeera.domain.repo.AnimeDataRepository
import com.github.dhakarpd.animeera.domain.usecase.EnsureAnimeSyncUseCase
import com.github.dhakarpd.animeera.presentation.common.SnackbarController
import com.github.dhakarpd.animeera.presentation.common.SnackbarEvent
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AnimeListScreenViewModelTest {

    private lateinit var repository: AnimeDataRepository
    private lateinit var ensureAnimeSyncUseCase: EnsureAnimeSyncUseCase
    private lateinit var viewModel: AnimeListScreenViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        
        // Mock Log to avoid RuntimeException in tests
        mockkStatic(Log::class)
        every { Log.isLoggable(any(), any()) } returns false
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0

        repository = mockk()
        ensureAnimeSyncUseCase = mockk()
        
        // Default behavior to avoid init failure
        every { repository.getAnimePager() } returns flowOf(PagingData.empty())
        every { repository.fetchPopularAnimeList() } returns flowOf(SyncStatus.IDLE)
        every { ensureAnimeSyncUseCase.execute() } returns true
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
        Dispatchers.resetMain()
    }

    @Test
    fun `init with device online sets isDeviceOnline to true`() = runTest {
        // Given
        every { ensureAnimeSyncUseCase.execute() } returns true

        // When
        viewModel = AnimeListScreenViewModel(repository, ensureAnimeSyncUseCase)

        // Then
        assertTrue(viewModel.getDeviceOnlineStatus())
    }

    @Test
    fun `init with device offline sets isDeviceOnline to false and sends snackbar`() = runTest {
        // Given
        every { ensureAnimeSyncUseCase.execute() } returns false
        val snackbarEvents = mutableListOf<SnackbarEvent>()
        val job = launch(testDispatcher) {
            SnackbarController.events.collect { snackbarEvents.add(it) }
        }

        // When
        viewModel = AnimeListScreenViewModel(repository, ensureAnimeSyncUseCase)

        // Then
        assertFalse(viewModel.getDeviceOnlineStatus())
        assertEquals(1, snackbarEvents.size)
        assertEquals("Please check your internet connection", snackbarEvents[0].message)
        job.cancel()
    }

    @Test
    fun `fetchAnime updates syncStatus and sends snackbar on NO_INTERNET_CONNECTION`() = runTest {
        // Given
        val syncFlow = MutableSharedFlow<SyncStatus>(replay = 1)
        every { repository.fetchPopularAnimeList() } returns syncFlow
        
        val snackbarEvents = mutableListOf<SnackbarEvent>()
        val job = launch(testDispatcher) {
            SnackbarController.events.collect { snackbarEvents.add(it) }
        }

        // When
        viewModel = AnimeListScreenViewModel(repository, ensureAnimeSyncUseCase)
        syncFlow.tryEmit(SyncStatus.NO_INTERNET_CONNECTION)

        // Then
        assertEquals(SyncStatus.NO_INTERNET_CONNECTION, viewModel.syncStatus.value)
        assertEquals(1, snackbarEvents.size)
        assertEquals("Please check your internet connection", snackbarEvents[0].message)
        job.cancel()
    }

    @Test
    fun `fetchAnime updates syncStatus and sends snackbar on ERROR`() = runTest {
        // Given
        val syncFlow = MutableSharedFlow<SyncStatus>(replay = 1)
        every { repository.fetchPopularAnimeList() } returns syncFlow
        
        val snackbarEvents = mutableListOf<SnackbarEvent>()
        val job = launch(testDispatcher) {
            SnackbarController.events.collect { snackbarEvents.add(it) }
        }

        // When
        viewModel = AnimeListScreenViewModel(repository, ensureAnimeSyncUseCase)
        syncFlow.tryEmit(SyncStatus.ERROR)

        // Then
        assertEquals(SyncStatus.ERROR, viewModel.syncStatus.value)
        assertEquals(1, snackbarEvents.size)
        assertEquals("Something went wrong", snackbarEvents[0].message)
        job.cancel()
    }

    @Test
    fun `fetchAnime updates syncStatus on SUCCESS and does not send snackbar`() = runTest {
        // Given
        val syncFlow = MutableSharedFlow<SyncStatus>(replay = 1)
        every { repository.fetchPopularAnimeList() } returns syncFlow
        
        val snackbarEvents = mutableListOf<SnackbarEvent>()
        val job = launch(testDispatcher) {
            SnackbarController.events.collect { snackbarEvents.add(it) }
        }

        // When
        viewModel = AnimeListScreenViewModel(repository, ensureAnimeSyncUseCase)
        syncFlow.tryEmit(SyncStatus.SUCCESS)

        // Then
        assertEquals(SyncStatus.SUCCESS, viewModel.syncStatus.value)
        assertTrue(snackbarEvents.isEmpty())
        job.cancel()
    }

    @Test
    fun `fetchAnime updates syncStatus on SYNCING`() = runTest {
        // Given
        val syncFlow = MutableSharedFlow<SyncStatus>(replay = 1)
        every { repository.fetchPopularAnimeList() } returns syncFlow

        // When
        viewModel = AnimeListScreenViewModel(repository, ensureAnimeSyncUseCase)
        syncFlow.tryEmit(SyncStatus.SYNCING)

        // Then
        assertEquals(SyncStatus.SYNCING, viewModel.syncStatus.value)
    }

    @Test
    fun `animePager transforms AnimeEntity to Anime correctly`() = runTest {
        // Given
        val entity = AnimeEntity(
            id = 1,
            title = "Test Title",
            numberOfEpisodes = 12,
            rating = 9.5,
            posterImageUrl = "http://poster.url",
            timestamp = 0L,
            isActive = true
        )

        every { repository.getAnimePager() } returns flowOf(PagingData.from(listOf(entity)))

        // When
        viewModel = AnimeListScreenViewModel(repository, ensureAnimeSyncUseCase)

        // Use a PagingDataPresenter to extract items from PagingData and trigger mapping
        val presenter = object : PagingDataPresenter<Anime>(testDispatcher) {
            override suspend fun presentPagingDataEvent(event: PagingDataEvent<Anime>) {
                // Not needed for this test as we use snapshot()
            }
        }

        val job = launch(testDispatcher) {
            viewModel.animePager.collect { pagingData ->
                presenter.collectFrom(pagingData)
            }
        }

        // Then
        val snapshot = presenter.snapshot()
        assertTrue(snapshot.isNotEmpty())
        val anime = snapshot[0]
        assertEquals(entity.id, anime?.id)
        assertEquals(entity.title, anime?.title)
        assertEquals(entity.numberOfEpisodes, anime?.numberOfEpisodes)
        assertEquals(entity.rating, anime?.rating)
        assertEquals(entity.posterImageUrl, anime?.posterImageUrl)

        job.cancel()
    }
}
