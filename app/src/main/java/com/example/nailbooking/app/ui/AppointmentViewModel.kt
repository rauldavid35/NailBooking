package com.example.nailbooking.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nailbooking.app.data.Appointment
import com.example.nailbooking.app.data.AppDatabase
import com.example.nailbooking.app.data.AppointmentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AppointmentViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = AppointmentRepository(
        AppDatabase.getDatabase(application).appointmentDao()
    )

    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments: StateFlow<List<Appointment>> = _appointments

    private val _allDates = MutableStateFlow<List<String>>(emptyList())
    val allDates: StateFlow<List<String>> = _allDates

    private val _searchResults = MutableStateFlow<List<Appointment>>(emptyList())
    val searchResults: StateFlow<List<Appointment>> = _searchResults

    private val _overlapError = MutableStateFlow(false)
    val overlapError: StateFlow<Boolean> = _overlapError

    init {
        viewModelScope.launch {
            repo.getAllDates().collectLatest { _allDates.value = it }
        }
    }

    fun loadDate(date: String) {
        viewModelScope.launch {
            repo.getByDate(date).collectLatest { _appointments.value = it }
        }
    }

    fun search(query: String) {
        viewModelScope.launch {
            repo.searchByName(query).collectLatest { _searchResults.value = it }
        }
    }

    suspend fun saveAppointment(appointment: Appointment): Appointment? {
        val existing = repo.getByDateSync(appointment.date)
        val startMinutes = repo.timeToMinutes(appointment.startTime)
        if (repo.hasOverlap(existing, startMinutes, appointment.durationMinutes, appointment.id)) {
            _overlapError.value = true
            return null
        }
        _overlapError.value = false
        return if (appointment.id == 0) {
            val newId = repo.insert(appointment)
            appointment.copy(id = newId.toInt())
        } else {
            repo.update(appointment)
            appointment
        }
    }

    suspend fun delete(appointment: Appointment) = repo.delete(appointment)
}