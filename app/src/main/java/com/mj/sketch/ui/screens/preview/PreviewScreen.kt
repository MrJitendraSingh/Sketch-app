package com.mj.sketch.ui.screens.preview

import android.app.Activity
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewScreen(
    imageUri: Uri,
    rows: Int = 1,
    cols: Int = 1,
    sectionIndex: Int = 0,
    sheetWidthMm: Float = 210f,
    sheetHeightMm: Float = 297f,
    viewModel: PreviewViewModel = viewModel(),
    onBackToPicker: () -> Unit,
) {
    val context = LocalContext.current
    val view = LocalView.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Hide/show system status bar and navigation bar when locked/unlocked
    DisposableEffect(uiState.isLocked) {
        val activity = context as? Activity
        val window = activity?.window
        if (window != null) {
            val insetsController = WindowInsetsControllerCompat(window, view)
            if (uiState.isLocked) {
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
                insetsController.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
        onDispose {
            val activityDispose = context as? Activity
            val windowDispose = activityDispose?.window
            if (windowDispose != null) {
                WindowInsetsControllerCompat(windowDispose, view).show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    LaunchedEffect(imageUri, rows, cols, sectionIndex, sheetWidthMm, sheetHeightMm) {
        viewModel.processIntent(
            PreviewIntent.SetImageUriAndGrid(
                uri = imageUri,
                context = context,
                rows = rows,
                cols = cols,
                sectionIndex = sectionIndex,
                sheetWidthMm = sheetWidthMm,
                sheetHeightMm = sheetHeightMm,
            ),
        )
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is PreviewEffect.NavigateBack -> onBackToPicker()
                is PreviewEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    BackHandler {
        viewModel.processIntent(PreviewIntent.BackClicked)
    }

    // Outer root container: full-screen Box
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        // 1. Paper Frame & Image layer in full screen preserving section aspect ratio
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(uiState.isLocked) {
                    if (!uiState.isLocked) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            viewModel.processIntent(PreviewIntent.Transform(zoom, pan))
                        }
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            val c = uiState.currentCol
            val r = uiState.currentRow
            val cols = uiState.cols
            val rows = uiState.rows

            val sectionAspect = if ((cols > 0) && (rows > 0) && (uiState.paperAspect > 0f)) {
                uiState.paperAspect * (rows.toFloat() / cols.toFloat())
            } else 1f

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .aspectRatio(if (sectionAspect > 0f) sectionAspect else 1f)
                    .clipToBounds(),
                contentAlignment = Alignment.Center,
            ) {
                val secWidth = constraints.maxWidth.toFloat()
                val secHeight = constraints.maxHeight.toFloat()

                // White Paper Frame Layer for section (r, c)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            transformOrigin = TransformOrigin(0f, 0f),
                            scaleX = cols * uiState.scale,
                            scaleY = rows * uiState.scale,
                            translationX = (-c * secWidth) + uiState.offset.x,
                            translationY = (-r * secHeight) + uiState.offset.y,
                        )
                        .background(Color.White),
                ) {
                    val fw = uiState.imageFittedWidthFraction
                    val fh = uiState.imageFittedHeightFraction

                    // Fitted Image centered on Paper Frame
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fw.coerceIn(0.01f, 1.0f))
                            .fillMaxHeight(fh.coerceIn(0.01f, 1.0f))
                            .align(Alignment.Center),
                    ) {
                        uiState.imageUri?.let { uri ->
                            AsyncImage(
                                model = uri,
                                contentDescription = "Tracing Piece ${uiState.sectionIndex + 1}",
                                contentScale = ContentScale.FillBounds,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }
                }
            }
        }

        // 2. Touch interceptor when locked (blocks all touch gestures on image)
        if (uiState.isLocked) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            down.consume()

                            viewModel.processIntent(PreviewIntent.LockedTouchAttempted)

                            do {
                                val event = awaitPointerEvent()
                                event.changes.forEach { it.consume() }
                            } while (event.changes.any { it.pressed })
                        }
                    },
            )
        }

        // 3. Overlay Column containing UI buttons and controls
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // Top Overlay Bar (visible when unlocked)
            if (!uiState.isLocked) {
                Surface(
                    color = Color.Black.copy(alpha = 0.5f),
                    contentColor = Color.White,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(
                            onClick = { viewModel.processIntent(PreviewIntent.BackClicked) },
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                            )
                        }

                        Text(
                            text = if (uiState.totalSections > 1) {
                                "Piece ${uiState.sectionIndex + 1} of ${uiState.totalSections}"
                            } else {
                                "Preview"
                            },
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 18.sp,
                        )

                        Row {
                            if (uiState.totalSections > 1) {
                                IconButton(
                                    onClick = { viewModel.processIntent(PreviewIntent.ToggleSectionPicker) },
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.GridView,
                                        contentDescription = "Grid Sections",
                                        tint = Color.White,
                                    )
                                }
                            }

                            val isZoomed = (uiState.scale != 1f) || (uiState.offset != androidx.compose.ui.geometry.Offset.Zero)
                            if (isZoomed) {
                                IconButton(onClick = { viewModel.processIntent(PreviewIntent.ResetZoom) }) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Reset Zoom",
                                        tint = Color.White,
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(1.dp))
            }

            // Bottom Overlay Bar (visible when unlocked and totalSections > 1)
            if (!uiState.isLocked && (uiState.totalSections > 1)) {
                Surface(
                    color = Color.Black.copy(alpha = 0.5f),
                    contentColor = Color.White,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.processIntent(PreviewIntent.PreviousSection) },
                            enabled = uiState.sectionIndex > 0,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.NavigateBefore,
                                contentDescription = "Previous Section",
                                tint = Color.White,
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Prev", color = Color.White)
                        }

                        TextButton(
                            onClick = {
                                viewModel.processIntent(PreviewIntent.ToggleSectionPicker)
                            },
                        ) {
                            Text(
                                text = "Piece ${uiState.sectionIndex + 1} of ${uiState.totalSections}",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 15.sp,
                            )
                        }

                        OutlinedButton(
                            onClick = { viewModel.processIntent(PreviewIntent.NextSection) },
                            enabled = uiState.sectionIndex < (uiState.totalSections - 1),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                        ) {
                            Text("Next", color = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.NavigateNext,
                                contentDescription = "Next Section",
                                tint = Color.White,
                            )
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(1.dp))
            }
        }

        // 4. Circular Transparent Lock/Unlock Button (Always floating at bottom corner)
        Surface(
            onClick = { viewModel.processIntent(PreviewIntent.ToggleLock) },
            shape = CircleShape,
            color = Color.Black.copy(alpha = 0.3f),
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = 24.dp,
                    bottom = if (!uiState.isLocked && (uiState.totalSections > 1)) 100.dp else 24.dp,
                )
                .size(56.dp)
                .border(1.5.dp, Color.White.copy(alpha = 0.7f), CircleShape),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize(),
            ) {
                Icon(
                    imageVector = if (uiState.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                    contentDescription = if (uiState.isLocked) "Unlock Screen" else "Lock Screen",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp),
                )
            }
        }

        // Section Picker Dialog
        if (uiState.showSectionPicker) {
            AlertDialog(
                onDismissRequest = { viewModel.processIntent(PreviewIntent.ToggleSectionPicker) },
                title = {
                    Text(
                        text = "Select Piece to Trace",
                        fontWeight = FontWeight.Bold,
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Tap any paper piece to switch:",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        SectionGridPicker(
                            rows = uiState.rows,
                            cols = uiState.cols,
                            currentSectionIndex = uiState.sectionIndex,
                            imageUri = uiState.imageUri,
                            paperAspect = uiState.paperAspect,
                            imageWidthFraction = uiState.imageFittedWidthFraction,
                            imageHeightFraction = uiState.imageFittedHeightFraction,
                        ) { index ->
                            viewModel.processIntent(PreviewIntent.SelectSection(index))
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.processIntent(PreviewIntent.ToggleSectionPicker) }) {
                        Text("Close")
                    }
                },
                containerColor = Color.White
            )
        }
    }
}

