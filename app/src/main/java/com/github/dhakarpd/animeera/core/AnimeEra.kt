package com.github.dhakarpd.animeera.core

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AnimeEra: Application() {
    override fun onCreate() {
        super.onCreate()
    }
}