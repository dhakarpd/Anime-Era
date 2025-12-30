package com.github.dhakarpd.animeera.core.di

import android.content.Context
import com.github.dhakarpd.animeera.data.local.AnimeDatabase
import com.github.dhakarpd.animeera.data.network.service.ApiService
import com.github.dhakarpd.animeera.data.repo.AnimeDataRepoImpl
import com.github.dhakarpd.animeera.domain.repo.AnimeDataRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAnimeDataRepository(
        apiService: ApiService,
        animeDatabase: AnimeDatabase,
        @ApplicationContext context: Context
    ): AnimeDataRepository {
        return AnimeDataRepoImpl(apiService, animeDatabase.animeDao, context)
    }
}
