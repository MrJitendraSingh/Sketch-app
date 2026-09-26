package com.mj.sketch.ui.screens.grid

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GridSetupScreen(
    imageUri: Uri,
    viewModel: GridSetupViewModel = viewModel(),
    onStartTracing: (Uri, Int, Int, Int, Float, Float) -> Unit,
    onBackToPicker: () -> Unit,
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(imageUri) {
        viewModel.processIntent(GridSetupIntent.SetImageUri(imageUri, context))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is GridSetupEffect.NavigateToPreview -> {
                    onStartTracing(
                        effect.uri,
                        effect.rows,
                        effect.cols,
                        effect.sectionIndex,
                        effect.sheetWidthMm,
                        effect.sheetHeightMm,
                    )
                }
                is GridSetupEffect.NavigateBack -> onBackToPicker()
                is GridSetupEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    BackHandler {
        viewModel.processIntent(GridSetupIntent.BackClicked)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Paper & Grid Setup",
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.processIntent(GridSetupIntent.BackClicked) }) {
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
        bottomBar = {
            Surface(
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = "Piece ${uiState.selectedSectionIndex + 1} of ${uiState.totalSections}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Grid: ${uiState.cols} cols × ${uiState.rows} rows",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Button(
                        onClick = { viewModel.processIntent(GridSetupIntent.StartTracingClicked) },
                        modifier = Modifier.height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 6.dp),
                        )
                        Text(
                            text = "Start Tracing",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 1. Sheet Size Selection Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "1. Paper Sheet Frame",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        SheetType.entries.forEach { sheetType ->
                            val isSelected = uiState.selectedSheetType == sheetType
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    viewModel.processIntent(GridSetupIntent.SelectSheetType(sheetType))
                                },
                                label = {
                                    Text(
                                        text = sheetType.displayName,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    )
                                },
                                leadingIcon = if (isSelected) {
                                    {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                        )
                                    }
                                } else null,
                            )
                        }
                    }

                    // Custom Sheet Size Inputs if Custom selected
                    if (uiState.selectedSheetType == SheetType.CUSTOM) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            OutlinedTextField(
                                value = uiState.customWidthMm,
                                onValueChange = {
                                    viewModel.processIntent(GridSetupIntent.UpdateCustomWidth(it))
                                },
                                label = { Text("Width (mm)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                            )
                            OutlinedTextField(
                                value = uiState.customHeightMm,
                                onValueChange = {
                                    viewModel.processIntent(GridSetupIntent.UpdateCustomHeight(it))
                                },
                                label = { Text("Height (mm)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                            )
                        }
                    }
                }
            }

            // 2. Paper & Grid Summary
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "2. Grid Division Settings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )

                        if (!uiState.isAutoGrid) {
                            OutlinedButton(
                                onClick = { viewModel.processIntent(GridSetupIntent.ResetAutoGrid) },
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Auto Reset", fontSize = 12.sp)
                            }
                        }
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = "Paper Size: ${uiState.sheetWidthMm.toInt()} × ${uiState.sheetHeightMm.toInt()} mm",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "Fitted Image: ${uiState.imageFittedWidthMm.toInt()} × ${uiState.imageFittedHeightMm.toInt()} mm",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            if ((uiState.verticalMarginMm > 1f) || (uiState.horizontalMarginMm > 1f)) {
                                val marginText = if (uiState.verticalMarginMm > 1f) {
                                    "White Margins (Top/Bottom): ${uiState.verticalMarginMm.toInt()} mm"
                                } else {
                                    "White Margins (Left/Right): ${uiState.horizontalMarginMm.toInt()} mm"
                                }
                                Text(
                                    text = marginText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Text(
                                text = "Each Paper Piece: ${uiState.tileWidthMm.toInt()} × ${uiState.tileHeightMm.toInt()} mm",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = if (uiState.isAutoGrid) {
                                    "✨ Image fitted inside paper without distortion. Grid divides entire paper sheet into phone-sized pieces."
                                } else {
                                    "⚙️ Custom adjusted paper grid."
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }

                    // Steppers for Columns and Rows
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Columns:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { viewModel.processIntent(GridSetupIntent.UpdateCols(uiState.cols - 1)) },
                                enabled = uiState.cols > 1,
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease cols")
                            }
                            Text(
                                text = uiState.cols.toString(),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 8.dp),
                            )
                            IconButton(
                                onClick = { viewModel.processIntent(GridSetupIntent.UpdateCols(uiState.cols + 1)) },
                                enabled = uiState.cols < 10,
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase cols")
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Rows:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { viewModel.processIntent(GridSetupIntent.UpdateRows(uiState.rows - 1)) },
                                enabled = uiState.rows > 1,
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease rows")
                            }
                            Text(
                                text = uiState.rows.toString(),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 8.dp),
                            )
                            IconButton(
                                onClick = { viewModel.processIntent(GridSetupIntent.UpdateRows(uiState.rows + 1)) },
                                enabled = uiState.rows < 10,
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase rows")
                            }
                        }
                    }
                }
            }

            // 3. Grid Preview Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "3. Paper Frame Grid Preview",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "Complete white paper frame with fitted image & divided pieces. Tap any piece to select:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    // Grid Overlay Preview Box
                    GridPreviewView(uiState = uiState) { index ->
                        viewModel.processIntent(GridSetupIntent.SelectSection(index))
                    }
                }
            }
        }
    }
}

@Composable
private fun GridPreviewView(
    uiState: GridSetupUiState,
    onSectionSelected: (Int) -> Unit,
) {
    val paperAspect = uiState.paperAspect

    // Outer Surface representing the White Paper Frame
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        color = Color.White, // White paper frame
        shadowElevation = 4.dp,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(if (paperAspect > 0f) paperAspect else 1f),
            contentAlignment = Alignment.Center,
        ) {
            // Fraction of paper occupied by fitted image
            val widthFraction = if (uiState.sheetWidthMm > 0f) {
                (uiState.imageFittedWidthMm / uiState.sheetWidthMm).coerceIn(0.01f, 1.0f)
            } else 1.0f

            val heightFraction = if (uiState.sheetHeightMm > 0f) {
                (uiState.imageFittedHeightMm / uiState.sheetHeightMm).coerceIn(0.01f, 1.0f)
            } else 1.0f

            // Inner Box representing fitted image centered inside white paper frame
            Box(
                modifier = Modifier
                    .fillMaxWidth(widthFraction)
                    .fillMaxHeight(heightFraction),
            ) {
                uiState.imageUri?.let { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = "Fitted Image on Paper Frame",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            // Grid Overlay spanning the ENTIRE white paper frame
            Column(modifier = Modifier.fillMaxSize()) {
                val rows = uiState.rows
                val cols = uiState.cols

                for (r in 0 until rows) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    ) {
                        for (c in 0 until cols) {
                            val sectionIndex = (r * cols) + c
                            val isSelected = sectionIndex == uiState.selectedSectionIndex

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                                    .border(1.dp, Color.Black.copy(alpha = 0.35f))
                                    .background(
                                        if (isSelected) {
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                                        } else {
                                            Color.Transparent
                                        },
                                    )
                                    .clickable { onSectionSelected(sectionIndex) },
                                contentAlignment = Alignment.Center,
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        Color.Black.copy(alpha = 0.65f)
                                    },
                                    contentColor = if (isSelected) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        Color.White
                                    },
                                    modifier = Modifier.size(28.dp),
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize(),
                                    ) {
                                        Text(
                                            text = (sectionIndex + 1).toString(),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            textAlign = TextAlign.Center,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
