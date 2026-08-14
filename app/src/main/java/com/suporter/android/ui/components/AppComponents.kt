package com.suporter.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.suporter.android.core.database.WebhookLogEntity
import com.suporter.android.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    borderColor: Color = BgCardBorder,
    backgroundColor: Color = BgCard,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(16.dp),
        content = content
    )
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    color: Color,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(BgCard)
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
        )
    }
}

@Composable
fun WarningCard(
    title: String,
    description: String,
    buttonText: String? = null,
    onButtonClick: (() -> Unit)? = null,
    isWarning: Boolean = true
) {
    val borderColor = if (isWarning) WarningAmber.copy(alpha = 0.4f) else ErrorRed.copy(alpha = 0.4f)
    val bgColor = if (isWarning) WarningAmber.copy(alpha = 0.08f) else ErrorRed.copy(alpha = 0.08f)
    val titleColor = if (isWarning) WarningAmber else ErrorRed

    AppCard(
        borderColor = borderColor,
        backgroundColor = bgColor
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Text(
                text = if (isWarning) "⚠️" else "🛑",
                fontSize = 20.sp,
                modifier = Modifier.padding(end = 12.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = titleColor
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                )
                if (buttonText != null && onButtonClick != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onButtonClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isWarning) WarningAmber else ErrorRed,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = buttonText,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (bgColor, textColor) = when (status) {
        "SUCCESS" -> SuccessGreen.copy(alpha = 0.15f) to SuccessGreen
        "FAILED" -> ErrorRed.copy(alpha = 0.15f) to ErrorRed
        else -> WarningAmber.copy(alpha = 0.15f) to WarningAmber
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = status,
            style = MaterialTheme.typography.labelLarge.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        )
    }
}

@Composable
fun LogDetailDialog(
    log: WebhookLogEntity,
    onDismiss: () -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val dateStr = dateFormat.format(Date(log.timestamp))

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, BgCardBorder, RoundedCornerShape(16.dp)),
            color = BgCard
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Detail Log Webhook",
                        style = MaterialTheme.typography.titleLarge.copy(color = TextPrimary)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusBadge(status = log.status)
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = BgCardBorder)
                Spacer(modifier = Modifier.height(14.dp))

                DetailItem(label = "Aplikasi Sumber", value = "${log.sourceAppName} (${log.sourcePackage})")
                DetailItem(label = "Judul Notifikasi", value = log.notificationTitle.ifBlank { "-" })
                DetailItem(label = "Teks Notifikasi", value = log.notificationText.ifBlank { "-" })
                DetailItem(label = "Nominal Diekstrak", value = "Rp ${log.extractedAmount}")
                DetailItem(label = "URL Webhook", value = log.requestUrl, isMono = true)
                DetailItem(label = "HTTP Status", value = "${log.responseCode}")
                DetailItem(label = "Response Body", value = log.responseBody, isMono = true)

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Tutup", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String, isMono: Boolean = false) {
    Column(modifier = Modifier.padding(bottom = 10.dp)) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted
            )
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 13.sp,
                fontFamily = if (isMono) FontFamily.Monospace else FontFamily.Default,
                color = TextPrimary
            )
        )
    }
}
