package com.github.dhakarpd.animeera.data.repo

import android.content.Context
import androidx.paging.PagingSource
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.Operation
import androidx.work.WorkManager
import com.github.dhakarpd.animeera.data.local.AnimeDatabase
import com.github.dhakarpd.animeera.data.local.dao.AnimeDao
import com.github.dhakarpd.animeera.data.local.entity.AnimeEntity
import com.github.dhakarpd.animeera.data.local.entity.AnimeWithDetailsEntity
import com.github.dhakarpd.animeera.data.network.model.AnimeDataDto
import com.github.dhakarpd.animeera.data.network.model.AnimeDetailedDataDto
import com.github.dhakarpd.animeera.data.network.model.AnimeDto
import com.github.dhakarpd.animeera.data.network.model.Trailer
import com.github.dhakarpd.animeera.data.network.model.Genre
import com.github.dhakarpd.animeera.data.network.service.ApiService
import com.github.dhakarpd.animeera.domain.model.AnimeFetchState
import com.github.dhakarpd.animeera.domain.model.SyncStatus
import com.github.dhakarpd.animeera.util.Constants
import com.github.dhakarpd.animeera.util.InternetConnectivityChecker
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONException
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.net.SocketTimeoutException

@OptIn(ExperimentalCoroutinesApi::class)
class AnimeDataRepoImplTest {

    private lateinit var apiService: ApiService
    private lateinit var animeDatabase: AnimeDatabase
    private lateinit var animeDao: AnimeDao
    private lateinit var context: Context
    private lateinit var internetConnectivityChecker: InternetConnectivityChecker
    private lateinit var workManager: WorkManager
    private lateinit var repository: AnimeDataRepoImpl

    @Before
    fun setUp() {
        apiService = mockk()
        animeDatabase = mockk()
        animeDao = mockk()
        context = mockk()
        internetConnectivityChecker = mockk()
        workManager = mockk()

        every { animeDatabase.animeDao } returns animeDao
        mockkStatic(WorkManager::class)
        every { WorkManager.getInstance(context) } returns workManager

        repository = AnimeDataRepoImpl(
            apiService = apiService,
            animeDatabase = animeDatabase,
            context = context,
            internetConnectivityChecker = internetConnectivityChecker,
        )
    }

    @After
    fun tearDown() {
        unmockkStatic(WorkManager::class)
    }

    @Test
    fun `syncPopularAnimeList returns true on success and updates dao`() = runTest {
        // Arrange
        val animeDto = mockk<AnimeDto>(relaxed = true) {
            every { mal_id } returns 1
            every { title } returns "Test"
        }
        val animeDataDto = AnimeDataDto(data = listOf(animeDto), pagination = mockk())
        every { internetConnectivityChecker.isConnectedToInternet() } returns true
        coEvery { apiService.getPopularAnime() } returns Response.success(animeDataDto)
        coJustRun { animeDao.upsertAnime(any()) }

        // Act
        val result = repository.syncPopularAnimeList()

        // Assert
        assertTrue(result)
        coVerify { animeDao.upsertAnime(any()) }
    }

    @Test
    fun `syncPopularAnimeList returns false when no internet`() = runTest {
        // Arrange
        every { internetConnectivityChecker.isConnectedToInternet() } returns false

        // Act
        val result = repository.syncPopularAnimeList()

        // Assert
        assertFalse(result)
        coVerify(exactly = 0) { apiService.getPopularAnime() }
    }

    @Test
    fun `syncPopularAnimeList returns false on api failure`() = runTest {
        // Arrange
        val errorJson = "{\"message\":\"Not Found\"}"
        val errorResponseBody = errorJson.toResponseBody()
        every { internetConnectivityChecker.isConnectedToInternet() } returns true
        coEvery { apiService.getPopularAnime() } returns Response.error(404, errorResponseBody)

        // Act
        val result = repository.syncPopularAnimeList()

        // Assert
        assertFalse(result)
        coVerify(exactly = 0) { animeDao.upsertAnime(any()) }
    }

