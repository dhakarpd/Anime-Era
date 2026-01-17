package com.github.dhakarpd.animeera.core.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.github.dhakarpd.animeera.domain.repo.AnimeDataRepository

/**
 * @HiltWorker not used in this case because HiltWorkerFactory currently not compatible with
 * kotlin 2.x
 * **/
class SyncWorker(
    context: Context,
    params: WorkerParameters,
    private val repository: AnimeDataRepository
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        try {
            println("-------------------------------Sync Worker executed -------------------------")
            // 3. Call the new direct suspend function
            val success = repository.syncPopularAnimeList()
            return if (success) Result.success() else Result.retry()
        } catch (e: Exception) {
            println("-------------------------------Sync Worker failed with $e -------------------")
            return Result.failure()
        }
    }
}