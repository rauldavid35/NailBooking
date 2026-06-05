package com.example.nailbooking.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppointmentDao {

    @Query("SELECT * FROM appointments WHERE date = :date ORDER BY startTime ASC")
    fun getByDate(date: String): Flow<List<Appointment>>

    @Query("SELECT DISTINCT date FROM appointments")
    fun getAllDates(): Flow<List<String>>

    @Query("SELECT * FROM appointments WHERE customerName LIKE '%' || :query || '%' ORDER BY date ASC, startTime ASC")
    fun searchByName(query: String): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE date = :date ORDER BY startTime ASC")
    suspend fun getByDateSync(date: String): List<Appointment>

    @Query("SELECT * FROM appointments")
    suspend fun getAllSync(): List<Appointment>

    @Insert
    suspend fun insert(appointment: Appointment): Long

    @Update
    suspend fun update(appointment: Appointment)

    @Delete
    suspend fun delete(appointment: Appointment)
}