    @Test
    fun `syncPopularAnimeList returns false on exception`() = runTest {
        // Arrange
        every { internetConnectivityChecker.isConnectedToInternet() } returns true
        coEvery { apiService.getPopularAnime() } throws JSONException("Expected double but integer found")

        // Act
        val result = repository.syncPopularAnimeList()

        // Assert
        assertFalse(result)
        coVerify(exactly = 0) { animeDao.upsertAnime(any()) }
    }

    @Test
    fun `convertAnimeDtoListToEntityList handles null fields correctly`() = runTest {
        // Arrange
        val animeDto = mockk<AnimeDto>(relaxed = true) {
            every { mal_id } returns null
            every { title } returns null
            every { episodes } returns null
            every { score } returns null
            every { images } returns null
        }
        val animeDataDto = AnimeDataDto(data = listOf(animeDto), pagination = mockk())
        every { internetConnectivityChecker.isConnectedToInternet() } returns true
        coEvery { apiService.getPopularAnime() } returns Response.success(animeDataDto)
        
        val slot = slot<List<AnimeEntity>>()
        coEvery { animeDao.upsertAnime(capture(slot)) } returns Unit

        // Act
        repository.syncPopularAnimeList()

        // Assert
        val captured = slot.captured[0]
        assertEquals(0, captured.id)
        assertEquals("", captured.title)
        assertEquals(0, captured.numberOfEpisodes)
        assertEquals(0.0, captured.rating!!, 0.0)
        assertEquals("", captured.posterImageUrl)
    }

    @Test
    fun `fetchPopularAnimeList emits syncing and success on internet success`() = runTest {
        // Arrange
        val animeDto = mockk<AnimeDto>(relaxed = true)
        val animeDataDto = AnimeDataDto(data = listOf(animeDto), pagination = mockk())
        every { internetConnectivityChecker.isConnectedToInternet() } returns true
        coEvery { apiService.getPopularAnimeByPage(page = 1) } returns Response.success(animeDataDto)
        coEvery { animeDao.upsertAnime(any()) } returns Unit

        // Act
        val results = repository.fetchPopularAnimeList().toList()

        // Assert
        assertEquals(listOf(SyncStatus.SYNCING, SyncStatus.SUCCESS), results)
        coVerify { animeDao.upsertAnime(any()) }
    }
    @Test
    fun `fetchPopularAnimeList emits syncing and error on api failure`() = runTest {
        // Arrange
        val errorJson = "{\"message\":\"Not Found\"}"
        val errorResponseBody = errorJson.toResponseBody()
        every { internetConnectivityChecker.isConnectedToInternet() } returns true
        coEvery { apiService.getPopularAnimeByPage(page = 1) } returns Response.error(404, errorResponseBody)

        // Act
        val results = repository.fetchPopularAnimeList().toList()

        // Assert
        assertEquals(listOf(SyncStatus.SYNCING, SyncStatus.ERROR), results)
        coVerify(exactly = 0) { animeDao.upsertAnime(any()) }
    }
    @Test
    fun `fetchPopularAnimeList emits syncing and error on exception`() = runTest {
        // Arrange
        every { internetConnectivityChecker.isConnectedToInternet() } returns true
        coEvery { apiService.getPopularAnimeByPage(page = 1) } throws SocketTimeoutException("Unable to resolve host")

        // Act
        val results = repository.fetchPopularAnimeList().toList()

        // Assert
        assertEquals(listOf(SyncStatus.SYNCING, SyncStatus.ERROR), results)
        coVerify(exactly = 0) { animeDao.upsertAnime(any()) }
    }

    @Test
    fun `fetchPopularAnimeList emits syncing and error when body is null`() = runTest {
        // Arrange
        every { internetConnectivityChecker.isConnectedToInternet() } returns true
        coEvery { apiService.getPopularAnimeByPage(page = 1) } returns Response.success(null)

        // Act
        val results = repository.fetchPopularAnimeList().toList()

        // Assert
        assertEquals(listOf(SyncStatus.SYNCING, SyncStatus.ERROR), results)
    }

