package com.github.dhakarpd.animeera.data.local.typeConvertor

import androidx.room.TypeConverter

class StringListConverter {

    @TypeConverter
    fun fromList(list: List<String>?): String {
        if (list == null) return ""
        return list.joinToString(separator = ",")
    }

    @TypeConverter
    fun toList(value: String): List<String> {
        return if (value.isBlank()) emptyList() else value.split(",")
    }
}