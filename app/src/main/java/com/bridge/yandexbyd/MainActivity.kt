package com.bridge.yandexbyd

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var cardAdb: LinearLayout
    private lateinit var tvAdbCommand: TextView
    private lateinit var tvTileAccessibility: TextView
    private lateinit var tvTileCapture: TextView
    private lateinit var tvTileProjectMedia: TextView
    private lateinit var tvTileBattery: TextView
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
        cardAdb = findViewById(R.id.cardAdb)
        tvAdbCommand = findViewById(R.id.tvAdbCommand)
        tvTileAccessibility = findViewById(R.id.tvTileAccessibility)
        tvTileCapture = findViewById(R.id.tvTileCapture)
        tvTileProjectMedia = findViewById(R.id.tvTileProjectMedia)
        tvTileBattery = findViewById(R.id.tvTileBattery)
        btnGrant = findViewById(R.id.btnGrant)
        tvAdbCommand.text = SetupHelper.ADB_GRANT_CMD
        btnGrant.setOnClickListener { requestCapture() }
        findViewById<Button>(R.id.btnCopyAdb).setOnClickListener { copyAdbCommand() }
        findViewById<Button>(R.id.btnBattery).setOnClickListener {
            runCatching { startActivity(SetupHelper.batterySettingsIntent(this)) }
        }
        autoCaptureTried = false
    }

    override fun onResume() {
        super.onResume()
        if (!SetupHelper.isAccessibilityEnabled(this)) {
            SetupHelper.tryEnableAccessibility(this)
        }
        if (!SetupHelper.isProjectMediaAllowed(this)) {
            SetupHelper.tryAllowProjectMedia(this)
        }
        if (SetupHelper.isAccessibilityEnabled(this) &&
            !CaptureService.isReady() &&
            !autoCaptureTried
        ) {
            autoCaptureTried = true
            requestCapture()
        }
        refreshStatus()
    }

    private fun refreshStatus() {
        val adbOk = SetupHelper.hasWriteSecureSettings(this)
        cardAdb.visibility = if (adbOk) View.GONE else View.VISIBLE

        val a11y = SetupHelper.isAccessibilityEnabled(this)
        tvTileAccessibility.text = tileLine(
            getString(R.string.setup_tile_accessibility),
            if (a11y) R.string.setup_status_ok else R.string.setup_status_off
        )

        val capture = CaptureService.isReady()
        tvTileCapture.text = tileLine(
            getString(R.string.setup_tile_capture),
            if (capture) R.string.setup_status_ok else R.string.setup_status_pending
        )

        val media = SetupHelper.isProjectMediaAllowed(this)
        tvTileProjectMedia.text = tileLine(
            getString(R.string.setup_tile_project_media),
            if (media) R.string.setup_status_ok else R.string.setup_status_pending
        )

        val battery = SetupHelper.isBatteryUnrestricted(this)
        tvTileBattery.text = tileLine(
            getString(R.string.setup_tile_battery),
            if (battery) R.string.setup_status_ok else R.string.setup_status_pending
        )
    }

    private fun tileLine(label: String, statusRes: Int): String {
        val mark = if (statusRes == R.string.setup_status_ok) "\u2713" else "\u2717"
        return "$mark $label: ${getString(statusRes)}"
    }

    private fun copyAdbCommand() {
        val cm = getSystemService(ClipboardManager::class.java)
        cm.setPrimaryClip(ClipData.newPlainText("adb", SetupHelper.ADB_GRANT_CMD))
        Toast.makeText(this, R.string.setup_adb_copied, Toast.LENGTH_SHORT).show()
    }

    private fun requestCapture() {
        val mpm = getSystemService(MediaProjectionManager::class.java)
        runCatching { projLauncher.launch(mpm.createScreenCaptureIntent()) }
            .onFailure {
                Log.w(TAG, "screen capture request failed: ${it.message}")
                tvTileCapture.text = tileLine(
                    getString(R.string.setup_tile_capture),
                    R.string.setup_status_off
                )
            }
    }

    companion object {
        private const val TAG = "VoltFlowNav"
    }
}