    @Test
    fun `fetchPopularAnimeList emits syncing, no internet and enqueues work on no connection`() = runTest {
        // Arrange
        every { internetConnectivityChecker.isConnectedToInternet() } returns false
        every { workManager.enqueueUniqueWork(any<String>(), any<ExistingWorkPolicy>(), any<OneTimeWorkRequest>()) } returns mockk<Operation>()

        // Act
        val results = repository.fetchPopularAnimeList().toList()

        // Assert
        assertEquals(listOf(SyncStatus.SYNCING, SyncStatus.NO_INTERNET_CONNECTION), results)
        coVerify {
            workManager.enqueueUniqueWork(
                Constants.WORK_REQUEST_NAME,
                ExistingWorkPolicy.REPLACE,
                any<OneTimeWorkRequest>()
            )
        }
    }

    @Test
    fun `fetchAnimeByID emits loading, stale data, syncing and success`() = runTest {
        // Arrange
        val animeId = 1
        val genre = mockk<Genre> { every { name } returns "Action" }
        val animeDto = mockk<AnimeDto>(relaxed = true) {
            every { mal_id } returns animeId
            every { title } returns "Test Title"
            every { genres } returns listOf(genre)
            every { trailer } returns mockk<Trailer> { every { url } returns "http://trailer" }
        }
        val animeDetailedDataDto = AnimeDetailedDataDto(data = animeDto)
        val staleEntity = mockk<AnimeWithDetailsEntity>(relaxed = true) {
            every { toDomain() } returns mockk(relaxed = true)
        }

        every { internetConnectivityChecker.isConnectedToInternet() } returns true
        coEvery { animeDao.getAnimeWithDetailsById(animeId) } returns staleEntity
        coEvery { apiService.getAnimeById(animeId) } returns Response.success(animeDetailedDataDto)
        coEvery { animeDao.upsertAnimeWithDetails(any()) } returns Unit

        // Act
        val results = repository.fetchAnimeByID(animeId).toList()

        // Assert
        assertTrue(results[0] is AnimeFetchState.Loading)
        assertTrue(results[1] is AnimeFetchState.StaleDataFetched)
        assertTrue(results[2] is AnimeFetchState.Syncing)
        assertTrue(results[3] is AnimeFetchState.SyncSuccess)
        coVerify { animeDao.upsertAnimeWithDetails(any()) }
    }

    @Test
    fun `fetchAnimeByID emits loading, syncing and no internet when connection is lost and no stale data`() = runTest {
        // Arrange
        val animeId = 1
        every { internetConnectivityChecker.isConnectedToInternet() } returns false
        coEvery { animeDao.getAnimeWithDetailsById(animeId) } returns null

        // Act
        val results = repository.fetchAnimeByID(animeId).toList()

        // Assert
        val expected = listOf(
            AnimeFetchState.Loading,
            AnimeFetchState.Syncing,
            AnimeFetchState.DataFetchFailure,
            AnimeFetchState.NoInternetAvailable
        )
        assertEquals(expected, results)
    }

    @Test
    fun `fetchAnimeByID emits loading, stale failure, syncing, data failure and no internet on dao error`() = runTest {
        // Arrange
        val animeId = 1
        val errorMessage = "Database Error"
        coEvery { animeDao.getAnimeWithDetailsById(animeId) } throws Exception(errorMessage)
        every { internetConnectivityChecker.isConnectedToInternet() } returns false

        // Act
        val results = repository.fetchAnimeByID(animeId).toList()

        // Assert
        val expected = listOf(
            AnimeFetchState.Loading,
            AnimeFetchState.StaleDataFetchFailure(errorMessage),
            AnimeFetchState.Syncing,
            AnimeFetchState.DataFetchFailure,
            AnimeFetchState.NoInternetAvailable
        )
        assertEquals(expected, results)
    }

    @Test
    fun `fetchAnimeByID emits loading, syncing and data failure when api response is unsuccessful`() = runTest {
        // Arrange
        val animeId = 1
        coEvery { animeDao.getAnimeWithDetailsById(animeId) } returns null
        every { internetConnectivityChecker.isConnectedToInternet() } returns true
        coEvery { apiService.getAnimeById(animeId) } returns Response.error(404, "Not Found".toResponseBody())

        // Act
        val results = repository.fetchAnimeByID(animeId).toList()

        // Assert
        val expected = listOf(
            AnimeFetchState.Loading,
            AnimeFetchState.Syncing,
            AnimeFetchState.DataFetchFailure
        )
        assertEquals(expected, results)
    }

