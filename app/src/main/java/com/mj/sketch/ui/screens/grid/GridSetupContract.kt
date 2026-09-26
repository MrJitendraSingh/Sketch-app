package com.mj.sketch.ui.screens.grid

import android.content.Context
import android.net.Uri
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

enum class SheetType(val displayName: String, val widthMm: Float, val heightMm: Float) {
    A4("A4 (210 × 297 mm)", 210f, 297f),
    A3("A3 (297 × 420 mm)", 297f, 420f),
    A5("A5 (148 × 210 mm)", 148f, 210f),
    LETTER("Letter (216 × 279 mm)", 215.9f, 279.4f),
    CUSTOM("Custom Size", 200f, 200f)
}

data class GridSetupUiState(
    val imageUri: Uri? = null,
    val imageWidth: Int = 0,
    val imageHeight: Int = 0,
    val selectedSheetType: SheetType = SheetType.A4,
    val customWidthMm: String = "200",
    val customHeightMm: String = "200",
    val cols: Int = 1,
    val rows: Int = 1,
    val isAutoGrid: Boolean = true,
    val selectedSectionIndex: Int = 0,
    val phoneWidthMm: Float = 70f,
    val phoneHeightMm: Float = 140f,
) {
    val imageAspect: Float
        get() = if (imageHeight > 0) imageWidth.toFloat() / imageHeight else 1f

    val sheetWidthMm: Float
        get() {
            val (baseW, baseH) = if (selectedSheetType == SheetType.CUSTOM) {
                val w = customWidthMm.toFloatOrNull() ?: 200f
                val h = customHeightMm.toFloatOrNull() ?: 200f
                Pair(w, h)
            } else {
                Pair(selectedSheetType.widthMm, selectedSheetType.heightMm)
            }
            return if (imageAspect > 1.0f) {
                max(baseW, baseH)
            } else {
                min(baseW, baseH)
            }
        }

    val sheetHeightMm: Float
        get() {
            val (baseW, baseH) = if (selectedSheetType == SheetType.CUSTOM) {
                val w = customWidthMm.toFloatOrNull() ?: 200f
                val h = customHeightMm.toFloatOrNull() ?: 200f
                Pair(w, h)
            } else {
                Pair(selectedSheetType.widthMm, selectedSheetType.heightMm)
            }
            return if (imageAspect > 1.0f) {
                min(baseW, baseH)
            } else {
                max(baseW, baseH)
            }
        }

    val paperAspect: Float
        get() = if (sheetHeightMm > 0f) sheetWidthMm / sheetHeightMm else 1f

    val imageFittedWidthMm: Float
        get() {
            return if (imageAspect > paperAspect) {
                sheetWidthMm
            } else {
                sheetHeightMm * imageAspect
            }
        }

    val imageFittedHeightMm: Float
        get() {
            return if (imageAspect > paperAspect) {
                sheetWidthMm / imageAspect
            } else {
                sheetHeightMm
            }
        }

    val horizontalMarginMm: Float
        get() = max(0f, (sheetWidthMm - imageFittedWidthMm) / 2f)

    val verticalMarginMm: Float
        get() = max(0f, (sheetHeightMm - imageFittedHeightMm) / 2f)

    val tileWidthMm: Float
        get() = if (cols > 0) sheetWidthMm / cols else sheetWidthMm

    val tileHeightMm: Float
        get() = if (rows > 0) sheetHeightMm / rows else sheetHeightMm

    val totalSections: Int
        get() = cols * rows

    fun calculateAutoCols(): Int {
        val calculated = ceil(sheetWidthMm / phoneWidthMm).toInt()
        return max(1, calculated)
    }

    fun calculateAutoRows(): Int {
        val calculated = ceil(sheetHeightMm / phoneHeightMm).toInt()
        return max(1, calculated)
    }
}

sealed interface GridSetupIntent {
    data class SetImageUri(val uri: Uri, val context: Context) : GridSetupIntent
    data class SelectSheetType(val sheetType: SheetType) : GridSetupIntent
    data class UpdateCustomWidth(val widthMm: String) : GridSetupIntent
    data class UpdateCustomHeight(val heightMm: String) : GridSetupIntent
    data class UpdateCols(val cols: Int) : GridSetupIntent
    data class UpdateRows(val rows: Int) : GridSetupIntent
    data object ResetAutoGrid : GridSetupIntent
    data class SelectSection(val index: Int) : GridSetupIntent
    data object StartTracingClicked : GridSetupIntent
    data object BackClicked : GridSetupIntent
}

sealed interface GridSetupEffect {
    data class NavigateToPreview(
        val uri: Uri,
        val rows: Int,
        val cols: Int,
        val sectionIndex: Int,
        val sheetWidthMm: Float,
        val sheetHeightMm: Float,
    ) : GridSetupEffect
    data object NavigateBack : GridSetupEffect
    data class ShowToast(val message: String) : GridSetupEffect
}
