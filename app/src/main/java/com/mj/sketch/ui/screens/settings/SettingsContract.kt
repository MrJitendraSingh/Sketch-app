package com.mj.sketch.ui.screens.settings

import android.content.Context

data class SettingsUiState(
    val appVersionName: String = "1.0.0",
)

sealed interface SettingsIntent {
    data class LoadVersionName(val context: Context) : SettingsIntent
    data class OpenPrivacyPolicy(val context: Context) : SettingsIntent
    data class OpenSupportEmail(val context: Context) : SettingsIntent
    data object HistoryClicked : SettingsIntent
    data object BackClicked : SettingsIntent
}

sealed interface SettingsEffect {
    data object NavigateToHistory : SettingsEffect
    data object NavigateBack : SettingsEffect
    data class OpenWebUrl(val url: String) : SettingsEffect
    data class OpenEmailClient(val email: String, val subject: String) : SettingsEffect
    data class ShowToast(val message: String) : SettingsEffect
}
