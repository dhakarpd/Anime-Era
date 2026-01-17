package com.github.dhakarpd.animeera.core

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkerFactory
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class AnimeEra : Application(), Configuration.Provider{

    @Inject
    lateinit var workerFactory: WorkerFactory

    /**Setting logging level is very helpful in debug builds to identify why worker failing
     * even without executing. Im this case because
     * a- Hilt worker factory was not working
     * b- When startupInitialisation was not blocked in manifest at that time also default
     * factory was trying to initialize the SyncWorker which was not working.
     * **/
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(Log.DEBUG)
            .build()
}
