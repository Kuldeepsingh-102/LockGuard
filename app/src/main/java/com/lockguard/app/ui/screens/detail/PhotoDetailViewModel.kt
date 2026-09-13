package com.lockguard.app.ui.screens.detail

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lockguard.app.data.repository.IntruderRepository
import com.lockguard.app.domain.model.IntruderEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PhotoDetailUiState(
    val event: IntruderEvent? = null,
    val bitmap: Bitmap? = null,
    val isLoading: Boolean = true,
    val isDeleted: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val showExportDialog: Boolean = false
)

@HiltViewModel
class PhotoDetailViewModel @Inject constructor(
    private val intruderRepository: IntruderRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val eventId: Long = savedStateHandle.get<Long>("eventId")
        ?: savedStateHandle.get<String>("eventId")?.toLongOrNull()
        ?: 0L

    private val _uiState = MutableStateFlow(PhotoDetailUiState())
    val uiState: StateFlow<PhotoDetailUiState> = _uiState.asStateFlow()

    init {
        loadEventDetails()
    }

    private fun loadEventDetails() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val event = intruderRepository.getEventById(eventId)
            if (event != null) {
                val bitmap = event.photoPath?.let { intruderRepository.loadDecryptedPhoto(it) }
                _uiState.value = _uiState.value.copy(
                    event = event,
                    bitmap = bitmap,
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun showDeleteConfirmation(show: Boolean) {
        _uiState.value = _uiState.value.copy(showDeleteDialog = show)
    }

    fun showExportConfirmation(show: Boolean) {
        _uiState.value = _uiState.value.copy(showExportDialog = show)
    }

    fun deleteEvent() {
        viewModelScope.launch {
            val event = _uiState.value.event ?: return@launch
            val success = intruderRepository.deleteEvent(event)
            if (success) {
                _uiState.value = _uiState.value.copy(isDeleted = true, showDeleteDialog = false)
            }
        }
    }

    fun confirmAndExport(context: Context) {
        viewModelScope.launch {
            val event = _uiState.value.event ?: return@launch
            val photoPath = event.photoPath ?: return@launch

            val tempFile = intruderRepository.preparePhotoForExport(photoPath) ?: return@launch
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                tempFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "LockGuard Intruder Evidence [Confidential]")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            _uiState.value = _uiState.value.copy(showExportDialog = false)
            context.startActivity(Intent.createChooser(shareIntent, "Share Encrypted Evidence"))
        }
    }
}
