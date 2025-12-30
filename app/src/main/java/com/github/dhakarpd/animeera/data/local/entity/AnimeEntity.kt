package com.github.dhakarpd.animeera.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "anime_entity")
data class AnimeEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val title: String,
    val numberOfEpisodes: Int?,
    val rating: Double?,
    val posterImageUrl: String?,
    val timestamp: Long,
    val isActive: Boolean
)
