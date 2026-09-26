package com.mj.sketch.ui.screens.settings

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _effect = Channel<SettingsEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun processIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.LoadVersionName -> loadVersionName(intent.context)
            is SettingsIntent.HistoryClicked -> {
                viewModelScope.launch {
                    _effect.send(SettingsEffect.NavigateToHistory)
                }
            }
            is SettingsIntent.OpenPrivacyPolicy -> {
                viewModelScope.launch {
                    _effect.send(SettingsEffect.OpenWebUrl("https://www.freeprivacypolicy.com/live/4f518e79-8f20-4e00-8e41-36f8acdf05f6"))
                }
            }
            is SettingsIntent.OpenSupportEmail -> {
                viewModelScope.launch {
                    _effect.send(SettingsEffect.OpenEmailClient("vigyaanam.in@gmail.com", "Sketch App Support"))
                }
            }
            is SettingsIntent.BackClicked -> {
                viewModelScope.launch {
                    _effect.send(SettingsEffect.NavigateBack)
                }
            }
        }
    }

    private fun loadVersionName(context: Context) {
        val versionName = try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: "1.0.0"
        } catch (_: Exception) {
            "1.0.0"
        }
        _uiState.update { it.copy(appVersionName = versionName) }
    }
}
