package com.github.dhakarpd.animeera.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.github.dhakarpd.animeera.data.local.dao.AnimeDao
import com.github.dhakarpd.animeera.data.local.entity.AnimeEntity
import com.github.dhakarpd.animeera.data.local.entity.AnimeWithDetailsEntity
import com.github.dhakarpd.animeera.data.local.typeConvertor.StringListConverter

/**
 * TODO: How it is actually tested: You should write Instrumented Tests
 * (androidTest folder) for your DAOs (AnimeDao) to ensure SQL
 * queries work correctly on a real device/emulator. A JVM unit
 * test cannot run Room easily.
 * **/
@Database(entities = [AnimeEntity::class, AnimeWithDetailsEntity::class], version = 1, exportSchema = false)
@TypeConverters(StringListConverter::class)
abstract class AnimeDatabase : RoomDatabase() {
    abstract val animeDao: AnimeDao
}
