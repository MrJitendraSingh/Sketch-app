package com.mj.sketch.ui.screens.grid

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlin.math.max

class GridSetupViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(GridSetupUiState())
    val uiState: StateFlow<GridSetupUiState> = _uiState.asStateFlow()

    private val _effect = Channel<GridSetupEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun processIntent(intent: GridSetupIntent) {
        when (intent) {
            is GridSetupIntent.SetImageUri -> setImageUri(intent.uri, intent.context)
            is GridSetupIntent.SelectSheetType -> selectSheetType(intent.sheetType)
            is GridSetupIntent.UpdateCustomWidth -> updateCustomWidth(intent.widthMm)
            is GridSetupIntent.UpdateCustomHeight -> updateCustomHeight(intent.heightMm)
            is GridSetupIntent.UpdateCols -> updateCols(intent.cols)
            is GridSetupIntent.UpdateRows -> updateRows(intent.rows)
            is GridSetupIntent.ResetAutoGrid -> resetAutoGrid()
            is GridSetupIntent.SelectSection -> selectSection(intent.index)
            is GridSetupIntent.StartTracingClicked -> startTracing()
            is GridSetupIntent.BackClicked -> handleBack()
        }
    }

    private fun setImageUri(uri: Uri, context: Context) {
        val (w, h) = getImageDimensions(context, uri) ?: Pair(1000, 1000)
        val (phoneW, phoneH) = getPhoneDisplayDimensionsMm(context)

        val newState = _uiState.value.copy(
            imageUri = uri,
            imageWidth = w,
            imageHeight = h,
            phoneWidthMm = phoneW,
            phoneHeightMm = phoneH,
        )

        val autoCols = newState.calculateAutoCols()
        val autoRows = newState.calculateAutoRows()

        _uiState.value = newState.copy(
            cols = autoCols,
            rows = autoRows,
            isAutoGrid = true,
            selectedSectionIndex = 0,
        )
    }

    private fun selectSheetType(sheetType: SheetType) {
        val updated = _uiState.value.copy(selectedSheetType = sheetType)
        recalculateGridIfAuto(updated)
    }

    private fun updateCustomWidth(widthMm: String) {
        val updated = _uiState.value.copy(customWidthMm = widthMm)
        recalculateGridIfAuto(updated)
    }

    private fun updateCustomHeight(heightMm: String) {
        val updated = _uiState.value.copy(customHeightMm = heightMm)
        recalculateGridIfAuto(updated)
    }

    private fun recalculateGridIfAuto(currentState: GridSetupUiState) {
        val newState = if (currentState.isAutoGrid) {
            val autoCols = currentState.calculateAutoCols()
            val autoRows = currentState.calculateAutoRows()
            currentState.copy(
                cols = autoCols,
                rows = autoRows,
                selectedSectionIndex = 0,
            )
        } else {
            val maxSection = max(0, currentState.totalSections - 1)
            currentState.copy(
                selectedSectionIndex = currentState.selectedSectionIndex.coerceIn(0, maxSection),
            )
        }
        _uiState.value = newState
    }

    private fun updateCols(cols: Int) {
        val validCols = cols.coerceIn(1, 10)
        val updated = _uiState.value.copy(
            cols = validCols,
            isAutoGrid = false,
        )
        val maxSection = max(0, updated.totalSections - 1)
        _uiState.value = updated.copy(
            selectedSectionIndex = updated.selectedSectionIndex.coerceIn(0, maxSection),
        )
    }

    private fun updateRows(rows: Int) {
        val validRows = rows.coerceIn(1, 10)
        val updated = _uiState.value.copy(
            rows = validRows,
            isAutoGrid = false,
        )
        val maxSection = max(0, updated.totalSections - 1)
        _uiState.value = updated.copy(
            selectedSectionIndex = updated.selectedSectionIndex.coerceIn(0, maxSection),
        )
    }

    private fun resetAutoGrid() {
        val currentState = _uiState.value.copy(isAutoGrid = true)
        val autoCols = currentState.calculateAutoCols()
        val autoRows = currentState.calculateAutoRows()
        _uiState.value = currentState.copy(
            cols = autoCols,
            rows = autoRows,
            selectedSectionIndex = 0,
        )
    }

    private fun selectSection(index: Int) {
        val maxSection = max(0, _uiState.value.totalSections - 1)
        _uiState.value = _uiState.value.copy(
            selectedSectionIndex = index.coerceIn(0, maxSection),
        )
    }

    private fun startTracing() {
        val uri = _uiState.value.imageUri ?: return
        viewModelScope.launch {
            _effect.send(
                GridSetupEffect.NavigateToPreview(
                    uri = uri,
                    rows = _uiState.value.rows,
                    cols = _uiState.value.cols,
                    sectionIndex = _uiState.value.selectedSectionIndex,
                    sheetWidthMm = _uiState.value.sheetWidthMm,
                    sheetHeightMm = _uiState.value.sheetHeightMm,
                ),
            )
        }
    }

    private fun handleBack() {
        viewModelScope.launch {
            _effect.send(GridSetupEffect.NavigateBack)
        }
    }

    private fun getImageDimensions(context: Context, uri: Uri): Pair<Int, Int>? {
        return try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }
            if ((options.outWidth > 0) && (options.outHeight > 0)) {
                Pair(options.outWidth, options.outHeight)
            } else null
        } catch (_: Exception) {
            null
        }
    }

    private fun getPhoneDisplayDimensionsMm(context: Context): Pair<Float, Float> {
        val dm = context.resources.displayMetrics
        val xdpi = if (dm.xdpi > 0) dm.xdpi else 160f
        val ydpi = if (dm.ydpi > 0) dm.ydpi else 160f
        val widthMm = (dm.widthPixels / xdpi) * 25.4f
        val heightMm = (dm.heightPixels / ydpi) * 25.4f
        return Pair(widthMm, heightMm)
    }
}
