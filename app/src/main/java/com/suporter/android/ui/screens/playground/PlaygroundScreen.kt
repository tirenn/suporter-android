package com.suporter.android.ui.screens.playground

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.suporter.android.core.database.AppDatabase
import com.suporter.android.core.database.WebhookLogEntity
import com.suporter.android.core.network.ApiClient
import com.suporter.android.core.network.ErrorParser
import com.suporter.android.core.network.HmacHelper
import com.suporter.android.core.preferences.UserPreferences
import com.suporter.android.data.model.CreateDonationRequest
import com.suporter.android.data.model.WebhookDonationRequest
import com.suporter.android.ui.components.AppCard
import com.suporter.android.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaygroundScreen(
    preferences: UserPreferences,
    database: AppDatabase,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val gson = remember { Gson() }

    var senderName by remember { mutableStateOf("Tester Android") }
    var amountText by remember { mutableStateOf("50000") }
    var message by remember { mutableStateOf("Uji coba alert donasi langsung ke OBS!") }

    var isRunning by remember { mutableStateOf(false) }
    var countdownSeconds by remember { mutableIntStateOf(0) }
    var testResult by remember { mutableStateOf<TestExecutionResult?>(null) }

    // Countdown Timer Ticker
    LaunchedEffect(countdownSeconds) {
        if (countdownSeconds > 0) {
            delay(1000L)
            countdownSeconds -= 1
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Playground & Test Webhook", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = TextPrimary)
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Explanation Card
            AppCard(borderColor = PrimaryEmerald.copy(alpha = 0.3f), backgroundColor = PrimaryEmerald.copy(alpha = 0.05f)) {
                Text(
                    text = "🧪 Pengujian Webhook Riil",
                    style = MaterialTheme.typography.titleMedium.copy(color = PrimaryEmerald, fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Proses ini membuat data donasi dengan status pending (berflag is_test=true) dan langsung mengeksekusi webhook dengan tanda tangan HMAC-SHA256. Notifikasi alert akan langsung muncul di overlay OBS Anda!",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary, fontSize = 12.sp)
                )
            }

            // Rate Limit Active Banner
            if (countdownSeconds > 0) {
                AppCard(
                    borderColor = WarningAmber.copy(alpha = 0.4f),
                    backgroundColor = WarningAmber.copy(alpha = 0.1f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.HourglassTop, contentDescription = null, tint = WarningAmber)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Rate Limit Aktif",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = WarningAmber)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Silakan tunggu $countdownSeconds detik sebelum mencoba pengujian lagi.",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp, color = TextPrimary)
                            )
                        }
                    }
                }
            }

            // Input Form Card
            AppCard {
                Text(
                    text = "NAMA PENGIRIM",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = senderName,
                    onValueChange = { senderName = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryEmerald,
                        unfocusedBorderColor = BgCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "NOMINAL DONASI (RUPIAH)",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryEmerald,
                        unfocusedBorderColor = BgCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "PESAN DONASI",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryEmerald,
                        unfocusedBorderColor = BgCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                val isButtonEnabled = !isRunning && countdownSeconds == 0

                Button(
                    onClick = {
                        val amount = amountText.toLongOrNull() ?: 50000L
                        val username = preferences.getUsername() ?: ""
                        val webhookKey = preferences.getWebhookKey() ?: ""
                        val webhookSecret = preferences.getWebhookSecret() ?: ""
                        val serverUrl = preferences.getServerUrl()

                        if (username.isBlank() || webhookKey.isBlank()) {
                            testResult = TestExecutionResult(
                                isSuccess = false,
                                stepName = "Validasi Sesi",
                                message = "Akun belum login atau Webhook Key tidak ditemukan"
                            )
                            return@Button
                        }

                        isRunning = true
                        testResult = null

                        scope.launch {
                            try {
                                val api = ApiClient.getService(serverUrl)

                                // Step 1: Create pending test donation
                                val donReq = CreateDonationRequest(
                                    streamerUsername = username,
                                    senderName = senderName,
                                    amount = amount,
                                    message = message
                                )
                                val createResp = api.createDonation(isTest = "true", request = donReq)

                                if (!createResp.isSuccessful || createResp.body() == null) {
                                    val errRaw = createResp.errorBody()?.string()
                                    val parsedMessage = ErrorParser.parse(errRaw, "HTTP ${createResp.code()}")

                                    if (createResp.code() == 429) {
                                        val retryAfter = ErrorParser.parseRetryAfter(errRaw) ?: 60
                                        countdownSeconds = retryAfter
                                    }

                                    testResult = TestExecutionResult(
                                        isSuccess = false,
                                        stepName = "Langkah 1: Buat Donasi Pending",
                                        message = parsedMessage
                                    )
                                    isRunning = false
                                    return@launch
                                }

                                val createdDonation = createResp.body()!!
                                val totalAmountToVerify = createdDonation.totalAmount

                                // Step 2: Hit Real Webhook with HMAC
                                val webhookReqObj = WebhookDonationRequest(amount = totalAmountToVerify)
                                val rawJson = gson.toJson(webhookReqObj)
                                val timestamp = System.currentTimeMillis() / 1000
                                val signature = HmacHelper.generateSignature(webhookSecret, timestamp, rawJson)

                                val requestBody = rawJson.toRequestBody("application/json".toMediaType())
                                val webhookResp = api.verifyWebhookRaw(
                                    webhookKey = webhookKey,
                                    timestamp = timestamp.toString(),
                                    signature = signature,
                                    rawBody = requestBody
                                )

                                val respCode = webhookResp.code()
                                val respBody = webhookResp.body()?.string() ?: webhookResp.errorBody()?.string() ?: ""

                                // Log to database
                                database.webhookLogDao().insertLog(
                                    WebhookLogEntity(
                                        sourcePackage = "com.suporter.android.playground",
                                        sourceAppName = "Playground Tester",
                                        notificationTitle = "Simulasi Donasi dari $senderName",
                                        notificationText = "Nominal: Rp $totalAmountToVerify, Pesan: $message",
                                        extractedAmount = totalAmountToVerify,
                                        requestUrl = "${serverUrl.trimEnd('/')}/api/v1/webhooks/donation",
                                        requestHeaders = "X-Suporter-Key: $webhookKey\nX-Suporter-Timestamp: $timestamp\nX-Suporter-Signature: $signature",
                                        requestPayload = rawJson,
                                        responseCode = respCode,
                                        responseBody = respBody,
                                        status = if (webhookResp.isSuccessful) "SUCCESS" else "FAILED"
                                    )
                                )

                                if (webhookResp.isSuccessful) {
                                    testResult = TestExecutionResult(
                                        isSuccess = true,
                                        stepName = "Semua Langkah Berhasil!",
                                        message = "Donasi Rp $totalAmountToVerify (Kode Unik: ${createdDonation.uniqueCode}) berhasil diverifikasi! Alert telah dikirim ke OBS.",
                                        rawResponse = respBody
                                    )
                                } else {
                                    val parsedWebhookErr = ErrorParser.parse(respBody, "Webhook gagal (${respCode})")
                                    if (respCode == 429) {
                                        val retryAfter = ErrorParser.parseRetryAfter(respBody) ?: 60
                                        countdownSeconds = retryAfter
                                    }
                                    testResult = TestExecutionResult(
                                        isSuccess = false,
                                        stepName = "Langkah 2: Eksekusi Webhook",
                                        message = parsedWebhookErr,
                                        rawResponse = respBody
                                    )
                                }

                            } catch (e: Exception) {
                                testResult = TestExecutionResult(
                                    isSuccess = false,
                                    stepName = "Koneksi Jaringan",
                                    message = e.localizedMessage ?: "Gagal terhubung ke server backend"
                                )
                            } finally {
                                isRunning = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (countdownSeconds > 0) BgCardBorder else PrimaryEmerald
                    ),
                    shape = RoundedCornerShape(10.dp),
                    enabled = isButtonEnabled
                ) {
                    if (isRunning) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
                    } else if (countdownSeconds > 0) {
                        Icon(Icons.Default.HourglassTop, contentDescription = null, tint = WarningAmber)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tunggu (${countdownSeconds}s)", fontWeight = FontWeight.Bold, color = WarningAmber)
                    } else {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Jalankan Test Webhook", fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
            }

            // Results Card
            if (testResult != null) {
                val res = testResult!!
                AppCard(
                    borderColor = if (res.isSuccess) SuccessGreen.copy(alpha = 0.4f) else ErrorRed.copy(alpha = 0.4f),
                    backgroundColor = if (res.isSuccess) SuccessGreen.copy(alpha = 0.08f) else ErrorRed.copy(alpha = 0.08f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (res.isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = null,
                            tint = if (res.isSuccess) SuccessGreen else ErrorRed
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = res.stepName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (res.isSuccess) SuccessGreen else ErrorRed
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = res.message,
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
                    )

                    if (!res.rawResponse.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "DETAIL RESPONSE:",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = res.rawResponse,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        )
                    }
                }
            }
        }
    }
}

data class TestExecutionResult(
    val isSuccess: Boolean,
    val stepName: String,
    val message: String,
    val rawResponse: String? = null
)
