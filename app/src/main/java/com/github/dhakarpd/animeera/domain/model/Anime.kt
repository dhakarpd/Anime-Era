package com.github.dhakarpd.animeera.domain.model

data class Anime(
    val id: Int,
    val title: String,
    val numberOfEpisodes: Int?,
    val rating: Double?,
    val posterImageUrl: String?,
)