package com.lockguard.app.ui.screens.gallery

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lockguard.app.data.repository.IntruderRepository
import com.lockguard.app.domain.model.DetectionSource
import com.lockguard.app.domain.model.IntruderEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

enum class GalleryFilter(val label: String) {
    ALL("All Events"),
    PHOTOS_ONLY("Photos"),
    SYSTEM_SCREEN("Lock Screen"),
    IN_APP("In-App Vault")
}

data class GalleryUiState(
    val selectedIds: Set<Long> = emptySet(),
    val isSelectionMode: Boolean = false,
    val activeFilter: GalleryFilter = GalleryFilter.ALL,
    val showDeleteAllDialog: Boolean = false,
    val showDeleteSelectedDialog: Boolean = false
)

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val intruderRepository: IntruderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GalleryUiState())
    val uiState: StateFlow<GalleryUiState> = _uiState.asStateFlow()

    private val allEventsFlow = intruderRepository.getAllEvents()

    val filteredEvents: StateFlow<List<IntruderEvent>> = combine(
        allEventsFlow,
        _uiState
    ) { events, state ->
        when (state.activeFilter) {
            GalleryFilter.ALL -> events
            GalleryFilter.PHOTOS_ONLY -> events.filter { it.photoPath != null }
            GalleryFilter.SYSTEM_SCREEN -> events.filter { it.source == DetectionSource.SYSTEM_LOCK_SCREEN }
            GalleryFilter.IN_APP -> events.filter { it.source == DetectionSource.IN_APP_PIN_FAILURE }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    suspend fun loadPhotoBitmap(filePath: String): Bitmap? {
        return intruderRepository.loadDecryptedPhoto(filePath)
    }

    fun setFilter(filter: GalleryFilter) {
        _uiState.value = _uiState.value.copy(activeFilter = filter)
    }

    fun toggleSelectionMode() {
        val nextMode = !_uiState.value.isSelectionMode
        _uiState.value = _uiState.value.copy(
            isSelectionMode = nextMode,
            selectedIds = if (nextMode) _uiState.value.selectedIds else emptySet()
        )
    }

    fun toggleEventSelection(id: Long) {
        val current = _uiState.value.selectedIds.toMutableSet()
        if (current.contains(id)) {
            current.remove(id)
        } else {
            current.add(id)
        }
        _uiState.value = _uiState.value.copy(
            selectedIds = current,
            isSelectionMode = current.isNotEmpty()
        )
    }

    fun selectAll(events: List<IntruderEvent>) {
        val allIds = events.map { it.id }.toSet()
        _uiState.value = _uiState.value.copy(selectedIds = allIds, isSelectionMode = true)
    }

    fun clearSelection() {
        _uiState.value = _uiState.value.copy(selectedIds = emptySet(), isSelectionMode = false)
    }

    fun showDeleteSelectedConfirmation(show: Boolean) {
        _uiState.value = _uiState.value.copy(showDeleteSelectedDialog = show)
    }

    fun showDeleteAllConfirmation(show: Boolean) {
        _uiState.value = _uiState.value.copy(showDeleteAllDialog = show)
    }

    fun deleteSelectedEvents() {
        viewModelScope.launch {
            val ids = _uiState.value.selectedIds.toList()
            intruderRepository.deleteEvents(ids)
            clearSelection()
            showDeleteSelectedConfirmation(false)
        }
    }

    fun deleteAllEvents() {
        viewModelScope.launch {
            intruderRepository.deleteAllEvents()
            clearSelection()
            showDeleteAllConfirmation(false)
        }
    }

    fun exportPhoto(context: Context, filePath: String) {
        viewModelScope.launch {
            val decryptedFile = intruderRepository.preparePhotoForExport(filePath) ?: return@launch
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                decryptedFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "LockGuard Intruder Evidence")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Export Intruder Evidence"))
        }
    }
}
