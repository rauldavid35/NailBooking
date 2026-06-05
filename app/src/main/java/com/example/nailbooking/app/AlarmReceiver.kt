package com.example.nailbooking.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val customerName = intent.getStringExtra("customerName") ?: "Clientă"
        val startTime = intent.getStringExtra("startTime") ?: ""
        val alarmUri = intent.getStringExtra("alarmUri") ?: ""

        // Pornește serviciul care redă sunetul
        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtra("customerName", customerName)
            putExtra("startTime", startTime)
            putExtra("alarmUri", alarmUri)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }

        // Deschide ecranul de alarmă
        val activityIntent = Intent(context, AlarmActivity::class.java).apply {
            putExtra("customerName", customerName)
            putExtra("startTime", startTime)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_NO_USER_ACTION
        }
        context.startActivity(activityIntent)
    }
}