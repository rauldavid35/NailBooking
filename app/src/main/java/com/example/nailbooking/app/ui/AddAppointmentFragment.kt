package com.example.nailbooking.app.ui

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.example.nailbooking.app.AlarmScheduler
import com.example.nailbooking.app.data.Appointment
import com.example.nailbooking.app.data.AppDatabase
import com.example.nailbooking.app.data.ServiceType
import com.example.nailbooking.app.databinding.FragmentAddAppointmentBinding
import kotlinx.coroutines.launch

class AddAppointmentFragment : Fragment() {

    private var _binding: FragmentAddAppointmentBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AppointmentViewModel by viewModels()
    private val args: AddAppointmentFragmentArgs by navArgs()

    private var selectedHour = -1
    private var selectedMinute = -1
    private var selectedAlarmUri = ""
    private var editingAppointment: Appointment? = null

    private val ringtonePicker = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            if (uri != null) {
                selectedAlarmUri = uri.toString()
                val ringtone = RingtoneManager.getRingtone(requireContext(), uri)
                binding.selectedSoundLabel.text = ringtone.getTitle(requireContext())
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddAppointmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val serviceNames = ServiceType.values().map { it.displayName }
        val spinnerAdapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_item, serviceNames
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerService.adapter = spinnerAdapter

        val appointmentId = args.appointmentId
        if (appointmentId != -1) {
            lifecycleScope.launch {
                val db = AppDatabase.getDatabase(requireContext())
                val appt = db.appointmentDao().getByDateSync(args.selectedDate)
                    .find { it.id == appointmentId }
                appt?.let { loadForEditing(it) }
            }
        }

        binding.btnPickTime.setOnClickListener {
            val picker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(if (selectedHour >= 0) selectedHour else 9)
                .setMinute(if (selectedMinute >= 0) selectedMinute else 0)
                .setTitleText("Select appointment time")
                .build()
            picker.addOnPositiveButtonClickListener {
                selectedHour = picker.hour
                selectedMinute = picker.minute
                binding.btnPickTime.text = "Ora: ${String.format("%02d:%02d", selectedHour, selectedMinute)}"
            }
            picker.show(parentFragmentManager, "timePicker")
        }

        binding.btnPickSound.setOnClickListener {
            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL)
                putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Choose Alarm Sound")
                if (selectedAlarmUri.isNotEmpty())
                    putExtra(
                        RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                        Uri.parse(selectedAlarmUri)
                    )
            }
            ringtonePicker.launch(intent)
        }

        binding.btnSave.setOnClickListener {
            val name = binding.fieldName.text.toString().trim()
            val durationStr = binding.fieldDuration.text.toString().trim()
            val notesText = binding.fieldNotes.text.toString().trim()
            val alarmMinsStr = binding.fieldAlarmMinutes.text.toString().trim()

            if (name.isEmpty() || selectedHour < 0 || durationStr.isEmpty()) {
                binding.overlapError.text = "Completează numele, ora și durata!"
                binding.overlapError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            val startTime = String.format("%02d:%02d", selectedHour, selectedMinute)
            val duration = durationStr.toInt()
            val alarmMins = alarmMinsStr.toIntOrNull() ?: 15
            val serviceType = ServiceType.values()[binding.spinnerService.selectedItemPosition]

            val appointment = Appointment(
                id = editingAppointment?.id ?: 0,
                customerName = name,
                date = args.selectedDate.ifEmpty {
                    java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                        .format(java.util.Date())
                },
                startTime = startTime,
                durationMinutes = duration,
                serviceType = serviceType,
                notes = notesText,
                alarmUri = selectedAlarmUri,
                alarmMinutesBefore = alarmMins
            )

            lifecycleScope.launch {
                val savedAppointment = viewModel.saveAppointment(appointment)
                if (savedAppointment != null) {
                    AlarmScheduler.schedule(requireContext(), savedAppointment)
                    findNavController().popBackStack()
                } else {
                    binding.overlapError.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun loadForEditing(appt: Appointment) {
        editingAppointment = appt
        binding.fieldName.setText(appt.customerName)
        val parts = appt.startTime.split(":")
        selectedHour = parts[0].toInt()
        selectedMinute = parts[1].toInt()
        binding.btnPickTime.text = "Time: ${appt.startTime}"
        binding.fieldDuration.setText(appt.durationMinutes.toString())
        binding.fieldNotes.setText(appt.notes)
        binding.fieldAlarmMinutes.setText(appt.alarmMinutesBefore.toString())
        binding.spinnerService.setSelection(appt.serviceType.ordinal)
        if (appt.alarmUri.isNotEmpty()) {
            selectedAlarmUri = appt.alarmUri
            val uri = Uri.parse(appt.alarmUri)
            val ringtone = RingtoneManager.getRingtone(requireContext(), uri)
            binding.selectedSoundLabel.text = ringtone?.getTitle(requireContext()) ?: "Custom sound"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}