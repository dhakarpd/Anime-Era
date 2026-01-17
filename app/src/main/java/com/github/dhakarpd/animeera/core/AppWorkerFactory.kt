package com.github.dhakarpd.animeera.core

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.github.dhakarpd.animeera.core.worker.SyncWorker
import com.github.dhakarpd.animeera.domain.repo.AnimeDataRepository
import javax.inject.Inject
import javax.inject.Provider

class AppWorkerFactory @Inject constructor(
    private val repository: Provider<AnimeDataRepository>
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            SyncWorker::class.java.name ->
                SyncWorker(appContext, workerParameters, repository.get())
            else -> null
        }
    }
}