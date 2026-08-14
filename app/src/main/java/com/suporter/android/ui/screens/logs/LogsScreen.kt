package com.suporter.android.ui.screens.logs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.suporter.android.core.database.WebhookLogEntity
import com.suporter.android.data.repository.LogRepository
import com.suporter.android.ui.components.LogDetailDialog
import com.suporter.android.ui.components.StatusBadge
import com.suporter.android.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsScreen(
    logRepository: LogRepository,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var selectedFilter by remember { mutableStateOf("ALL") }

    val logs by logRepository.getLogsByStatus(selectedFilter).collectAsState(initial = emptyList())
    var selectedLogForDetail by remember { mutableStateOf<WebhookLogEntity?>(null) }
    var isClearConfirmOpen by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Riwayat & Detail Log", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = TextPrimary)
                    }
                },
                actions = {
                    if (logs.isNotEmpty()) {
                        IconButton(onClick = { isClearConfirmOpen = true }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Bersihkan Log", tint = ErrorRed)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDark, titleContentColor = TextPrimary)
            )
        },
        containerColor = BgDark
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == "ALL",
                    onClick = { selectedFilter = "ALL" },
                    label = { Text("Semua") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryEmerald.copy(alpha = 0.2f),
                        selectedLabelColor = PrimaryEmerald
                    )
                )
                FilterChip(
                    selected = selectedFilter == "SUCCESS",
                    onClick = { selectedFilter = "SUCCESS" },
                    label = { Text("Berhasil") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SuccessGreen.copy(alpha = 0.2f),
                        selectedLabelColor = SuccessGreen
                    )
                )
                FilterChip(
                    selected = selectedFilter == "FAILED",
                    onClick = { selectedFilter = "FAILED" },
                    label = { Text("Gagal") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ErrorRed.copy(alpha = 0.2f),
                        selectedLabelColor = ErrorRed
                    )
                )
            }

            if (logs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada riwayat log notifikasi",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp)
                ) {
                    items(logs, key = { it.id }) { log ->
                        Surface(
                            onClick = { selectedLogForDetail = log },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, BgCardBorder, RoundedCornerShape(12.dp)),
                            color = BgCard
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(14.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = log.sourceAppName,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                        )
                                        Text(
                                            text = dateFormat.format(Date(log.timestamp)),
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontSize = 11.sp,
                                                color = TextMuted
                                            )
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = if (log.extractedAmount > 0) "Nominal: Rp ${log.extractedAmount}" else log.notificationTitle.ifBlank { "Tidak ada judul" },
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 12.sp,
                                            color = if (log.extractedAmount > 0) PrimaryEmerald else TextSecondary
                                        )
                                    )
                                }

                                StatusBadge(status = log.status)
                            }
                        }
                    }
                }
            }
        }

        // Log Detail Modal
        if (selectedLogForDetail != null) {
            LogDetailDialog(
                log = selectedLogForDetail!!,
                onDismiss = { selectedLogForDetail = null }
            )
        }

        // Clear Confirmation Dialog
        if (isClearConfirmOpen) {
            AlertDialog(
                onDismissRequest = { isClearConfirmOpen = false },
                title = { Text("Hapus Semua Log?", color = TextPrimary) },
                text = {
                    Text("Semua data riwayat log notifikasi dan pengujian webhook akan dihapus secara permanen.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                logRepository.clearLogs()
                                isClearConfirmOpen = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                    ) {
                        Text("Hapus", color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { isClearConfirmOpen = false }) {
                        Text("Batal", color = TextMuted)
                    }
                },
                containerColor = BgCard
            )
        }
    }
}
