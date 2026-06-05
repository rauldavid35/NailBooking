package com.example.nailbooking.app

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.nailbooking.app.data.Appointment
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object AlarmScheduler {

    private const val TAG = "AlarmScheduler"

    fun schedule(context: Context, appointment: Appointment) {
        Log.d(TAG, "schedule() apelat pentru: ${appointment.customerName}, id=${appointment.id}, alarmMinutes=${appointment.alarmMinutesBefore}")

        if (appointment.alarmMinutesBefore <= 0) {
            Log.d(TAG, "Alarma dezactivată (alarmMinutesBefore <= 0)")
            return
        }

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val dateTimeStr = "${appointment.date} ${appointment.startTime}"

        val appointmentTime = LocalDateTime.parse(dateTimeStr, formatter)
        val alarmTime = appointmentTime.minusMinutes(appointment.alarmMinutesBefore.toLong())
        val triggerAt = alarmTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val now = System.currentTimeMillis()

        Log.d(TAG, "Ora programare: $appointmentTime")
        Log.d(TAG, "Ora alarmă: $alarmTime")
        Log.d(TAG, "triggerAt=$triggerAt, now=$now, diff=${triggerAt - now}ms")

        if (triggerAt <= now) {
            Log.d(TAG, "ATENTIE: triggerAt e în trecut! Alarma nu va fi setată.")
            return
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("customerName", appointment.customerName)
            putExtra("startTime", appointment.startTime)
            putExtra("alarmUri", appointment.alarmUri)
            putExtra("appointmentId", appointment.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            appointment.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        try {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            Log.d(TAG, "Alarma setată cu succes! Va declanșa în ${(triggerAt - now) / 1000}s")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException — permisiunea SCHEDULE_EXACT_ALARM lipsește: ${e.message}")
        }
    }

    fun cancel(context: Context, appointment: Appointment) {
        Log.d(TAG, "cancel() pentru: ${appointment.customerName}, id=${appointment.id}")
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            appointment.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }
}