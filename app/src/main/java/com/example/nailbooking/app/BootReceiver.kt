package com.example.nailbooking.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.nailbooking.app.data.AppDatabase
import com.example.nailbooking.app.data.AppointmentRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val db = AppDatabase.getDatabase(context)
            val repo = AppointmentRepository(db.appointmentDao())
            CoroutineScope(Dispatchers.IO).launch {
                val all = repo.getAllSync()
                all.forEach { AlarmScheduler.schedule(context, it) }
            }
        }
    }
}