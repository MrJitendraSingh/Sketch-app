package com.mj.sketch.ui.screens.history

import android.net.Uri
import com.mj.sketch.data.local.ProjectEntity

data class HistoryUiState(
    val projects: List<ProjectEntity> = emptyList(),
)

sealed interface HistoryIntent {
    data class OpenProject(val project: ProjectEntity) : HistoryIntent
    data class DeleteProject(val id: Long) : HistoryIntent
    data object BackClicked : HistoryIntent
}

sealed interface HistoryEffect {
    data class OpenImageGrid(val uri: Uri) : HistoryEffect
    data object NavigateBack : HistoryEffect
}
