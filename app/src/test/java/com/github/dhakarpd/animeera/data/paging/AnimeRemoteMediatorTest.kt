package com.github.dhakarpd.animeera.data.paging

import androidx.paging.*
import androidx.room.withTransaction
import com.github.dhakarpd.animeera.data.local.AnimeDatabase
import com.github.dhakarpd.animeera.data.local.dao.AnimeDao
import com.github.dhakarpd.animeera.data.local.entity.AnimeEntity
import com.github.dhakarpd.animeera.data.network.model.AnimeDataDto
import com.github.dhakarpd.animeera.data.network.model.AnimeDto
import com.github.dhakarpd.animeera.data.network.model.Pagination
import com.github.dhakarpd.animeera.data.network.service.ApiService
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalPagingApi::class, ExperimentalCoroutinesApi::class)
class AnimeRemoteMediatorTest {

    private lateinit var apiService: ApiService
    private lateinit var animeDatabase: AnimeDatabase
    private lateinit var animeDao: AnimeDao
    private lateinit var remoteMediator: AnimeRemoteMediator

    @Before
    fun setUp() {
        apiService = mockk()
        animeDatabase = mockk()
        animeDao = mockk(relaxed = true)
        
        every { animeDatabase.animeDao } returns animeDao

        /**
         * This code block is a workaround to handle one of the trickiest parts of unit testing Room: mocking the withTransaction extension function.
         * Here is a breakdown of each part:
         * 1. mockkStatic("androidx.room.RoomDatabaseKt")
         * In Kotlin, extension functions (like RoomDatabase.withTransaction) are compiled into static methods in a generated Java class.
         * • Room defines this extension in a file called RoomDatabase.kt.
         * • At runtime, the class is named androidx.room.RoomDatabaseKt.
         * • mockkStatic tells MockK to "intercept" calls to any static methods in that class so we can provide fake behavior.
         * 2. coEvery { animeDatabase.withTransaction<Any>(any()) }
         * • coEvery: This is used because withTransaction is a suspend function.
         * • We are telling MockK: "Whenever someone calls withTransaction on my mocked animeDatabase, do the following..."
         * 3. it.invocation.args[1] (The most important part)
         * When an extension function is compiled to a static method, the arguments are rearranged:
         * • args[0]: Is the receiver object (the animeDatabase instance).
         * • args[1]: Is the lambda block you passed inside the curly braces { ... }.
         * In your RemoteMediator, you likely have code like this:
         * Kotlin
         * database.withTransaction {
         *     dao.clearAll()
         *     dao.insertAll(data)
         * }
         * The code it.invocation.args[1] grabs that block of code between the { }.
         * 4. block()
         * • Since we are in a unit test, there is no real database and no real transaction logic.
         * • If we don't mock this, the code will crash because Room tries to look for a real database connection.
         * • By calling block(), we are telling the test: "Just ignore the transaction overhead and execute the code inside the curly braces immediately."
         * **/
        // Mock withTransaction
        mockkStatic("androidx.room.RoomDatabaseKt")
        coEvery { animeDatabase.withTransaction<Any>(any()) } coAnswers {
            @Suppress("UNCHECKED_CAST")
            val block = it.invocation.args[1] as suspend () -> Any
            block()
        }

        remoteMediator = AnimeRemoteMediator(apiService, animeDatabase)
    }

