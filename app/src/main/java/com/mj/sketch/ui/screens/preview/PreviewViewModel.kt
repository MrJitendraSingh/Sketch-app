package com.mj.sketch.ui.screens.preview

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PreviewViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PreviewUiState())
    val uiState: StateFlow<PreviewUiState> = _uiState.asStateFlow()

    private val _effect = Channel<PreviewEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun processIntent(intent: PreviewIntent) {
        when (intent) {
            is PreviewIntent.SetImageUriAndGrid -> setImageUriAndGrid(
                intent.uri,
                intent.context,
                intent.rows,
                intent.cols,
                intent.sectionIndex,
                intent.sheetWidthMm,
                intent.sheetHeightMm,
            )
            is PreviewIntent.NextSection -> nextSection()
            is PreviewIntent.PreviousSection -> previousSection()
            is PreviewIntent.SelectSection -> selectSection(intent.sectionIndex)
            is PreviewIntent.ToggleSectionPicker -> toggleSectionPicker()
            is PreviewIntent.Transform -> handleTransform(intent.zoom, intent.pan)
            is PreviewIntent.ToggleLock -> toggleLock()
            is PreviewIntent.ResetZoom -> resetZoom()
            is PreviewIntent.BackClicked -> handleBackClick()
            is PreviewIntent.LockedTouchAttempted -> notifyLockedTouch()
        }
    }

    private fun setImageUriAndGrid(
        uri: Uri,
        context: Context,
        rows: Int,
        cols: Int,
        sectionIndex: Int,
        sheetWidthMm: Float,
        sheetHeightMm: Float,
    ) {
        viewModelScope.launch {
            val (w, h) = getImageDimensions(context, uri) ?: Pair(1000, 1000)
            val total = rows * cols
            val validIndex = sectionIndex.coerceIn(0, (total - 1).coerceAtLeast(0))
            _uiState.value = _uiState.value.copy(
                imageUri = uri,
                imageWidth = w,
                imageHeight = h,
                sheetWidthMm = sheetWidthMm,
                sheetHeightMm = sheetHeightMm,
                rows = rows.coerceAtLeast(1),
                cols = cols.coerceAtLeast(1),
                sectionIndex = validIndex,
                scale = 1f,
                offset = Offset.Zero,
            )
        }
    }

    private fun nextSection() {
        val current = _uiState.value.sectionIndex
        val total = _uiState.value.totalSections
        if (current < (total - 1)) {
            selectSection(current + 1)
        }
    }

    private fun previousSection() {
        val current = _uiState.value.sectionIndex
        if (current > 0) {
            selectSection(current - 1)
        }
    }

    private fun selectSection(index: Int) {
        val total = _uiState.value.totalSections
        val validIndex = index.coerceIn(0, (total - 1).coerceAtLeast(0))
        _uiState.value = _uiState.value.copy(
            sectionIndex = validIndex,
            scale = 1f,
            offset = Offset.Zero,
            showSectionPicker = false,
        )
    }

    private fun toggleSectionPicker() {
        _uiState.value = _uiState.value.copy(
            showSectionPicker = !_uiState.value.showSectionPicker,
        )
    }

    private fun handleTransform(zoom: Float, pan: Offset) {
        if (_uiState.value.isLocked) return
        val currentScale = _uiState.value.scale
        val newScale = (currentScale * zoom).coerceIn(1f, 5f)
        val currentOffset = _uiState.value.offset
        val newOffset = Offset(
            x = currentOffset.x + pan.x,
            y = currentOffset.y + pan.y,
        )
        _uiState.value = _uiState.value.copy(scale = newScale, offset = newOffset)
    }

    private fun toggleLock() {
        val nextLockState = !_uiState.value.isLocked
        _uiState.value = _uiState.value.copy(isLocked = nextLockState)
        viewModelScope.launch {
            val message = if (nextLockState) "Screen Locked!" else "Screen Unlocked!"
            _effect.send(PreviewEffect.ShowToast(message))
        }
    }

    private fun resetZoom() {
        if (_uiState.value.isLocked) return
        _uiState.value = _uiState.value.copy(scale = 1f, offset = Offset.Zero)
    }

    private fun handleBackClick() {
        viewModelScope.launch {
            if (_uiState.value.isLocked) {
                _effect.send(PreviewEffect.ShowToast("Screen is locked! Tap the Lock button to unlock."))
            } else {
                _effect.send(PreviewEffect.NavigateBack)
            }
        }
    }

    private fun notifyLockedTouch() {
        viewModelScope.launch {
            _effect.send(PreviewEffect.ShowToast("Screen is locked! Click unlock button to enable touches."))
        }
    }

    private suspend fun getImageDimensions(context: Context, uri: Uri): Pair<Int, Int>? = withContext(Dispatchers.IO) {
        try {
            val scheme = uri.scheme?.lowercase()
            if ((scheme == "http") || (scheme == "https")) {
                val url = java.net.URL(uri.toString())
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                connection.inputStream.use { stream ->
                    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeStream(stream, null, options)
                    if ((options.outWidth > 0) && (options.outHeight > 0)) {
                        Pair(options.outWidth, options.outHeight)
                    } else null
                }
            } else {
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, options)
                }
                if ((options.outWidth > 0) && (options.outHeight > 0)) {
                    Pair(options.outWidth, options.outHeight)
                } else null
            }
        } catch (_: Exception) {
            null
        }
    }
}
