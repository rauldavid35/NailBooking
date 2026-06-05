package com.example.nailbooking.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.nailbooking.app.data.AppDatabase
import com.example.nailbooking.app.databinding.FragmentSummaryBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SummaryFragment : Fragment() {

    private var _binding: FragmentSummaryBinding? = null
    private val binding get() = _binding!!
    private val args: SummaryFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val date = args.selectedDate
        binding.summaryTitle.text = "Rezumat pentru\n$date"

        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(requireContext())
            val appointments = db.appointmentDao().getByDateSync(date)

            val count = appointments.size
            val totalMinutes = appointments.sumOf { it.durationMinutes }
            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60

            val hoursText = when {
                count == 0 -> "0h"
                minutes == 0 -> "${hours}h"
                else -> "${hours}h ${minutes}min"
            }

            withContext(Dispatchers.Main) {
                binding.summaryCount.text = "$count"
                binding.summaryHours.text = hoursText
            }
        }

        binding.btnCloseSummary.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}