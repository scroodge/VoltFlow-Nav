package com.bridge.yandexbyd

import android.content.Context
import androidx.appcompat.app.AlertDialog

sealed class UpdateUiState {
    data object Checking : UpdateUiState()
    data object UpToDate : UpdateUiState()
    data class Available(
        val version: String,
        val notes: String,
    ) : UpdateUiState()
    data class Downloading(
        val version: String,
        val progress: String,
    ) : UpdateUiState()
    data class Error(val message: String) : UpdateUiState()
}

object UpdateDialogHelper {

    private const val NOTES_MAX = 1200

    fun show(
        context: Context,
        currentVersion: String,
        state: UpdateUiState,
        onPrimary: () -> Unit,
        onDismiss: () -> Unit,
    ): AlertDialog {
        val title = context.getString(R.string.update_dialog_title)
        val message = messageFor(context, currentVersion, state)
        val (positiveLabel, positiveEnabled) = primaryAction(context, state)

        val builder = AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(state !is UpdateUiState.Downloading)
            .setNegativeButton(R.string.update_dialog_close) { _, _ -> onDismiss() }

        if (positiveLabel != null) {
            builder.setPositiveButton(positiveLabel) { _, _ -> onPrimary() }
        }

        val dialog = builder.create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = positiveEnabled
        }
        if (state is UpdateUiState.Downloading) {
            dialog.setCanceledOnTouchOutside(false)
        }
        return dialog
    }

    fun messageFor(
        context: Context,
        currentVersion: String,
        state: UpdateUiState,
    ): String {
        val installed = context.getString(R.string.update_current_version, currentVersion)
        val body = when (state) {
            is UpdateUiState.Checking -> context.getString(R.string.update_checking)
            is UpdateUiState.UpToDate -> context.getString(R.string.update_up_to_date)
            is UpdateUiState.Available -> {
                val notes = truncateNotes(state.notes)
                buildString {
                    append(context.getString(R.string.update_available, state.version))
                    if (notes.isNotBlank()) {
                        append("\n\n")
                        append(context.getString(R.string.update_whats_new))
                        append("\n")
                        append(notes)
                    }
                }
            }
            is UpdateUiState.Downloading -> {
                context.getString(R.string.update_downloading, state.version, state.progress)
            }
            is UpdateUiState.Error -> {
                context.getString(R.string.update_error, state.message)
            }
        }
        return "$installed\n\n$body"
    }

    private fun primaryAction(context: Context, state: UpdateUiState): Pair<String?, Boolean> =
        when (state) {
            is UpdateUiState.Checking -> null to false
            is UpdateUiState.UpToDate -> null to false
            is UpdateUiState.Available ->
                context.getString(R.string.update_download_install) to true
            is UpdateUiState.Downloading -> null to false
            is UpdateUiState.Error ->
                context.getString(R.string.update_retry) to true
        }

    private fun truncateNotes(notes: String): String {
        val trimmed = notes.trim()
        if (trimmed.length <= NOTES_MAX) return trimmed
        return trimmed.take(NOTES_MAX) + "…"
    }
}
