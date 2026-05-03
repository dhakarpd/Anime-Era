package com.github.dhakarpd.animeera.core.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.github.dhakarpd.animeera.domain.repo.AnimeDataRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SyncWorkerTest {

    private lateinit var context: Context
    private lateinit var workerParameters: WorkerParameters
    private lateinit var repository: AnimeDataRepository
    private lateinit var worker: SyncWorker

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        workerParameters = mockk(relaxed = true)
        repository = mockk()
        worker = SyncWorker(context, workerParameters, repository)
    }

    @Test
    fun `doWork returns success when repository sync returns true`() = runTest {
        // Given
        coEvery { repository.syncPopularAnimeList() } returns true

        // When
        val result = worker.doWork()

        // Then
        assertEquals(ListenableWorker.Result.success(), result)
    }

    @Test
    fun `doWork returns retry when repository sync returns false`() = runTest {
        // Given
        coEvery { repository.syncPopularAnimeList() } returns false

        // When
        val result = worker.doWork()

        // Then
        assertEquals(ListenableWorker.Result.retry(), result)
    }

    @Test
    fun `doWork returns failure when repository sync throws exception`() = runTest {
        // Given
        coEvery { repository.syncPopularAnimeList() } throws Exception("Sync failed")

        // When
        val result = worker.doWork()

        // Then
        assertEquals(ListenableWorker.Result.failure(), result)
    }
}
