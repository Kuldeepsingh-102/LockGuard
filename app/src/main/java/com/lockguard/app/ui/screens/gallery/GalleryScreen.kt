package com.lockguard.app.ui.screens.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.SelectAll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lockguard.app.ui.components.EmptyStateView
import com.lockguard.app.ui.components.IntruderPhotoCard
import com.lockguard.app.ui.components.LockGuardTopBar
import com.lockguard.app.ui.theme.ElectricBlue
import com.lockguard.app.ui.theme.Navy800
import com.lockguard.app.ui.theme.Navy950
import com.lockguard.app.ui.theme.StatusRed

@Composable
fun GalleryScreen(
    viewModel: GalleryViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (Long) -> Unit
) {
    val events by viewModel.filteredEvents.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy950)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            if (uiState.isSelectionMode) {
                // Selection Toolbar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Cancel",
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${uiState.selectedIds.size} Selected",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { viewModel.selectAll(events) }) {
                            Icon(
                                imageVector = Icons.Rounded.SelectAll,
                                contentDescription = "Select All",
                                tint = ElectricBlue
                            )
                        }
                        if (uiState.selectedIds.isNotEmpty()) {
                            IconButton(onClick = { viewModel.showDeleteSelectedConfirmation(true) }) {
                                Icon(
                                    imageVector = Icons.Rounded.Delete,
                                    contentDescription = "Delete Selected",
                                    tint = StatusRed
                                )
                            }
                        }
                    }
                }
            } else {
                LockGuardTopBar(
                    title = "Intruder Evidence",
                    onBackClick = onNavigateBack,
                    actions = {
                        if (events.isNotEmpty()) {
                            IconButton(onClick = { viewModel.toggleSelectionMode() }) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = "Select",
                                    tint = Color.White
                                )
                            }
                            IconButton(onClick = { viewModel.showDeleteAllConfirmation(true) }) {
                                Icon(
                                    imageVector = Icons.Rounded.DeleteSweep,
                                    contentDescription = "Delete All",
                                    tint = StatusRed
                                )
                            }
                        }
                    }
                )
            }

            // Filter Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GalleryFilter.entries.forEach { filter ->
                    val isSelected = uiState.activeFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setFilter(filter) },
                        label = { Text(text = filter.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricBlue,
                            selectedLabelColor = Color.White,
                            containerColor = Navy800,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Photo & Event Grid
            if (events.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyStateView(
                        title = "No Evidence Found",
                        subtitle = "No intruder events match the selected filter. Any failed authentication detections will be encrypted and listed here."
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(events, key = { it.id }) { event ->
                        IntruderPhotoCard(
                            event = event,
                            onLoadBitmap = { path -> viewModel.loadPhotoBitmap(path) },
                            onClick = { onNavigateToDetail(event.id) },
                            isSelectionMode = uiState.isSelectionMode,
                            isSelected = uiState.selectedIds.contains(event.id),
                            onSelectToggle = { viewModel.toggleEventSelection(event.id) }
                        )
                    }
                }
            }
        }

        // Delete Selected Confirmation Dialog
        if (uiState.showDeleteSelectedDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.showDeleteSelectedConfirmation(false) },
                title = { Text(text = "Delete Evidence Records?") },
                text = {
                    Text(
                        text = "Are you sure you want to delete ${uiState.selectedIds.size} selected intruder record(s)? This will permanently erase the encrypted photos and logs from private storage."
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.deleteSelectedEvents() },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusRed)
                    ) {
                        Text(text = "Delete", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.showDeleteSelectedConfirmation(false) }) {
                        Text(text = "Cancel")
                    }
                }
            )
        }

        // Delete All Confirmation Dialog
        if (uiState.showDeleteAllDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.showDeleteAllConfirmation(false) },
                title = { Text(text = "Delete All Evidence?") },
                text = {
                    Text(
                        text = "This will permanently delete all captured intruder photos and authentication logs. This action cannot be undone."
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.deleteAllEvents() },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusRed)
                    ) {
                        Text(text = "Delete All", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.showDeleteAllConfirmation(false) }) {
                        Text(text = "Cancel")
                    }
                }
            )
        }
    }
}
