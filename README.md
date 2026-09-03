# Suporter Android — Webhook Notification Listener 📱⚡

A native Android application built with **Kotlin**, **Jetpack Compose**, **Clean Architecture**, and `NotificationListenerService` that intercepts payment and banking notifications (ShopeePay, DANA, GoPay, OVO, BCA Mobile, Livin Mandiri, BRImo, etc.), extracts transaction amounts using smart regex parsers, computes HMAC-SHA256 signatures, and automatically dispatches donation webhooks to the Suporter backend to trigger real-time streamer OBS alerts.

---

## 🤖 GitHub Actions CI / Automated APK Builds

We provide automated GitHub Actions workflows for Android:

### 1. `Android CI / PR Checks` (`.github/workflows/ci.yml`)
- **Trigger**: Pull Requests and direct merges into `main`/`master`.
- **Actions**: Sets up JDK 17, executes unit tests (`testDebugUnitTest`), and verifies the debug build (`assembleDebug`).

### 2. `Build and Release Android APK on Tag` (`.github/workflows/release.yml`)
- **Trigger**: Pushing any tag (e.g. `v1.0.0`).
- **Actions**:
  1. Runs unit test suite.
  2. Compiles production release and debug APKs (`assembleRelease`, `assembleDebug`).
  3. Uploads the built `.apk` files as a GitHub Actions workflow artifact.
  4. Automatically publishes a **GitHub Release** with the `.apk` files attached for one-click downloading!

---

## 📥 How to Download the Built APK

When you push a tag (e.g., `git tag v1.0.0 && git push origin v1.0.0`):

1. **Option A: GitHub Releases (Easiest)**
   - Go to your repository on GitHub ➔ Click **Releases** (on the right sidebar).
   - Click on your release tag (e.g. `v1.0.0`).
   - Under **Assets**, click on the `.apk` file (e.g. `app-release.apk` or `app-debug.apk`) to download and install on your phone.

2. **Option B: GitHub Actions Artifacts**
   - Go to **Actions** ➔ Click on the latest **Build and Release Android APK on Tag** workflow run.
   - Scroll down to the **Artifacts** section at the bottom.
   - Click **`suporter-android-apks-v1.0.0`** to download the ZIP containing the `.apk` files.

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

## 🛠️ How to Build & Run Locally

### Prerequisites
- **Android Studio**: Hedgehog (2023.1.1) or newer.
- **Android SDK**: API Level 34 (compileSdk 34, minSdk 26).
- **JDK**: Java 17.

### Opening in Android Studio
1. Open Android Studio.
2. Select **File > Open** and choose the `android` folder.
3. Allow Gradle to sync dependencies.
4. Run on an Android Emulator or connected physical device (Android 8.0+).
