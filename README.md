# Suporter Android — Webhook Notification Listener 📱⚡

A native Android application built with **Kotlin**, **Jetpack Compose**, **Clean Architecture**, and `NotificationListenerService` that intercepts payment and banking notifications (ShopeePay, DANA, GoPay, OVO, BCA Mobile, Livin Mandiri, BRImo, etc.), extracts transaction amounts using smart regex parsers, computes HMAC-SHA256 signatures, and automatically dispatches donation webhooks to the Suporter backend to trigger real-time streamer OBS alerts.

---

## ✨ Features

1. **Streamer Mobile Authentication**:
   - Direct login with streamer credentials (`POST /api/v1/auth/mobile-login`).
   - Retrieves and securely stores JWT token, `webhook_key`, and `webhook_secret`.
   - Supports custom backend server URL (e.g. `http://10.0.2.2:8080` for Android Emulator, `http://192.168.x.x:8080` for physical devices, or `https://api.yourdomain.com`).

2. **2-in-1 Webhook Playground & OBS Simulator**:
   - Allows streamers to simulate live donations with custom **Sender Name**, **Nominal (Rupiah)**, and **Message**.
   - Generates a pending donation record with `is_test=true` via header `X-Is-Test: true`.
   - Computes HMAC-SHA256 signature with timestamp and immediately verifies the webhook, triggering the live OBS stream alert.

3. **Smart Amount Extractor (Regex Engine)**:
   - Regex patterns tailored for Indonesian banking and e-wallet push notifications:
     - `Rp 50.000`, `Rp50000`, `IDR 25.000`
     - `sebesar Rp 100.000`
     - `+ Rp 25.000`
     - `Transfer masuk 50.000 berhasil`

4. **14 Default Pre-Seeded Keywords (Room Database)**:
   - `berhasil diterima`, `cr`, `credit`, `dana masuk`, `diterima`, `incoming`, `masuk`, `payment received`, `pembayaran diterima`, `pembayaran masuk`, `received`, `saldo bertambah`, `terima`, `transfer masuk`.
   - Ability to add, toggle, and delete custom keywords.

5. **Aplikasi Dipantau (App Selector)**:
   - Scans installed apps and lets streamers toggle which e-wallets/banking apps should be monitored.

6. **24/7 Lock-Screen Background Listening**:
   - `NotificationListenerService` system-level binding.
   - Persistent `KeepAliveForegroundService` with WakeLock.
   - Boot receiver (`BOOT_COMPLETED`) to auto-start listener upon device reboot.
   - Battery optimization status detection & exemption request button.

7. **Detailed Webhook Logs**:
   - Complete log tracking in Room DB (Status `SUCCESS`, `FAILED`, `IGNORED`).
   - Log Detail Modal showing: Source App, Raw Notification Title & Text, Extracted Amount, Target URL, Request Headers, JSON Payload, HTTP Code, and Response Body.

---

## 🛠️ How to Build & Run

### Prerequisites
- **Android Studio**: Hedgehog (2023.1.1) or newer.
- **Android SDK**: API Level 34 (compileSdk 34, minSdk 26).
- **JDK**: Java 17.

### Opening in Android Studio
1. Open Android Studio.
2. Select **File > Open** and choose the `suporter-android` folder.
3. Allow Gradle to sync dependencies.
4. Run on an Android Emulator or connected physical device (Android 8.0+).

---

## 🔐 Webhook Authentication Headers

When sending payment notifications, the app sends:
```http
POST /api/v1/webhooks/donation
Host: api.yourdomain.com
Content-Type: application/json
X-Suporter-Key: wk_a1b2c3d4...
X-Suporter-Timestamp: 1723642800
X-Suporter-Signature: 5d41402abc4b2a76b9719d911017c592...

{"amount": 50142}
```
Where `X-Suporter-Signature = HMAC-SHA256(webhook_secret, timestamp + "." + raw_body)`.
