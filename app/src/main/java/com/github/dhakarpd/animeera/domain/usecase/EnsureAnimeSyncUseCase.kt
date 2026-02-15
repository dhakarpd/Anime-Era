package com.github.dhakarpd.animeera.domain.usecase

import com.github.dhakarpd.animeera.domain.repo.SyncScheduler
import com.github.dhakarpd.animeera.util.InternetConnectivityChecker
import javax.inject.Inject

class EnsureAnimeSyncUseCase @Inject constructor(
    private val connectivityChecker: InternetConnectivityChecker,
    private val syncScheduler: SyncScheduler
) {
    fun execute(): Boolean {
        return if (!connectivityChecker.isConnectedToInternet()) {
            syncScheduler.scheduleSync()
            false
        } else {
            true
        }
    }
}
