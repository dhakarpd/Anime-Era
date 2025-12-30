package com.github.dhakarpd.animeera.data.local.typeConvertor

import androidx.room.TypeConverter

class StringListConverter {

    @TypeConverter
    fun fromList(list: List<String>?): String {
        return list?.joinToString(separator = ",") ?: ""
    }

    @TypeConverter
    fun toList(value: String): List<String> {
        return if (value.isEmpty()) emptyList() else value.split(",")
    }
}