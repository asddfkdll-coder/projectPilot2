package com.projectpilot.app.data.local

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromString(value: String): List<String> {
        return if (value.isBlank()) emptyList() else value.split(",")
    }

    @TypeConverter
    fun fromList(list: List<String>): String {
        return list.joinToString(",")
    }

    @TypeConverter
    fun fromIntList(value: String): List<Int> {
        return if (value.isBlank()) emptyList() else value.split(",").map { it.toInt() }
    }

    @TypeConverter
    fun toIntList(list: List<Int>): String {
        return list.joinToString(",")
    }

    @TypeConverter
    fun fromStringMap(value: String): Map<String, String> {
        return if (value.isBlank()) emptyMap() else value.split(",").associate {
            val parts = it.split("=")
            parts[0] to (parts.getOrNull(1) ?: "")
        }
    }

    @TypeConverter
    fun toStringMap(map: Map<String, String>): String {
        return map.map { "${it.key}=${it.value}" }.joinToString(",")
    }
}