    @Test
    fun `fetchAnimeByID emits loading, syncing and data failure when api response body is null`() = runTest {
        // Arrange
        val animeId = 1
        coEvery { animeDao.getAnimeWithDetailsById(animeId) } returns null
        every { internetConnectivityChecker.isConnectedToInternet() } returns true
        coEvery { apiService.getAnimeById(animeId) } returns Response.success(null)

        // Act
        val results = repository.fetchAnimeByID(animeId).toList()

        // Assert
        val expected = listOf(
            AnimeFetchState.Loading,
            AnimeFetchState.Syncing,
            AnimeFetchState.DataFetchFailure
        )
        assertEquals(expected, results)
    }

    @Test
    fun `fetchAnimeByID emits loading, syncing and sync failure when api throws exception`() = runTest {
        // Arrange
        val animeId = 1
        val errorMessage = "Network timeout"
        coEvery { animeDao.getAnimeWithDetailsById(animeId) } returns null
        every { internetConnectivityChecker.isConnectedToInternet() } returns true
        coEvery { apiService.getAnimeById(animeId) } throws Exception(errorMessage)

        // Act
        val results = repository.fetchAnimeByID(animeId).toList()

        // Assert
        val expected = listOf(
            AnimeFetchState.Loading,
            AnimeFetchState.Syncing,
            AnimeFetchState.SyncFailure(errorMessage)
        )
        assertEquals(expected, results)
    }

    @Test
    fun `fetchAnimeByID skips stale data emission when none found`() = runTest {
        // Arrange
        val animeId = 1
        coEvery { animeDao.getAnimeWithDetailsById(animeId) } returns null
        every { internetConnectivityChecker.isConnectedToInternet() } returns false

        // Act
        val results = repository.fetchAnimeByID(animeId).toList()

        // Assert
        assertTrue(results[0] is AnimeFetchState.Loading)
        assertTrue(results[1] is AnimeFetchState.Syncing)
        assertFalse(results.any { it is AnimeFetchState.StaleDataFetched })
    }

    @Test
    fun `getAnimePager returns paging data flow`() = runTest {
        // Arrange
        val pagingSource = mockk<PagingSource<Int, AnimeEntity>>()
        every { animeDao.getAllAnime() } returns pagingSource

        // Act
        val result = repository.getAnimePager()

        // Assert
        assertNotNull(result)
    }

    @Test
    fun `toAnimeWithDetailsEntity handles partial null fields and filtering correctly`() = runTest {
        // Arrange
        val animeId = 1
        val genreWithNullName = mockk<Genre> { every { name } returns null }
        val genreWithValidName = mockk<Genre> { every { name } returns "Adventure" }
        
        val animeDto = mockk<AnimeDto>(relaxed = true) {
            every { mal_id } returns animeId
            every { title } returns null
            every { episodes } returns null
            every { score } returns null
            every { synopsis } returns "Some synopsis"
            every { images } returns mockk {
                every { jpg } returns mockk {
                    every { image_url } returns null
                }
            }
            every { trailer } returns mockk {
                every { url } returns null
            }
            every { genres } returns listOf(genreWithNullName, genreWithValidName)
        }
        val animeDetailedDataDto = AnimeDetailedDataDto(data = animeDto)

        every { internetConnectivityChecker.isConnectedToInternet() } returns true
        coEvery { animeDao.getAnimeWithDetailsById(animeId) } returns null
        coEvery { apiService.getAnimeById(animeId) } returns Response.success(animeDetailedDataDto)
        
        val slot = slot<AnimeWithDetailsEntity>()
        coEvery { animeDao.upsertAnimeWithDetails(capture(slot)) } returns Unit

        // Act
        repository.fetchAnimeByID(animeId).toList()

        // Assert
        val captured = slot.captured
        assertEquals(animeId, captured.id)
        assertEquals("", captured.title)
        assertEquals(0, captured.numberOfEpisodes)
        assertEquals(0.0, captured.rating!!, 0.0)
        assertEquals("", captured.posterImageUrl)
        assertEquals("Some synopsis", captured.synopsis)
        assertEquals(null, captured.trailerUrl)
        assertEquals(listOf("Adventure"), captured.genres)
    }
}
