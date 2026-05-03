package com.github.dhakarpd.animeera.presentation.animeDetail

import com.github.dhakarpd.animeera.domain.model.AnimeFetchState
import com.github.dhakarpd.animeera.domain.model.AnimeWithDetail
import com.github.dhakarpd.animeera.domain.repo.AnimeDataRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AnimeDetailScreenViewModelTest {

    private lateinit var repository: AnimeDataRepository
    private lateinit var viewModel: AnimeDetailScreenViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    private val sampleAnime = AnimeWithDetail(
        animeId = 1,
        title = "Test Anime",
        numberOfEpisodes = 12,
        rating = 8.5,
        posterImageUrl = "url",
        trailerUrl = "trailer",
        cast = listOf("Actor 1"),
        genres = listOf("Action"),
        synopsis = "Synopsis"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        viewModel = AnimeDetailScreenViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getAnimeDetails updates fetchState and animeDetailState on StaleDataFetched`() = runTest {
        // Given
        val id = 1
        val state = AnimeFetchState.StaleDataFetched(sampleAnime)
        every { repository.fetchAnimeByID(id) } returns flowOf(state)

        // When
        viewModel.getAnimeDetails(id)

        // Then
        assertEquals(state, viewModel.fetchState.value)
        assertEquals(sampleAnime, viewModel.animeDetailState.value)
    }

    @Test
    fun `getAnimeDetails updates fetchState and animeDetailState on SyncSuccess`() = runTest {
        // Given
        val id = 1
        val state = AnimeFetchState.SyncSuccess(sampleAnime)
        every { repository.fetchAnimeByID(id) } returns flowOf(state)

        // When
        viewModel.getAnimeDetails(id)

        // Then
        assertEquals(state, viewModel.fetchState.value)
        assertEquals(sampleAnime, viewModel.animeDetailState.value)
    }

    @Test
    fun `getAnimeDetails updates fetchState but not animeDetailState on Loading`() = runTest {
        // Given
        val id = 1
        val state = AnimeFetchState.Loading
        every { repository.fetchAnimeByID(id) } returns flowOf(state)
        val initialState = viewModel.animeDetailState.value

        // When
        viewModel.getAnimeDetails(id)

        // Then
        assertEquals(state, viewModel.fetchState.value)
        assertEquals(initialState, viewModel.animeDetailState.value)
    }

    @Test
    fun `getAnimeDetails updates fetchState on SyncFailure`() = runTest {
        // Given
        val id = 1
        val state = AnimeFetchState.SyncFailure("Error")
        every { repository.fetchAnimeByID(id) } returns flowOf(state)
        val initialState = viewModel.animeDetailState.value

        // When
        viewModel.getAnimeDetails(id)

        // Then
        assertEquals(state, viewModel.fetchState.value)
        assertEquals(initialState, viewModel.animeDetailState.value)
    }

    @Test
    fun `getAnimeDetails updates fetchState and handles multiple emissions`() = runTest {
        // Given
        val id = 1
        val staleAnime = sampleAnime.copy(title = "Stale")
        val syncAnime = sampleAnime.copy(title = "Synced")
        
        val emissions = listOf(
            AnimeFetchState.Loading,
            AnimeFetchState.StaleDataFetched(staleAnime),
            AnimeFetchState.Syncing,
            AnimeFetchState.SyncSuccess(syncAnime)
        )
        
        every { repository.fetchAnimeByID(id) } returns emissions.asFlow()

        // When
        viewModel.getAnimeDetails(id)

        // Then
        assertEquals(AnimeFetchState.SyncSuccess(syncAnime), viewModel.fetchState.value)
        assertEquals(syncAnime, viewModel.animeDetailState.value)
    }
}
