package com.mj.sketch.ui.screens.splash

sealed interface SplashUiState {
    data class Content(val alpha: Float = 0f) : SplashUiState
}

sealed interface SplashIntent {
    data object StartSplash : SplashIntent
}

sealed interface SplashEffect {
    data object NavigateToPicker : SplashEffect
}
