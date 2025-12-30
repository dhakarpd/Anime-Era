package com.github.dhakarpd.animeera.domain.model

data class AnimeWithDetail(
    val animeId: Int,
    val title: String,
    val numberOfEpisodes: Int?,
    val rating: Double?,
    val posterImageUrl: String?,
    val trailerUrl: String?,
    val cast: List<String>,
    val genres: List<String>,
    val synopsis: String?,
)