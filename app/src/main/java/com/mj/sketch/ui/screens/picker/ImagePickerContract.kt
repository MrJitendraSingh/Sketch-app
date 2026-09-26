package com.mj.sketch.ui.screens.picker

import android.net.Uri

sealed interface ImagePickerUiState {
    data object Idle : ImagePickerUiState
}

sealed interface ImagePickerIntent {
    data class ImageSelected(val uri: Uri) : ImagePickerIntent
}

sealed interface ImagePickerEffect {
    data class NavigateToPreview(val uri: Uri) : ImagePickerEffect
}
