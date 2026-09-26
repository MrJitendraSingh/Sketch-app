package com.mj.sketch.ui.screens.preview

import android.content.Context
import android.net.Uri
import androidx.compose.ui.geometry.Offset

data class PreviewUiState(
    val imageUri: Uri? = null,
    val imageWidth: Int = 0,
    val imageHeight: Int = 0,
    val sheetWidthMm: Float = 210f,
    val sheetHeightMm: Float = 297f,
    val rows: Int = 1,
    val cols: Int = 1,
    val sectionIndex: Int = 0,
    val isLocked: Boolean = false,
    val scale: Float = 1f,
    val offset: Offset = Offset.Zero,
    val showSectionPicker: Boolean = false,
) {
    val imageAspect: Float
        get() = if (imageHeight > 0) imageWidth.toFloat() / imageHeight else 1f

    val paperAspect: Float
        get() = if (sheetHeightMm > 0f) sheetWidthMm / sheetHeightMm else 1f

    val imageFittedWidthFraction: Float
        get() {
            return if (imageAspect > paperAspect) {
                1.0f
            } else {
                (imageAspect / paperAspect).coerceIn(0.01f, 1.0f)
            }
        }

    val imageFittedHeightFraction: Float
        get() {
            return if (imageAspect > paperAspect) {
                (paperAspect / imageAspect).coerceIn(0.01f, 1.0f)
            } else {
                1.0f
            }
        }

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
        val context: Context,
        val rows: Int = 1,
        val cols: Int = 1,
        val sectionIndex: Int = 0,
        val sheetWidthMm: Float = 210f,
        val sheetHeightMm: Float = 297f,
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
