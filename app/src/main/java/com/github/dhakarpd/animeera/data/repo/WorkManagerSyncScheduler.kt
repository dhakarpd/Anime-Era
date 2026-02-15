package com.github.dhakarpd.animeera.data.repo

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.github.dhakarpd.animeera.core.worker.SyncWorker
import com.github.dhakarpd.animeera.domain.repo.SyncScheduler
import com.github.dhakarpd.animeera.util.Constants
import javax.inject.Inject

class WorkManagerSyncScheduler @Inject constructor(
    private val context: Context
) : SyncScheduler {

    override fun scheduleSync() {
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                Constants.WORK_REQUEST_NAME,
                ExistingWorkPolicy.REPLACE,
                request
            )
    }
}
