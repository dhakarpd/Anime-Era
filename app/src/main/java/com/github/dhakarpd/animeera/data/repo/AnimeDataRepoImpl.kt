package com.github.dhakarpd.animeera.data.repo

import android.content.Context
import com.github.dhakarpd.animeera.data.local.dao.AnimeDao
import com.github.dhakarpd.animeera.data.local.entity.AnimeEntity
import com.github.dhakarpd.animeera.data.local.entity.AnimeWithDetailsEntity
import com.github.dhakarpd.animeera.data.network.model.AnimeDto
import com.github.dhakarpd.animeera.data.network.service.ApiService
import com.github.dhakarpd.animeera.domain.model.AnimeFetchState
import com.github.dhakarpd.animeera.domain.model.SyncStatus
import com.github.dhakarpd.animeera.domain.repo.AnimeDataRepository
import com.github.dhakarpd.animeera.util.InternetConnectivityChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class AnimeDataRepoImpl @Inject constructor(val apiService: ApiService, val animeDao: AnimeDao, val context: Context): AnimeDataRepository {
    private fun convertAnimeDtoListToEntityList(animeList: List<AnimeDto>): List<AnimeEntity> {
        return animeList.map { animeDto ->
            AnimeEntity(
                id = animeDto.mal_id ?: 0,
                title = animeDto.title ?: "",
                numberOfEpisodes = animeDto.episodes ?: 0,
                rating = animeDto.score ?: 0.0,
                posterImageUrl = animeDto.images?.jpg?.image_url ?: "",
                timestamp = System.currentTimeMillis(),
                isActive = true,
            )
        }
    }

    override fun fetchPopularAnimeList(): Flow<SyncStatus> = flow {
        emit(SyncStatus.SYNCING)
        if (InternetConnectivityChecker.isConnectedToInternet(context)) {
            delay(5000)
            val response = apiService.getPopularAnime()
            if (response.isSuccessful && response.body() != null) {
                val animeList = response.body()!!.data
                animeDao.upsertAnime(convertAnimeDtoListToEntityList(animeList))
                emit(SyncStatus.SUCCESS)
            } else {
                emit(SyncStatus.ERROR)
            }
        } else {
            emit(SyncStatus.NO_INTERNET_CONNECTION)
        }
    }.catch {
        emit(SyncStatus.ERROR)
    }.flowOn(Dispatchers.IO)

    override fun fetchAnimeByID(animeId: Int): Flow<AnimeFetchState> = flow {
        emit(AnimeFetchState.Loading)
        try {
            val staleData = animeDao.getAnimeWithDetailsById(animeId)?.toDomain()
            staleData?.let {
                emit(AnimeFetchState.StaleDataFetched(it))
            }
        } catch (e: Exception){
            emit(AnimeFetchState.StaleDataFetchFailure(e.message))
        }
        emit(AnimeFetchState.Syncing)
        if (InternetConnectivityChecker.isConnectedToInternet(context)) {
            delay(5000)
            val response = apiService.getAnimeById(animeId)
            if (response.isSuccessful && response.body() != null) {
                val anime = response.body()!!.data
                val dbInsertionData = anime.toAnimeWithDetailsEntity()
                animeDao.upsertAnimeWithDetails(dbInsertionData)
                emit(AnimeFetchState.SyncSuccess(dbInsertionData.toDomain()))
            } else {
                emit(AnimeFetchState.SyncFailure(null))
            }
        } else {
            emit(AnimeFetchState.NoInternetAvailable)
        }
    }.catch {
        emit(AnimeFetchState.SyncFailure(it.message))
    }.flowOn(Dispatchers.IO)

    private fun AnimeDto.toAnimeWithDetailsEntity(): AnimeWithDetailsEntity {
        var trailerUrl : String? = this.trailer?.url.toString()
        if (trailerUrl == "null") {
            //DEFAULT JUST FOR EXOPLAYER FLOW CHECK
            //trailerUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
            trailerUrl = null
        }

        val genres = this.genres?.let {
            it.filter { genre ->
                genre.name != null
            }.map { genre ->
                genre.name ?:""
            }
        } ?: run {
            emptyList()
        }

        return AnimeWithDetailsEntity(
            id = this.mal_id ?: 0,
            title = this.title ?: "",
            numberOfEpisodes = this.episodes ?: 0,
            rating = this.score ?: 0.0,
            posterImageUrl = this.images?.jpg?.image_url ?: "",
            synopsis = this.synopsis ?: "",
            genres = genres,
            timestamp = System.currentTimeMillis(),
            isActive = true,
            trailerUrl = trailerUrl,
            cast = emptyList(),
        )

    }
}

