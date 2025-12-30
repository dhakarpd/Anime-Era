package com.github.dhakarpd.animeera.data.network.model

data class AnimeDataDto(
    val data: List<AnimeDto>,
    val pagination: Pagination
)