package com.example.nailbooking.app.data

import kotlinx.coroutines.flow.Flow

class AppointmentRepository(private val dao: AppointmentDao) {

    fun getByDate(date: String): Flow<List<Appointment>> = dao.getByDate(date)
    fun getAllDates(): Flow<List<String>> = dao.getAllDates()
    fun searchByName(query: String): Flow<List<Appointment>> = dao.searchByName(query)

    suspend fun getByDateSync(date: String): List<Appointment> = dao.getByDateSync(date)
    suspend fun getAllSync(): List<Appointment> = dao.getAllSync()

    suspend fun insert(appointment: Appointment): Long = dao.insert(appointment)
    suspend fun update(appointment: Appointment) = dao.update(appointment)
    suspend fun delete(appointment: Appointment) = dao.delete(appointment)

    fun hasOverlap(
        existing: List<Appointment>,
        newStart: Int,
        newDuration: Int,
        excludeId: Int = -1
    ): Boolean {
        val newEnd = newStart + newDuration
        return existing
            .filter { it.id != excludeId }
            .any { appt ->
                val existStart = timeToMinutes(appt.startTime)
                val existEnd = existStart + appt.durationMinutes
                newStart < existEnd && newEnd > existStart
            }
    }

    fun timeToMinutes(time: String): Int {
        val parts = time.split(":")
        return parts[0].toInt() * 60 + parts[1].toInt()
    }
}