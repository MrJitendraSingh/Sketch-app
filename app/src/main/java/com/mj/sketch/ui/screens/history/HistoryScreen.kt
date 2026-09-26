package com.mj.sketch.ui.screens.history

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.mj.sketch.data.local.ProjectEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = viewModel(),
    onOpenProject: (Uri) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is HistoryEffect.OpenImageGrid -> onOpenProject(effect.uri)
                is HistoryEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "History",
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.processIntent(HistoryIntent.BackClicked) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
        ) {
            if (uiState.projects.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No projects in history yet",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Images you pick and trace will appear here automatically.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(
                        items = uiState.projects,
                        key = { it.id },
                    ) { project ->
                        HistoryItemCard(
                            project = project,
                            onClick = {
                                viewModel.processIntent(HistoryIntent.OpenProject(project))
                            },
                            onDelete = {
                                viewModel.processIntent(HistoryIntent.DeleteProject(project.id))
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryItemCard(
    project: ProjectEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = project.imageUri,
            contentDescription = "Project image",
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            contentScale = ContentScale.Crop,
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = getDisplayName(project.imageUri),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Grid: ${project.rows} × ${project.cols}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "Last used: ${formatTimestamp(project.lastUsedAt)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        IconButton(onClick = onDelete, modifier = Modifier.align(Alignment.Bottom)) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete project",
                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
            )
        }
    }
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 4.dp),
        color = MaterialTheme.colorScheme.outlineVariant,
    )
}

private fun getDisplayName(uriString: String): String {
    return try {
        val uri = uriString.toUri()
        val path = uri.lastPathSegment
        if (!path.isNullOrBlank()) {
            path.substringAfterLast('/')
        } else {
            "Project Image"
        }
    } catch (_: Exception) {
        "Project Image"
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy - HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
