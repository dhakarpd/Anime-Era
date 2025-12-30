package com.github.dhakarpd.animeera.domain.repo

import com.github.dhakarpd.animeera.domain.model.AnimeFetchState
import com.github.dhakarpd.animeera.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow

interface AnimeDataRepository {

    /**
     * Single Source Of Truth for Anime Data is Room Database.
     * This function will insert data to Room Database and emit values
     * regarding sync status with remote server to UI
     * **/
    fun fetchPopularAnimeList(): Flow<SyncStatus>

    /**
     * Single Source Of Truth for Anime Data is Room Database.
     * This function will insert data to Room Database and emit values
     * regarding sync status with remote server to UI
     * **/
    fun fetchAnimeByID(animeId: Int): Flow<AnimeFetchState>

}