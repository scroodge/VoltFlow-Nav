package com.bridge.yandexbyd

import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var btnGrant: Button
    private var autoCaptureTried = false

    private val projLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
            if (res.resultCode == RESULT_OK && res.data != null) {
                val i = Intent(this, CaptureService::class.java)
                    .putExtra(CaptureService.EXTRA_RESULT_CODE, res.resultCode)
                    .putExtra(CaptureService.EXTRA_DATA, res.data)
                if (Build.VERSION.SDK_INT >= 26) startForegroundService(i) else startService(i)
            }
            refreshStatus()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tvStatus = findViewById(R.id.tvStatus)
        btnGrant = findViewById(R.id.btnGrant)
        btnGrant.setOnClickListener { requestCapture() }
        autoCaptureTried = false
    }

    override fun onResume() {
        super.onResume()
        // Self-heal accessibility (disabled by Android on every reinstall).
        if (!isAccessibilityEnabled()) tryEnableAccessibility()
        // Auto-establish screen capture once per app open (projection is lost on reboot).
        if (isAccessibilityEnabled() && !CaptureService.isReady() && !autoCaptureTried) {
            autoCaptureTried = true
            requestCapture()
        }
        refreshStatus()
    }

    private fun refreshStatus() {
        val a11y = isAccessibilityEnabled()
        tvStatus.text = buildString {
            append(if (a11y) "✓ Accessibility enabled\n" else "✗ Accessibility OFF\n")
            append(if (CaptureService.isReady()) "✓ Screen capture running" else "… starting screen capture")
        }
        btnGrant.text = "Restart Screen Capture"
    }

    private fun requestCapture() {
        val mpm = getSystemService(MediaProjectionManager::class.java)
        runCatching { projLauncher.launch(mpm.createScreenCaptureIntent()) }
            .onFailure { tvStatus.text = "Screen-capture request failed: ${it.message}" }
    }

    /**
     * Re-enable our accessibility service without ADB. Needs WRITE_SECURE_SETTINGS,
     * granted once via `adb shell pm grant <pkg> android.permission.WRITE_SECURE_SETTINGS`
     * (that grant survives reinstalls, so the app keeps healing itself).
     */
    private fun tryEnableAccessibility() {
        try {
            val component = "$packageName/$packageName.YandexA11yService"
            val current = Settings.Secure.getString(
                contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ).orEmpty()
            val set = current.split(':').filter { it.isNotEmpty() }.toMutableSet()
            if (set.add(component)) {
                Settings.Secure.putString(
                    contentResolver,
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
                    set.joinToString(":")
                )
            }
            Settings.Secure.putInt(contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED, 1)
            Log.d("YandexBYDBridge", "accessibility self-enabled")
        } catch (e: Exception) {
            Log.w("YandexBYDBridge", "auto-enable accessibility failed (grant WRITE_SECURE_SETTINGS): ${e.message}")
        }
    }

    private fun isAccessibilityEnabled(): Boolean {
        val flat = Settings.Secure.getString(
            contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        return !TextUtils.isEmpty(flat) && flat.contains(packageName)
    }
}
