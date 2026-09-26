package com.mj.sketch.ui.screens.preview

import android.net.Uri
import androidx.compose.ui.geometry.Offset

data class PreviewUiState(
    val imageUri: Uri? = null,
    val rows: Int = 1,
    val cols: Int = 1,
    val sectionIndex: Int = 0,
    val isLocked: Boolean = false,
    val scale: Float = 1f,
    val offset: Offset = Offset.Zero,
    val showSectionPicker: Boolean = false,
) {
    val totalSections: Int
        get() = rows * cols

    val currentRow: Int
        get() = if (cols > 0) sectionIndex / cols else 0

    val currentCol: Int
        get() = if (cols > 0) sectionIndex % cols else 0
}

sealed interface PreviewIntent {
    data class SetImageUriAndGrid(
        val uri: Uri,
        val rows: Int = 1,
        val cols: Int = 1,
        val sectionIndex: Int = 0,
    ) : PreviewIntent
    data object NextSection : PreviewIntent
    data object PreviousSection : PreviewIntent
    data class SelectSection(val sectionIndex: Int) : PreviewIntent
    data object ToggleSectionPicker : PreviewIntent
    data class Transform(val zoom: Float, val pan: Offset) : PreviewIntent
    data object ToggleLock : PreviewIntent
    data object ResetZoom : PreviewIntent
    data object BackClicked : PreviewIntent
    data object LockedTouchAttempted : PreviewIntent
}

sealed interface PreviewEffect {
    data object NavigateBack : PreviewEffect
    data class ShowToast(val message: String) : PreviewEffect
}
