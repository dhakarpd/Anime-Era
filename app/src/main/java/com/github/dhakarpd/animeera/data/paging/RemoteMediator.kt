package com.github.dhakarpd.animeera.data.paging // Or your preferred package

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.github.dhakarpd.animeera.data.local.AnimeDatabase // Assuming this is your Database class
import com.github.dhakarpd.animeera.data.local.entity.AnimeEntity
import com.github.dhakarpd.animeera.data.network.service.ApiService
import retrofit2.HttpException
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class AnimeRemoteMediator(
    private val apiService: ApiService,
    private val animeDatabase: AnimeDatabase
) : RemoteMediator<Int, AnimeEntity>() {

    private val animeDao = animeDatabase.animeDao
    // TODO : Will not survive process death according to use case necessity can
    //  be moved to stored persistently if needed
    private var currentPage = 1 // Track the current page number

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, AnimeEntity>
    ): MediatorResult {
        return try {
            // Determine the page to load
            val loadKey = when (loadType) {
                // REFRESH is called on the initial load or when PagingDataAdapter.refresh() is called
                LoadType.REFRESH -> 1
                // PREPEND is not needed for this example as we only page forward
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                // APPEND is called when we need to load the next page
                LoadType.APPEND -> {
                    // This is where you would normally get the last item and determine the next page.
                    // For this API, we can simply increment our tracked page number.
                    currentPage + 1
                }
            }

            // Make the network request
            val response = apiService.getPopularAnimeByPage(page = loadKey)
            val animeDto = response.body()

            if (animeDto != null) {
                currentPage = loadKey // Update current page on successful load
                val animeEntities = animeDto.data.map { dto ->
                    // Map your DTO to your Entity
                    AnimeEntity(
                        id = dto.mal_id?:0,
                        title = dto.title?:"",
                        numberOfEpisodes = dto.episodes,
                        rating = dto.score,
                        posterImageUrl = dto.images?.jpg?.image_url,
                        timestamp = System.currentTimeMillis(),
                        isActive = true // Or based on some logic
                    )
                }

                // Save the data to the local database in a transaction
                animeDatabase.withTransaction {
                    if (loadType == LoadType.REFRESH) {
                        animeDao.clearAllAnime()
                    }
                    animeDao.upsertAnime(animeEntities)
                }

                // Check if there are more pages to load
                val endOfPaginationReached = animeDto.pagination.has_next_page == false
                MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
            }else {
                // This case handles non-2xx responses that are not exceptions
                MediatorResult.Success(endOfPaginationReached = true)
            }

        } catch (e: IOException) {
            // IOException means no network connection.
            // Tell the Paging library the mediation succeeded and no more pages can be loaded.
            // This allows the UI to display the cached data from Room.
            println("Offline Support: No network connection. - $e")
            MediatorResult.Success(endOfPaginationReached = true)
        } catch (e: HttpException) {
            // HttpException for non-2xx responses.
            // Treat this the same as no network: stop this remote load attempt.
            println("Offline Support: HTTP error. - $e")
            MediatorResult.Success(endOfPaginationReached = true)
        }
    }
}
