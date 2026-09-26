package com.mj.sketch.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class SplashViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Content())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    private val _effect = Channel<SplashEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun processIntent(intent: SplashIntent) {
        when (intent) {
            is SplashIntent.StartSplash -> startSplashTimer()
        }
    }

    private fun startSplashTimer() {
        viewModelScope.launch {
            _uiState.value = SplashUiState.Content(alpha = 1f)
            delay(1500)
            _effect.send(SplashEffect.NavigateToPicker)
        }
    }
}
