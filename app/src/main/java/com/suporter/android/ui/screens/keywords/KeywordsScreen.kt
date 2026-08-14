package com.suporter.android.ui.screens.keywords

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.suporter.android.core.database.KeywordEntity
import com.suporter.android.data.repository.KeywordRepository
import com.suporter.android.ui.components.AppCard
import com.suporter.android.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeywordsScreen(
    keywordRepository: KeywordRepository,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val keywords by keywordRepository.allKeywords.collectAsState(initial = emptyList())

    var isAddDialogOpen by remember { mutableStateOf(false) }
    var newKeywordText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kata Kunci Notifikasi", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDark, titleContentColor = TextPrimary)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isAddDialogOpen = true },
                containerColor = PrimaryEmerald,
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Kata Kunci")
            }
        },
        containerColor = BgDark
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
        ) {
            item {
                AppCard(
                    borderColor = AccentIndigo.copy(alpha = 0.3f),
                    backgroundColor = AccentIndigo.copy(alpha = 0.05f)
                ) {
                    Text(
                        text = "ℹ️ Sistem Pencocokan Notifikasi",
                        style = MaterialTheme.typography.titleMedium.copy(color = AccentIndigo, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Jika judul atau isi notifikasi dari aplikasi yang dipantau mengandung salah satu kata kunci aktif di bawah ini, aplikasi akan mengekstrak nominal dan mengirim webhook.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary, fontSize = 12.sp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(keywords, key = { it.id }) { kw ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, if (kw.isEnabled) PrimaryEmerald.copy(alpha = 0.3f) else BgCardBorder, RoundedCornerShape(12.dp)),
                    color = BgCard
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Text(
                                text = kw.keyword,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (kw.isEnabled) TextPrimary else TextMuted
                                )
                            )
                            if (kw.isDefault) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(AccentIndigo.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Bawaan",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AccentIndigo
                                        )
                                    )
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (!kw.isDefault) {
                                IconButton(
                                    onClick = {
                                        scope.launch { keywordRepository.deleteKeyword(kw) }
                                    },
                                    colors = IconButtonDefaults.iconButtonColors(contentColor = ErrorRed)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Hapus")
                                }
                            }

                            Switch(
                                checked = kw.isEnabled,
                                onCheckedChange = {
                                    scope.launch { keywordRepository.toggleKeyword(kw) }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = PrimaryEmerald
                                )
                            )
                        }
                    }
                }
            }
        }

        // Add Keyword Dialog
        if (isAddDialogOpen) {
            AlertDialog(
                onDismissRequest = { isAddDialogOpen = false },
                title = { Text("Tambah Kata Kunci Baru", color = TextPrimary) },
                text = {
                    Column {
                        Text(
                            text = "Masukkan teks / frase kata kunci yang muncul pada notifikasi transfer:",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = newKeywordText,
                            onValueChange = { newKeywordText = it },
                            placeholder = { Text("contoh: topup diterima", color = TextMuted) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryEmerald,
                                unfocusedBorderColor = BgCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newKeywordText.isNotBlank()) {
                                scope.launch {
                                    keywordRepository.addKeyword(newKeywordText)
                                    newKeywordText = ""
                                    isAddDialogOpen = false
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald)
                    ) {
                        Text("Simpan", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { isAddDialogOpen = false }) {
                        Text("Batal", color = TextMuted)
                    }
                },
                containerColor = BgCard
            )
        }
    }
}
