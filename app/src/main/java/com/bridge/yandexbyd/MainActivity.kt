package com.bridge.yandexbyd

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var cardAdb: LinearLayout
    private lateinit var tvAdbCommand: TextView
    private lateinit var tvTileAccessibility: TextView
    private lateinit var tvTileCapture: TextView
    private lateinit var tvTileProjectMedia: TextView
    private lateinit var tvTileBattery: TextView
    private lateinit var tvAppVersion: TextView
    private lateinit var switchAutoCheck: SwitchCompat
    private lateinit var btnGrant: Button
    private var autoCaptureTried = false
    private var autoUpdateChecked = false

    private var updateDialog: AlertDialog? = null
    private var pendingUpdate: UpdateChecker.UpdateInfo? = null
    private var updateUiState: UpdateUiState? = null

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
        tvAppVersion = findViewById(R.id.tvAppVersion)
        switchAutoCheck = findViewById(R.id.switchAutoCheck)
        btnGrant = findViewById(R.id.btnGrant)
        tvAdbCommand.text = SetupHelper.ADB_GRANT_CMD
        btnGrant.setOnClickListener { requestCapture() }
        findViewById<Button>(R.id.btnCopyAdb).setOnClickListener { copyAdbCommand() }
        findViewById<Button>(R.id.btnBattery).setOnClickListener {
            runCatching { startActivity(SetupHelper.batterySettingsIntent(this)) }
        }
        switchAutoCheck.isChecked = UpdateChecker.isAutoCheckEnabled(this)
        switchAutoCheck.setOnCheckedChangeListener { _, enabled ->
            UpdateChecker.setAutoCheckEnabled(this, enabled)
        }
        findViewById<Button>(R.id.btnCheckUpdates).setOnClickListener {
            runManualUpdateCheck()
        }
        refreshVersionLabel()
        autoCaptureTried = false
        autoUpdateChecked = false
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
        maybeAutoCheckForUpdates()
    }

    private fun maybeAutoCheckForUpdates() {
        if (autoUpdateChecked || !UpdateChecker.isAutoCheckEnabled(this)) return
        autoUpdateChecked = true
        lifecycleScope.launch {
            try {
                val info = UpdateChecker.checkForUpdate(this@MainActivity, forceCheck = false)
                if (info != null) {
                    pendingUpdate = info
                    showUpdateDialog(
                        UpdateUiState.Available(info.version, info.releaseNotes),
                    )
                }
            } catch (e: Exception) {
                Log.w(TAG, "auto update check failed: ${e.message}")
            }
        }
    }

    private fun runManualUpdateCheck() {
        showUpdateDialog(UpdateUiState.Checking)
        lifecycleScope.launch {
            try {
                val info = UpdateChecker.checkForUpdate(this@MainActivity, forceCheck = true)
                if (info != null) {
                    pendingUpdate = info
                    showUpdateDialog(
                        UpdateUiState.Available(info.version, info.releaseNotes),
                    )
                } else {
                    pendingUpdate = null
                    showUpdateDialog(UpdateUiState.UpToDate)
                }
            } catch (e: Exception) {
                pendingUpdate = null
                showUpdateDialog(UpdateUiState.Error(e.message ?: "Unknown error"))
            }
        }
    }

    private fun showUpdateDialog(state: UpdateUiState) {
        updateUiState = state
        updateDialog?.dismiss()
        updateDialog = UpdateDialogHelper.show(
            context = this,
            currentVersion = installedVersion(),
            state = state,
            onPrimary = { onUpdateDialogPrimary() },
            onDismiss = { dismissUpdateDialog() },
        ).also { it.show() }
    }

    private fun onUpdateDialogPrimary() {
        when (updateUiState) {
            is UpdateUiState.Available -> {
                val info = pendingUpdate ?: return
                showUpdateDialog(UpdateUiState.Downloading(info.version, "Downloading: 0%"))
                lifecycleScope.launch {
                    try {
                        UpdateChecker.downloadAndInstall(this@MainActivity, info) { progress ->
                            updateUiState = UpdateUiState.Downloading(info.version, progress)
                            updateDialog?.setMessage(
                                UpdateDialogHelper.messageFor(
                                    this@MainActivity,
                                    installedVersion(),
                                    updateUiState!!,
                                ),
                            )
                        }
                        dismissUpdateDialog()
                    } catch (e: Exception) {
                        showUpdateDialog(UpdateUiState.Error(e.message ?: "Download failed"))
                    }
                }
            }
            is UpdateUiState.Error -> runManualUpdateCheck()
            else -> Unit
        }
    }

    private fun dismissUpdateDialog() {
        updateDialog?.dismiss()
        updateDialog = null
        updateUiState = null
        pendingUpdate = null
    }

    private fun installedVersion(): String =
        runCatching { BuildConfig.VERSION_NAME }.getOrDefault("?")

    private fun refreshVersionLabel() {
        tvAppVersion.text = getString(R.string.update_version_label, installedVersion())
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
