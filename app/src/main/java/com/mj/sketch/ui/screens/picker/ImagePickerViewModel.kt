package com.mj.sketch.ui.screens.picker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class ImagePickerViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<ImagePickerUiState>(ImagePickerUiState.Idle)
    val uiState: StateFlow<ImagePickerUiState> = _uiState.asStateFlow()

    private val _effect = Channel<ImagePickerEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun processIntent(intent: ImagePickerIntent) {
        when (intent) {
            is ImagePickerIntent.ImageSelected -> {
                viewModelScope.launch {
                    _effect.send(ImagePickerEffect.NavigateToPreview(intent.uri))
                }
            }
        }
    }
}
