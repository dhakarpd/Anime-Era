package com.github.dhakarpd.animeera.core.di

import android.content.Context
import androidx.room.Room
import com.github.dhakarpd.animeera.data.local.AnimeDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAnimeDatabase(@ApplicationContext context: Context): AnimeDatabase {
        return Room.databaseBuilder(
            context,
            AnimeDatabase::class.java,
            "anime_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideAnimeDao(animeDatabase: AnimeDatabase) = animeDatabase.animeDao
}