    @Test
    fun `refresh load returns Success when more data is present`() = runTest {
        // Given
        val animeDataDto = AnimeDataDto(
            data = emptyList(),
            pagination = Pagination(current_page = 1, has_next_page = true, items = null, last_visible_page = 10)
        )
        coEvery { apiService.getPopularAnimeByPage(1) } returns Response.success(animeDataDto)

        // When
        val pagingState = PagingState<Int, AnimeEntity>(
            pages = listOf(),
            anchorPosition = null,
            config = PagingConfig(pageSize = 20),
            leadingPlaceholderCount = 0
        )
        val result = remoteMediator.load(LoadType.REFRESH, pagingState)

        // Then
        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertFalse((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
        
        coVerify { animeDao.clearAllAnime() }
        coVerify { animeDao.upsertAnime(any()) }
    }

    @Test
    fun `refresh load returns Success and endOfPaginationReached when no more data`() = runTest {
        // Given
        val animeDataDto = AnimeDataDto(
            data = emptyList(),
            pagination = Pagination(current_page = 1, has_next_page = false, items = null, last_visible_page = 1)
        )
        coEvery { apiService.getPopularAnimeByPage(1) } returns Response.success(animeDataDto)

        // When
        val pagingState = PagingState<Int, AnimeEntity>(
            pages = listOf(),
            anchorPosition = null,
            config = PagingConfig(pageSize = 20),
            leadingPlaceholderCount = 0
        )
        val result = remoteMediator.load(LoadType.REFRESH, pagingState)

        // Then
        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertTrue((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
    }

    @Test
    fun `append load returns Success and increments page`() = runTest {
        // Given
        // Initial refresh to set currentPage to 1
        val refreshDto = AnimeDataDto(
            data = emptyList(),
            pagination = Pagination(current_page = 1, has_next_page = true, items = null, last_visible_page = 10)
        )
        coEvery { apiService.getPopularAnimeByPage(1) } returns Response.success(refreshDto)
        
        val pagingState = PagingState<Int, AnimeEntity>(
            pages = listOf(),
            anchorPosition = null,
            config = PagingConfig(pageSize = 20),
            leadingPlaceholderCount = 0
        )
        remoteMediator.load(LoadType.REFRESH, pagingState)

        val appendDto = AnimeDataDto(
            data = emptyList(),
            pagination = Pagination(current_page = 2, has_next_page = true, items = null, last_visible_page = 10)
        )
        coEvery { apiService.getPopularAnimeByPage(2) } returns Response.success(appendDto)

        // When
        val result = remoteMediator.load(LoadType.APPEND, pagingState)

        // Then
        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertFalse((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
        
        coVerify(exactly = 1) { animeDao.clearAllAnime() }
        coVerify { apiService.getPopularAnimeByPage(2) }
    }

    @Test
    fun `load returns Error when IOException occurs and database is empty`() = runTest {
        // Given
        coEvery { apiService.getPopularAnimeByPage(any()) } throws java.io.IOException()
        coEvery { animeDao.getAnimeCount() } returns 0

        // When
        val pagingState = PagingState<Int, AnimeEntity>(
            pages = listOf(),
            anchorPosition = null,
            config = PagingConfig(pageSize = 20),
            leadingPlaceholderCount = 0
        )
        val result = remoteMediator.load(LoadType.REFRESH, pagingState)

        // Then
        assertTrue(result is RemoteMediator.MediatorResult.Error)
    }

    @Test
    fun `load returns Success when IOException occurs and database is not empty`() = runTest {
        // Given
        coEvery { apiService.getPopularAnimeByPage(any()) } throws java.io.IOException()
        coEvery { animeDao.getAnimeCount() } returns 5

        // When
        val pagingState = PagingState<Int, AnimeEntity>(
            pages = listOf(),
            anchorPosition = null,
            config = PagingConfig(pageSize = 20),
            leadingPlaceholderCount = 0
        )
        val result = remoteMediator.load(LoadType.REFRESH, pagingState)

        // Then
        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertTrue((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
    }

    @Test
    fun `load returns Error when HttpException occurs and database is empty`() = runTest {
        // Given
        val httpException = mockk<retrofit2.HttpException>(relaxed = true)
        coEvery { apiService.getPopularAnimeByPage(any()) } throws httpException
        coEvery { animeDao.getAnimeCount() } returns 0

        // When
        val pagingState = PagingState<Int, AnimeEntity>(
            pages = listOf(),
            anchorPosition = null,
            config = PagingConfig(pageSize = 20),
            leadingPlaceholderCount = 0
        )
        val result = remoteMediator.load(LoadType.REFRESH, pagingState)

        // Then
        assertTrue(result is RemoteMediator.MediatorResult.Error)
    }

    @Test
    fun `load returns Success when HttpException occurs and database is not empty`() = runTest {
        // Given
        val httpException = mockk<retrofit2.HttpException>(relaxed = true)
        coEvery { apiService.getPopularAnimeByPage(any()) } throws httpException
        coEvery { animeDao.getAnimeCount() } returns 10

        // When
        val pagingState = PagingState<Int, AnimeEntity>(
            pages = listOf(),
            anchorPosition = null,
            config = PagingConfig(pageSize = 20),
            leadingPlaceholderCount = 0
        )
        val result = remoteMediator.load(LoadType.REFRESH, pagingState)

        // Then
        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertTrue((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
    }

    @Test
    fun `prepend load returns Success and endOfPaginationReached`() = runTest {
        val result = remoteMediator.load(LoadType.PREPEND, PagingState(listOf(), null, PagingConfig(20), 0))
        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertTrue((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
    }

    @Test
    fun `refresh load returns Success when body is null`() = runTest {
        coEvery { apiService.getPopularAnimeByPage(1) } returns Response.success(null)
        val result = remoteMediator.load(LoadType.REFRESH, PagingState(listOf(), null, PagingConfig(20), 0))
        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertTrue((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
    }

    @Test
    fun `refresh load with populated data covers mapping branches`() = runTest {
        // Test with a mix of null and non-null fields to cover elvis and safe call branches
        val animeDto1 = mockk<AnimeDto> {
            every { mal_id } returns null // Covers ?: 0
            every { title } returns "Test Anime"
            every { episodes } returns 12
            every { score } returns 8.5
            every { images } returns null // Covers ?. safe calls (images is null)
        }
        val animeDto2 = mockk<AnimeDto> {
            every { mal_id } returns 123 // Covers mal_id NOT null
            every { title } returns null // Covers title ?: "" null branch
            every { episodes } returns null
            every { score } returns null
            every { images } returns mockk {
                every { jpg } returns null // Covers images?.jpg null branch
            }
        }
        val animeDto3 = mockk<AnimeDto> {
            every { mal_id } returns 456
            every { title } returns "Anime 3"
            every { episodes } returns 24
            every { score } returns 9.0
            every { images } returns mockk {
                every { jpg } returns mockk {
                    every { image_url } returns "http://example.com/image.jpg" // Covers full path
                }
            }
        }

        val animeDataDto = AnimeDataDto(
            data = listOf(animeDto1, animeDto2, animeDto3),
            pagination = Pagination(current_page = 1, has_next_page = null, items = null, last_visible_page = 10)
        )
        coEvery { apiService.getPopularAnimeByPage(1) } returns Response.success(animeDataDto)

        val result = remoteMediator.load(LoadType.REFRESH, PagingState(listOf(), null, PagingConfig(20), 0))

        val slot = slot<List<AnimeEntity>>()
        coVerify { animeDao.upsertAnime(capture(slot)) }
        val entities = slot.captured
        
        // Verify animeDto1
        assertTrue(entities[0].id == 0)
        assertTrue(entities[0].title == "Test Anime")
        assertTrue(entities[0].posterImageUrl == null)
        
        // Verify animeDto2
        assertTrue(entities[1].id == 123)
        assertTrue(entities[1].title == "")
        assertTrue(entities[1].posterImageUrl == null)

        // Verify animeDto3
        assertTrue(entities[2].id == 456)
        assertTrue(entities[2].posterImageUrl == "http://example.com/image.jpg")

        // Verify has_next_page == null branch
        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertFalse((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
    }
}
