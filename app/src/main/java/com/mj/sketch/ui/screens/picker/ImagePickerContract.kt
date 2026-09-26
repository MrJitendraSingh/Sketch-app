package com.mj.sketch.ui.screens.picker

import android.content.Context
import android.net.Uri

data class ImagePickerUiState(
    val urlInput: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface ImagePickerIntent {
    data class ImageSelected(val uri: Uri) : ImagePickerIntent
    data class UrlInputChanged(val url: String) : ImagePickerIntent
    data class SubmitUrl(val context: Context) : ImagePickerIntent
    data object ClearErrorMessage : ImagePickerIntent
}

sealed interface ImagePickerEffect {
    data class NavigateToPreview(val uri: Uri) : ImagePickerEffect
    data class ShowToast(val message: String) : ImagePickerEffect
}
