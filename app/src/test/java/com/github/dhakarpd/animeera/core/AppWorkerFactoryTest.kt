package com.github.dhakarpd.animeera.core

import android.content.Context
import androidx.work.WorkerParameters
import com.github.dhakarpd.animeera.core.worker.SyncWorker
import com.github.dhakarpd.animeera.domain.repo.AnimeDataRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import javax.inject.Provider

class AppWorkerFactoryTest {

    private lateinit var repository: AnimeDataRepository
    private lateinit var repositoryProvider: Provider<AnimeDataRepository>
    private lateinit var factory: AppWorkerFactory
    private lateinit var context: Context
    private lateinit var workerParameters: WorkerParameters

    @Before
    fun setUp() {
        repository = mockk()
        repositoryProvider = mockk()
        context = mockk(relaxed = true)
        workerParameters = mockk(relaxed = true)

        every { repositoryProvider.get() } returns repository

        factory = AppWorkerFactory(repositoryProvider)
    }

    @Test
    fun `createWorker returns SyncWorker for SyncWorker class name`() {
        // Given
        val workerClassName = SyncWorker::class.java.name

        // When
        val worker = factory.createWorker(context, workerClassName, workerParameters)

        // Then
        assertTrue(worker is SyncWorker)
    }

    @Test
    fun `createWorker returns null for unknown class name`() {
        // Given
        val workerClassName = "UnknownWorker"

        // When
        val worker = factory.createWorker(context, workerClassName, workerParameters)

        // Then
        assertNull(worker)
    }
}