@Composable
private fun SectionGridPicker(
    rows: Int,
    cols: Int,
    currentSectionIndex: Int,
    imageUri: Uri?,
    paperAspect: Float,
    imageWidthFraction: Float,
    imageHeightFraction: Float,
    onSectionClick: (Int) -> Unit,
) {
    Surface(
        modifier = Modifier
            .height(240.dp),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(if (paperAspect > 0f) paperAspect else 1f)
                    .background(Color.White),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(imageWidthFraction.coerceIn(0.01f, 1.0f))
                        .fillMaxHeight(imageHeightFraction.coerceIn(0.01f, 1.0f))
                        .align(Alignment.Center),
                ) {
                    imageUri?.let { uri ->
                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }

                Column(modifier = Modifier.fillMaxSize()) {
                    for (r in 0 until rows) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                        ) {
                            for (c in 0 until cols) {
                                val sectionIndex = (r * cols) + c
                                val isSelected = sectionIndex == currentSectionIndex

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxSize()
                                        .border(1.dp, Color.Black.copy(alpha = 0.35f))
                                        .background(
                                            if (isSelected) {
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                                            } else {
                                                Color.Transparent
                                            },
                                        )
                                        .clickable { onSectionClick(sectionIndex) },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isSelected) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            Color.Black.copy(alpha = 0.7f)
                                        },
                                        contentColor = Color.White,
                                        modifier = Modifier.size(24.dp),
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.fillMaxSize(),
                                        ) {
                                            Text(
                                                text = (sectionIndex + 1).toString(),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
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
}
