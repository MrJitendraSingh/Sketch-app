package com.mj.sketch.ui.screens.picker

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImagePickerScreen(
    viewModel: ImagePickerViewModel = viewModel(),
    onImageSelected: (Uri) -> Unit,
    onOpenSettings: () -> Unit,
) {
    val context = LocalContext.current
    @Suppress("DEPRECATION")
    val clipboardManager = LocalClipboardManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ImagePickerEffect.NavigateToPreview -> onImageSelected(effect.uri)
                is ImagePickerEffect.NavigateToSettings -> onOpenSettings()
                is ImagePickerEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri: Uri? ->
        uri?.let { viewModel.processIntent(ImagePickerIntent.ImageSelected(it, context)) }
    }

    val getContentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        uri?.let { viewModel.processIntent(ImagePickerIntent.ImageSelected(it, context)) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Sketch",
                        fontWeight = FontWeight.Bold,
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.processIntent(ImagePickerIntent.SettingsClicked)
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = "Select Image Icon",
                    modifier = Modifier.size(44.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Pick an Image",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Select an image from your gallery or paste a public web link to download and trace.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Option 1: Device Gallery
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "From Gallery",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Choose a photo stored on your device.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(context)) {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                                )
                            } else {
                                getContentLauncher.launch("image/*")
                            }
                        },
                        enabled = !uiState.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Select Image from Device",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // OR Divider
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
                Text(
                    text = "  OR  ",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Option 2: Image URL / Link
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Use Image Link",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Paste a public image URL. The app will download and save it for offline tracing.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (uiState.isLoading) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    OutlinedTextField(
                        value = uiState.urlInput,
                        onValueChange = {
                            viewModel.processIntent(ImagePickerIntent.UrlInputChanged(it))
                        },
                        enabled = !uiState.isLoading,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Image URL") },
                        placeholder = { Text("https://abc.com/img.jpg") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = null,
                            )
                        },
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (uiState.urlInput.isNotEmpty() && !uiState.isLoading) {
                                    IconButton(
                                        onClick = {
                                            viewModel.processIntent(ImagePickerIntent.UrlInputChanged(""))
                                        },
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear text",
                                        )
                                    }
                                }
                                IconButton(
                                    enabled = !uiState.isLoading,
                                    onClick = {
                                        val clipText = clipboardManager.getText()?.text
                                        if (!clipText.isNullOrBlank()) {
                                            viewModel.processIntent(
                                                ImagePickerIntent.UrlInputChanged(clipText),
                                            )
                                        }
                                    },
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentPaste,
                                        contentDescription = "Paste from clipboard",
                                    )
                                }
                            }
                        },
                        isError = uiState.errorMessage != null,
                        supportingText = if (uiState.errorMessage != null) {
                            {
                                Text(
                                    text = uiState.errorMessage!!,
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                        } else null,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Uri,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                                viewModel.processIntent(ImagePickerIntent.SubmitUrl(context))
                            },
                        ),
                        shape = RoundedCornerShape(12.dp),
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            keyboardController?.hide()
                            viewModel.processIntent(ImagePickerIntent.SubmitUrl(context))
                        },
                        enabled = !uiState.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.5.dp,
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Downloading Image...",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        } else {
                            Text(
                                text = "Load Image Link",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
