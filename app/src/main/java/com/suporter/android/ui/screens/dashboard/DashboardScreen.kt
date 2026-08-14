package com.suporter.android.ui.screens.dashboard

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.suporter.android.core.preferences.UserPreferences
import com.suporter.android.data.repository.LogRepository
import com.suporter.android.service.KeepAliveForegroundService
import com.suporter.android.ui.components.AppCard
import com.suporter.android.ui.components.MetricCard
import com.suporter.android.ui.components.WarningCard
import com.suporter.android.ui.theme.*

@Composable
fun DashboardScreen(
    preferences: UserPreferences,
    logRepository: LogRepository,
    onNavigateToPlayground: () -> Unit,
    onNavigateToApps: () -> Unit,
    onNavigateToKeywords: () -> Unit,
    onNavigateToLogs: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val totalCount by logRepository.totalCount.collectAsState(initial = 0)
    val successCount by logRepository.successCount.collectAsState(initial = 0)
    val failedCount by logRepository.failedCount.collectAsState(initial = 0)

    var isNotificationAccessGranted by remember { mutableStateOf(checkNotificationAccess(context)) }
    var isBatteryOptimizationIgnored by remember { mutableStateOf(checkBatteryOptimization(context)) }
    var isKeepAliveRunning by remember { mutableStateOf(preferences.isKeepAliveEnabled()) }
    var isWebhookForwardingEnabled by remember { mutableStateOf(preferences.isWebhookForwardingEnabled()) }

    // Observe app lifecycle ON_RESUME so permissions and battery optimizations refresh automatically upon returning from system Settings
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isNotificationAccessGranted = checkNotificationAccess(context)
                isBatteryOptimizationIgnored = checkBatteryOptimization(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Streamer Profile Header Card
        AppCard(borderColor = BgCardBorder) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = preferences.getName() ?: "Streamer",
                        style = MaterialTheme.typography.titleLarge.copy(color = TextPrimary)
                    )
                    Text(
                        text = "@${preferences.getUsername() ?: ""}",
                        style = MaterialTheme.typography.bodyMedium.copy(color = PrimaryEmerald)
                    )
                }

                IconButton(
                    onClick = onLogout,
                    colors = IconButtonDefaults.iconButtonColors(contentColor = ErrorRed)
                ) {
                    Icon(Icons.Default.PowerSettingsNew, contentDescription = "Logout")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = BgCardBorder)
            Spacer(modifier = Modifier.height(12.dp))

            // Webhook Key & Secret Preview
            Text(
                text = "WEBHOOK KEY",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = preferences.getWebhookKey() ?: "-",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = WarningAmber
                )
            )
        }

        // 1. Notification Listener Permission Status Card
        if (isNotificationAccessGranted) {
            AppCard(
                borderColor = SuccessGreen.copy(alpha = 0.4f),
                backgroundColor = SuccessGreen.copy(alpha = 0.08f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Akses Notifikasi Aktif",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Aplikasi memiliki izin penuh untuk mendengarkan notifikasi pembayaran secara real-time.",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp, color = TextSecondary)
                        )
                    }
                }
            }
        } else {
            WarningCard(
                title = "Izin Akses Notifikasi Diperlukan",
                description = "Suporter membutuhkan izin 'Akses Notifikasi' untuk mendengarkan notifikasi pembayaran yang masuk dari aplikasi e-wallet dan perbankan Anda.",
                buttonText = "Buka Pengaturan Izin",
                onButtonClick = {
                    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                    context.startActivity(intent)
                },
                isWarning = false
            )
        }

        // 2. Battery Optimization Status Card
        if (isBatteryOptimizationIgnored) {
            AppCard(
                borderColor = SuccessGreen.copy(alpha = 0.4f),
                backgroundColor = SuccessGreen.copy(alpha = 0.08f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Optimasi Baterai Dinonaktifkan",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Aplikasi berjalan optimal di latar belakang tanpa dibatasi oleh sistem penghemat daya (Doze Mode).",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp, color = TextSecondary)
                        )
                    }
                }
            }
        } else {
            WarningCard(
                title = "Optimasi Baterai Aktif (Wajib Dimatikan)",
                description = "Android secara otomatis mematikan proses di latar belakang saat layar terkunci untuk menghemat baterai (Doze Mode). Nonaktifkan optimasi baterai agar notifikasi pembayaran selalu tertangkap 24/7.",
                buttonText = "Matikan Optimasi Baterai",
                onButtonClick = {
                    requestIgnoreBatteryOptimization(context)
                },
                isWarning = true
            )
        }

        // Statistics Metrics Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(
                title = "Total",
                value = "$totalCount",
                color = AccentIndigo,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Berhasil",
                value = "$successCount",
                color = SuccessGreen,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Gagal",
                value = "$failedCount",
                color = ErrorRed,
                modifier = Modifier.weight(1f)
            )
        }

        // 1. Webhook Forwarding Service Control Card
        AppCard(borderColor = if (isWebhookForwardingEnabled) PrimaryEmerald.copy(alpha = 0.35f) else BgCardBorder) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Pengiriman Webhook Otomatis",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isWebhookForwardingEnabled) TextPrimary else TextMuted
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isWebhookForwardingEnabled) SuccessGreen.copy(alpha = 0.15f) else ErrorRed.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isWebhookForwardingEnabled) "AKTIF" else "JEDA",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isWebhookForwardingEnabled) SuccessGreen else ErrorRed
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = if (isWebhookForwardingEnabled)
                            "Notifikasi pembayaran yang terdeteksi akan otomatis dikirim ke webhook server & memicu alert OBS."
                        else
                            "Pengiriman webhook dijeda. Notifikasi tetap dicatat di log lokal namun TIDAK dikirim ke server.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 12.sp,
                            color = if (isWebhookForwardingEnabled) TextSecondary else TextMuted
                        )
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Switch(
                    checked = isWebhookForwardingEnabled,
                    onCheckedChange = { checked ->
                        isWebhookForwardingEnabled = checked
                        preferences.setWebhookForwardingEnabled(checked)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = PrimaryEmerald
                    )
                )
            }
        }

        // 2. Keep-Alive Service Control Card
        AppCard(borderColor = if (isKeepAliveRunning) PrimaryEmerald.copy(alpha = 0.3f) else BgCardBorder) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Layanan Keep-Alive Latar Belakang",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = if (isKeepAliveRunning)
                            "Aktif — Menampilkan notifikasi persisten di status bar agar Android tidak mematikan service saat layar HP terkunci."
                        else
                            "Nonaktif — Notifikasi mungkin terlewat saat HP masuk mode tidur (Doze Mode).",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 12.sp,
                            color = if (isKeepAliveRunning) SuccessGreen else TextMuted
                        )
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Switch(
                    checked = isKeepAliveRunning,
                    onCheckedChange = { checked ->
                        isKeepAliveRunning = checked
                        preferences.setKeepAliveEnabled(checked)
                        if (checked) {
                            KeepAliveForegroundService.start(context)
                        } else {
                            KeepAliveForegroundService.stop(context)
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = PrimaryEmerald
                    )
                )
            }
        }

        // Navigation Quick Action Buttons
        Text(
            text = "MENU UTAMA",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted
            ),
            modifier = Modifier.padding(top = 4.dp)
        )

        NavigationButton(
            title = "Playground & Test Webhook",
            subtitle = "Simulasikan donasi dan uji respon webhook secara langsung ke OBS",
            icon = Icons.Default.PlayArrow,
            color = PrimaryEmerald,
            onClick = onNavigateToPlayground
        )

        NavigationButton(
            title = "Aplikasi Dipantau",
            subtitle = "Pilih aplikasi e-wallet / bank (ShopeePay, DANA, GoPay, BCA, dll)",
            icon = Icons.Default.Apps,
            color = AccentIndigo,
            onClick = onNavigateToApps
        )

        NavigationButton(
            title = "Kata Kunci Notifikasi",
            subtitle = "Kelola 14 kata kunci bawaan dan kata kunci kustom",
            icon = Icons.Default.Key,
            color = WarningAmber,
            onClick = onNavigateToKeywords
        )

        NavigationButton(
            title = "Riwayat & Detail Log",
            subtitle = "Lihat payload request, response body, dan status setiap notifikasi",
            icon = Icons.AutoMirrored.Filled.List,
            color = AccentPurple,
            onClick = onNavigateToLogs
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun NavigationButton(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, BgCardBorder, RoundedCornerShape(12.dp)),
        color = BgCard
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp, color = TextMuted)
                )
            }
            Text(text = "→", fontSize = 18.sp, color = TextMuted)
        }
    }
}

private fun checkNotificationAccess(context: Context): Boolean {
    val enabledListeners = NotificationManagerCompat.getEnabledListenerPackages(context)
    return enabledListeners.contains(context.packageName)
}

private fun checkBatteryOptimization(context: Context): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }
    return true
}

private fun requestIgnoreBatteryOptimization(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            val fallbackIntent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            context.startActivity(fallbackIntent)
        }
    }
}
