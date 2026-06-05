package com.example.nailbooking.app

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.nailbooking.app.databinding.ActivityAlarmBinding

class AlarmActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlarmBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        binding = ActivityAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val customerName = intent.getStringExtra("customerName") ?: "Clientă"
        val startTime = intent.getStringExtra("startTime") ?: ""

        binding.alarmCustomerName.text = customerName
        binding.alarmTime.text = "la ora $startTime"

        binding.btnDismiss.setOnClickListener {
            stopService(Intent(this, AlarmService::class.java))
            finish()
        }
    }

    override fun onBackPressed() {
        stopService(Intent(this, AlarmService::class.java))
        super.onBackPressed()
        finish()
    }
}