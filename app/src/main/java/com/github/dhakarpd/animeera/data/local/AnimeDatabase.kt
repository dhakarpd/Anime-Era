package com.github.dhakarpd.animeera.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.github.dhakarpd.animeera.data.local.dao.AnimeDao
import com.github.dhakarpd.animeera.data.local.entity.AnimeEntity
import com.github.dhakarpd.animeera.data.local.entity.AnimeWithDetailsEntity
import com.github.dhakarpd.animeera.data.local.typeConvertor.StringListConverter

@Database(entities = [AnimeEntity::class, AnimeWithDetailsEntity::class], version = 1, exportSchema = false)
@TypeConverters(StringListConverter::class)
abstract class AnimeDatabase : RoomDatabase() {
    abstract val animeDao: AnimeDao
}
