package com.mj.sketch.ui.screens.picker

import android.app.Application
import android.content.Context
import android.net.Uri
import android.webkit.URLUtil
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
import java.io.File
import java.security.MessageDigest

class ImagePickerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProjectRepository.getInstance(application)

    private val _uiState = MutableStateFlow(ImagePickerUiState())
    val uiState: StateFlow<ImagePickerUiState> = _uiState.asStateFlow()

    private val _effect = Channel<ImagePickerEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun processIntent(intent: ImagePickerIntent) {
        when (intent) {
            is ImagePickerIntent.ImageSelected -> {
                saveAndNavigate(intent.uri)
            }
            is ImagePickerIntent.UrlInputChanged -> {
                _uiState.update { it.copy(urlInput = intent.url, errorMessage = null) }
            }
            is ImagePickerIntent.SubmitUrl -> {
                submitUrl(intent.context)
            }
            is ImagePickerIntent.ClearErrorMessage -> {
                _uiState.update { it.copy(errorMessage = null) }
            }
            is ImagePickerIntent.SettingsClicked -> {
                viewModelScope.launch {
                    _effect.send(ImagePickerEffect.NavigateToSettings)
                }
            }
        }
    }

    private fun saveAndNavigate(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveOrUpdateProject(imageUri = uri.toString())
            _effect.send(ImagePickerEffect.NavigateToPreview(uri))
        }
    }

    private fun submitUrl(context: Context) {
        val rawUrl = _uiState.value.urlInput.trim()
        if (rawUrl.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Please enter an image URL") }
            return
        }

        val formattedUrl = if (!rawUrl.startsWith("http://") && !rawUrl.startsWith("https://")) {
            "https://$rawUrl"
        } else {
            rawUrl
        }

        if (!URLUtil.isValidUrl(formattedUrl)) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid URL") }
            return
        }

        downloadAndSaveImage(context, formattedUrl)
    }

    private fun downloadAndSaveImage(context: Context, formattedUrl: String) {
        val fileName = generateFileNameFromUrl(formattedUrl)
        val imagesDir = File(context.filesDir, "downloaded_images")
        if (!imagesDir.exists()) {
            imagesDir.mkdirs()
        }
        val targetFile = File(imagesDir, fileName)

        if (targetFile.exists() && (targetFile.length() > 0)) {
            val localUri = Uri.fromFile(targetFile)
            _uiState.update { it.copy(isLoading = false, errorMessage = null) }
            saveAndNavigate(localUri)
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = java.net.URL(formattedUrl)
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                connection.instanceFollowRedirects = true
                connection.setRequestProperty(
                    "User-Agent",
                    "Mozilla/5.0 (Android; Mobile; rv:100.0) Gecko/100.0 Firefox/100.0",
                )
                connection.connect()

                val responseCode = connection.responseCode
                if (responseCode in 200..299) {
                    val tempFile = File(imagesDir, "$fileName.tmp")
                    connection.inputStream.use { input ->
                        tempFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }

                    if (tempFile.exists() && (tempFile.length() > 0)) {
                        if (targetFile.exists()) targetFile.delete()
                        tempFile.renameTo(targetFile)

                        val downloadedUri = Uri.fromFile(targetFile)
                        _uiState.update { it.copy(isLoading = false) }
                        repository.saveOrUpdateProject(imageUri = downloadedUri.toString())
                        _effect.send(ImagePickerEffect.NavigateToPreview(downloadedUri))
                    } else {
                        tempFile.delete()
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Downloaded file is empty",
                            )
                        }
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Failed to download image (HTTP $responseCode)",
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to download image: ${e.localizedMessage ?: "Network error"}",
                    )
                }
            }
        }
    }

    private fun generateFileNameFromUrl(url: String): String {
        val md5 = MessageDigest.getInstance("MD5")
        val digest = md5.digest(url.trim().toByteArray())
        val hash = digest.joinToString("") { "%02x".format(it) }
        val extension = when {
            url.contains(".png", ignoreCase = true) -> ".png"
            url.contains(".webp", ignoreCase = true) -> ".webp"
            url.contains(".gif", ignoreCase = true) -> ".gif"
            url.contains(".svg", ignoreCase = true) -> ".svg"
            else -> ".jpg"
        }
        return "img_$hash$extension"
    }
}
