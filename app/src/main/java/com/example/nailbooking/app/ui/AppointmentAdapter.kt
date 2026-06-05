package com.example.nailbooking.app.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nailbooking.app.R
import com.example.nailbooking.app.data.Appointment
import com.example.nailbooking.app.data.ServiceType
import com.example.nailbooking.app.databinding.ItemAppointmentBinding

class AppointmentAdapter(
    private val onDelete: (Appointment) -> Unit,
    private val onEdit: (Appointment) -> Unit
) : ListAdapter<Appointment, AppointmentAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(val binding: ItemAppointmentBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAppointmentBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val appt = getItem(position)
        with(holder.binding) {
            customerName.text = appt.customerName
            timeInfo.text = "${appt.startTime} · ${appt.durationMinutes} min"
            serviceLabel.text = appt.serviceType.displayName
            if (appt.notes.isNotBlank()) {
                notesLabel.visibility = android.view.View.VISIBLE
                notesLabel.text = appt.notes
            } else {
                notesLabel.visibility = android.view.View.GONE
            }

            val colorRes = when (appt.serviceType) {
                ServiceType.MANICURE -> R.color.service_manicure
                ServiceType.PEDICURE -> R.color.service_pedicure
                ServiceType.GEL -> R.color.service_gel
                ServiceType.NAIL_ART -> R.color.service_nail_art
                ServiceType.OTHER -> R.color.service_other
            }
            serviceColorBar.setBackgroundColor(
                ContextCompat.getColor(root.context, colorRes)
            )

            btnDelete.setOnClickListener { onDelete(appt) }
            root.setOnClickListener { onEdit(appt) }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Appointment>() {
        override fun areItemsTheSame(a: Appointment, b: Appointment) = a.id == b.id
        override fun areContentsTheSame(a: Appointment, b: Appointment) = a == b
    }
}