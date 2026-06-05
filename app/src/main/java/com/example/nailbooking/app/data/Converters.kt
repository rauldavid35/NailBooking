package com.example.nailbooking.app.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromServiceType(value: ServiceType): String = value.name

    @TypeConverter
    fun toServiceType(value: String): ServiceType = ServiceType.valueOf(value)
}