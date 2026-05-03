package com.github.dhakarpd.animeera.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.github.dhakarpd.animeera.data.local.entity.AnimeEntity
import com.github.dhakarpd.animeera.data.local.entity.AnimeWithDetailsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AnimeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAnime(anime: List<AnimeEntity>)

    @Delete
    suspend fun deleteAnime(anime: AnimeEntity)

    @Query("SELECT * FROM anime_entity")
    fun getAllAnime(): PagingSource<Int, AnimeEntity>

    // 2. Add a function to clear the table. This is needed by the RemoteMediator for a full refresh.
    @Query("DELETE FROM anime_entity")
    suspend fun clearAllAnime()

    @Query("SELECT COUNT(*) FROM anime_entity")
    suspend fun getAnimeCount(): Int

//    @Query("SELECT * FROM anime_entity WHERE id = :id")
//    suspend fun getAnimeById(id: Int): AnimeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAnimeWithDetails(anime: AnimeWithDetailsEntity)

    @Delete
    suspend fun deleteAnimeWithDetails(anime: AnimeWithDetailsEntity)

    @Query("SELECT * FROM anime_with_details_entity WHERE id = :id")
    suspend fun getAnimeWithDetailsById(id: Int): AnimeWithDetailsEntity?
}
