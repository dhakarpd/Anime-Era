package com.github.dhakarpd.animeera.domain.usecase

import com.github.dhakarpd.animeera.domain.repo.SyncScheduler
import com.github.dhakarpd.animeera.util.InternetConnectivityChecker
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class EnsureAnimeSyncUseCaseTest {

    private lateinit var connectivityChecker: InternetConnectivityChecker
    private lateinit var syncScheduler: SyncScheduler
    private lateinit var ensureAnimeSyncUseCase: EnsureAnimeSyncUseCase

    @Before
    fun setUp() {
        connectivityChecker = mockk()
        syncScheduler = mockk(relaxed = true)
        ensureAnimeSyncUseCase = EnsureAnimeSyncUseCase(connectivityChecker, syncScheduler)
    }

    @Test
    fun `execute returns true when connected to internet`() {
        // Given
        every { connectivityChecker.isConnectedToInternet() } returns true

        // When
        val result = ensureAnimeSyncUseCase.execute()

        // Then
        assertTrue(result)
        /*
        * Without that line, if someone later modified the execute()
        * function to always call scheduleSync() regardless of the internet
        * status, the test would still pass because assertTrue(result) would
        * still be true. Adding verify ensures the test fails if the
        * logic is broken.
        * */
        verify(exactly = 0) { syncScheduler.scheduleSync() }
    }

    @Test
    fun `execute returns false and schedules sync when not connected to internet`() {
        // Given
        every { connectivityChecker.isConnectedToInternet() } returns false

        // When
        val result = ensureAnimeSyncUseCase.execute()

        // Then
        assertFalse(result)
        verify(exactly = 1) { syncScheduler.scheduleSync() }
    }
}
