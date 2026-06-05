package com.example.nailbooking.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nailbooking.app.databinding.FragmentCalendarBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import com.example.nailbooking.app.R

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AppointmentViewModel by viewModels()
    private lateinit var adapter: AppointmentAdapter
    private var selectedDate: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = AppointmentAdapter(
            onDelete = { appt ->
                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Șterge programare")
                    .setMessage("Ești sigură că vrei să ștergi programarea pentru ${appt.customerName}?")
                    .setPositiveButton("Da, șterge") { _, _ ->
                        lifecycleScope.launch {
                            viewModel.delete(appt)
                            com.example.nailbooking.app.AlarmScheduler.cancel(requireContext(), appt)
                        }
                    }
                    .setNegativeButton("Anulează", null)
                    .show()
            },
            onEdit = { appt ->
                val action = CalendarFragmentDirections
                    .actionCalendarToAdd(selectedDate, appt.id)
                findNavController().navigate(action)
            }
        )

        binding.appointmentsRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.appointmentsRecycler.adapter = adapter

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        selectedDate = today
        binding.selectedDateLabel.text = getString(R.string.appointments_for) + today
        viewModel.loadDate(today)

        binding.calendarView.setOnDateChangeListener { _, year, month, day ->
            selectedDate = String.format("%04d-%02d-%02d", year, month + 1, day)
            binding.selectedDateLabel.text = getString(R.string.appointments_for) + selectedDate
            viewModel.loadDate(selectedDate)
        }

        binding.fabAdd.setOnClickListener {
            binding.searchField.text?.clear()
            val action = CalendarFragmentDirections.actionCalendarToAdd(selectedDate, -1)
            findNavController().navigate(action)
        }

        binding.btnSummary.setOnClickListener {
            val action = CalendarFragmentDirections.actionCalendarToSummary(selectedDate)
            findNavController().navigate(action)
        }

        binding.searchField.addTextChangedListener { text ->
            val query = text.toString().trim()
            if (query.isBlank()) {
                viewModel.loadDate(selectedDate)
                observeAppointments()
            } else {
                viewModel.search(query)
                observeSearchResults()
            }
        }

        observeAppointments()
    }

    private fun observeAppointments() {
        lifecycleScope.launch {
            viewModel.appointments.collectLatest { adapter.submitList(it) }
        }
    }

    private fun observeSearchResults() {
        lifecycleScope.launch {
            viewModel.searchResults.collectLatest { adapter.submitList(it) }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}