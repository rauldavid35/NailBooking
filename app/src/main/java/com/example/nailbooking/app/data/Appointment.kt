package com.example.nailbooking.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ServiceType(val displayName: String, val colorRes: Int) {
    MANICURE("Manichiură", android.R.color.holo_red_light),
    PEDICURE("Pedichiură", android.R.color.holo_purple),
    GEL("Gel", android.R.color.holo_blue_dark),
    NAIL_ART("Nail Art", android.R.color.holo_orange_dark),
    OTHER("Altele", android.R.color.darker_gray)
}

@Entity(tableName = "appointments")
data class Appointment(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val customerName: String,
    val date: String,
    val startTime: String,
    val durationMinutes: Int,
    val serviceType: ServiceType = ServiceType.OTHER,
    val notes: String = "",
    val alarmUri: String = "",
    val alarmMinutesBefore: Int = 15
)