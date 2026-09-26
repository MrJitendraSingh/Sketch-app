package com.mj.sketch.ui.screens.history

import android.app.Application
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mj.sketch.data.repository.ProjectRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProjectRepository.getInstance(application)

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private val _effect = Channel<HistoryEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        viewModelScope.launch {
            repository.allProjects.collect { projects ->
                _uiState.update { it.copy(projects = projects) }
            }
        }
    }

    fun processIntent(intent: HistoryIntent) {
        when (intent) {
            is HistoryIntent.OpenProject -> {
                val project = intent.project
                viewModelScope.launch(Dispatchers.IO) {
                    repository.saveOrUpdateProject(
                        imageUri = project.imageUri,
                        rows = project.rows,
                        cols = project.cols,
                        sectionIndex = project.sectionIndex,
                        sheetWidthMm = project.sheetWidthMm,
                        sheetHeightMm = project.sheetHeightMm,
                    )
                    _effect.send(HistoryEffect.OpenImageGrid(project.imageUri.toUri()))
                }
            }
            is HistoryIntent.DeleteProject -> {
                viewModelScope.launch(Dispatchers.IO) {
                    repository.deleteProject(intent.id)
                }
            }
            is HistoryIntent.BackClicked -> {
                viewModelScope.launch {
                    _effect.send(HistoryEffect.NavigateBack)
                }
            }
        }
    }
}
