package com.github.dhakarpd.animeera.core

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkerFactory
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class AnimeEra : Application(), Configuration.Provider, ImageLoaderFactory {

    @Inject
    lateinit var workerFactory: WorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(Log.DEBUG)
            .build()

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {              // Stores images in RAM
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {    // Path: /data/data/<app>/cache/image_cache ; Safe → Android may delete if storage is low
                DiskCache.Builder()
                    .directory(this.cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02) // Increase to 5% if better offline support needed
                    .build()
            }
            .respectCacheHeaders(false) //Ignores server headers like:  Force caching even if server says "no-cache"
            .crossfade(true)
            .build()
    }
}
