package com.bridge.yandexbyd

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var btnGrant: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tvStatus = findViewById(R.id.tvStatus)
        btnGrant = findViewById(R.id.btnGrant)
        btnGrant.setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }
    }

    override fun onResume() {
        super.onResume()
        val granted = isNotificationAccessGranted()
        tvStatus.text = if (granted)
            "Bridge active — listening for Yandex Navigator"
        else
            "Notification access required. Tap Grant Access."
        btnGrant.text = if (granted) "Open Settings" else "Grant Access"
    }

    private fun isNotificationAccessGranted(): Boolean {
        val flat = Settings.Secure.getString(
            contentResolver, "enabled_notification_listeners"
        )
        return !TextUtils.isEmpty(flat) && flat.contains(packageName)
    }
}
