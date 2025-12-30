package com.github.dhakarpd.animeera.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.dhakarpd.animeera.domain.model.AnimeWithDetail

@Entity(tableName = "anime_with_details_entity")
data class AnimeWithDetailsEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val title: String,
    val numberOfEpisodes: Int?,
    val rating: Double?,
    val posterImageUrl: String?,
    val trailerUrl: String?,
    val cast: List<String>,
    val genres: List<String>,
    val synopsis: String?,
    val timestamp: Long,
    val isActive: Boolean
){
    fun toDomain(): AnimeWithDetail{
        return AnimeWithDetail(
            animeId = this.id,
            title = this.title,
            numberOfEpisodes = this.numberOfEpisodes,
            rating = this.rating,
            posterImageUrl = this.posterImageUrl,
            trailerUrl = this.trailerUrl,
            cast = this.cast,
            genres = this.genres,
            synopsis = this.synopsis
        )
    }
}
