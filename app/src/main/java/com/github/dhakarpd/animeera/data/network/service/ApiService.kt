package com.github.dhakarpd.animeera.data.network.service

import com.github.dhakarpd.animeera.data.network.model.AnimeDataDto
import com.github.dhakarpd.animeera.data.network.model.AnimeDetailedDataDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("top/anime")
    suspend fun getPopularAnime(): Response<AnimeDataDto>

    @GET("anime/{id}")
    suspend fun getAnimeById(@Path("id") id: Int): Response<AnimeDetailedDataDto>
}
