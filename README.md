# LockGuard 🛡️📷

**Intruder Alert & Privacy Protection App for Android**

LockGuard is a production-quality, privacy-first Android security application engineered to detect unauthorized access attempts while adhering to Android's official security architecture, camera policies, and privacy principles.

---

## 📑 Table of Contents
- [Core Concept](#core-concept)
- [Android Security & Policy Compliance](#android-security--policy-compliance)
  - [Device Administration API (`watch-login`)](#device-administration-api-watch-login)
  - [Background Camera Execution Restrictions (Android 10+)](#background-camera-execution-restrictions-android-10)
  - [Compliant Dual-Vector Protection Model](#compliant-dual-vector-protection-model)
  - [Privacy Guarantees](#privacy-guarantees)
- [Architecture & Tech Stack](#architecture--tech-stack)
  - [Package & Module Structure](#package--module-structure)
  - [Technology Overview](#technology-overview)
- [Key Features](#key-features)
- [Getting Started & Setup](#getting-started--setup)
  - [Prerequisites](#prerequisites)
  - [Opening in Android Studio](#opening-in-android-studio)
  - [Building from Command Line](#building-from-command-line)
- [Permissions Reference](#permissions-reference)
- [Testing & Verification Guide](#testing--verification-guide)
- [License](#license)

---

## Core Concept

When someone attempts to access an Android device or unlock the LockGuard vault with an incorrect credential, LockGuard logs the forensic event, captures a front-camera intruder photo where legitimately permitted by the operating system, and hardware-encrypts the image before writing it to isolated internal storage.

---

## Android Security & Policy Compliance

### Device Administration API (`watch-login`)
LockGuard integrates with Android's official **Device Administration API** using `DeviceAdminReceiver` declared with the `<watch-login />` policy in `res/xml/device_admin.xml`.

```xml
<device-admin xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-policies>
        <watch-login />
    </uses-policies>
</device-admin>
```

When activated by the device owner, the Android OS broadcasts `ACTION_PASSWORD_FAILED` directly to `LockGuardDeviceAdminReceiver` whenever a failed unlock attempt occurs on the device's system lock screen.

### Background Camera Execution Restrictions (Android 10+)
Starting in Android 9 (Pie) and reinforced in Android 10 (Q) through Android 15:
1. **Background camera access is strictly prohibited**: Android prevents apps in the background or broadcast receivers from opening the camera device (`CameraManager` / CameraX).
2. **Background activity launching restrictions**: Android 10+ restricts background broadcast receivers from launching activities directly without user interaction.
3. Attempting to bypass these rules using hidden APIs, accessibility abuse, or background overlays violates Google Play policies and compromises user trust.

### Compliant Dual-Vector Protection Model
To deliver uncompromising security without exploiting workarounds, LockGuard provides two complementary protection vectors:

| Feature | System Lock Screen (DeviceAdmin) | In-App Vault Lock (AppLock) |
| :--- | :--- | :--- |
| **Detection Trigger** | OS lock screen PIN/pattern failure | In-app vault PIN failure |
| **Android API** | `DeviceAdminReceiver.onPasswordFailed()` | Foreground Compose Keypad & PIN Manager |
| **Camera Capture** | Logs attempt + fires discreet alert notification | Immediate front-facing CameraX capture |
| **Background Policy** | Fully compliant with Android 10+ background limits | Runs in foreground lifecycle |
| **Photo Encryption** | N/A (Alert logged) | AES-256-GCM via Android Keystore |

Additionally, a **Diagnostic Test Simulation** feature is accessible directly from the Dashboard to verify front-camera capture and encryption without requiring users to lock their device.

### Privacy Guarantees
- **Zero Password Snooping**: LockGuard **never** reads, intercepts, or stores your device's actual PIN, password, or pattern. Android's API only transmits an anonymous event indicating that a failure occurred.
- **100% On-Device**: Photos and logs remain completely local. There are no analytics libraries, third-party trackers, or cloud upload servers.
- **Hardware-Backed Cryptography**: All intruder photos are encrypted using **AES-256-GCM** with master keys stored in the Android KeyStore (backed by Secure Element / StrongBox hardware where available).
- **Discreet Notifications**: Security alerts display a neutral, non-revealing notification (*"Security alert: An unauthorized authentication attempt was detected."*) and never show intruder photos on the lock screen.
- **Data Sovereignty**: Complete multi-select deletion, automatic retention policies (7, 14, 30 days), and full cryptographic wipe capabilities.

---

## Architecture & Tech Stack

LockGuard adheres to **Clean Architecture** principles and the standard **MVVM (Model-View-ViewModel)** pattern:

```
LockGuard/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/lockguard/app/
│   │   │   ├── LockGuardApp.kt              # Application class & Hilt entry point
│   │   │   ├── MainActivity.kt              # Single-activity Compose host & auto-lock
│   │   │   ├── data/
│   │   │   │   ├── camera/                  # CameraX front-capture manager
│   │   │   │   ├── database/                # Room DB entity, DAO, converters
│   │   │   │   ├── repository/              # Intruder & Settings repositories
│   │   │   │   └── security/                # Android Keystore CryptoManager, PinManager, Biometrics
│   │   │   ├── domain/model/                # IntruderEvent, SecurityStatus, Settings models
│   │   │   ├── di/                          # Hilt dependency injection modules
│   │   │   ├── receiver/                    # LockGuardDeviceAdminReceiver
│   │   │   ├── service/                     # NotificationHelper (discreet alert channels)
│   │   │   ├── workers/                     # PhotoRetentionWorker (auto-purge retention)
│   │   │   └── ui/
│   │   │       ├── components/              # ShieldLogo, PinKeypad, StatCard, StatusBadge, Cards
│   │   │       ├── navigation/              # Compose NavHost & Screen routes
│   │   │       ├── screens/
│   │   │       │   ├── splash/              # Animated splash & route triage
│   │   │       │   ├── onboarding/          # 4-step privacy-first onboarding
│   │   │       │   ├── setup/               # Permission & Device Admin configuration
│   │   │       │   ├── dashboard/           # Security health, stats, quick test, recent events
│   │   │       │   ├── gallery/             # Multi-select, filtering, bulk deletion
│   │   │       │   ├── detail/              # Full-screen pinch/zoom viewer, export, delete
│   │   │       │   ├── applock/             # In-app PIN entry, biometric trigger, intruder capture
│   │   │       │   ├── settings/            # Protection toggles, retention policies, data wipe
│   │   │       │   ├── privacy/             # Transparency & encryption guarantee report
│   │   │       │   └── about/               # Architecture & Android compliance details
│   │   │       └── theme/                   # Material 3 colors, shapes, typography, dark/light theme
│   │   └── res/                             # Vector drawables, adaptive icons, XML policies
│   └── build.gradle.kts                     # App module configuration
├── gradle/
│   ├── libs.versions.toml                   # Gradle version catalog
│   └── wrapper/                             # Gradle wrapper configuration
├── build.gradle.kts                         # Root project build file
└── settings.gradle.kts                      # Project settings
```

### Technology Overview
- **Kotlin** with Kotlin 2.0 compiler
- **Jetpack Compose** & **Material 3** for edge-to-edge UI
- **CameraX** (`camera-core`, `camera-camera2`, `camera-lifecycle`) for front camera capture
- **Room Database** with Kotlin Coroutines & Flow for reactive local storage
- **Android Keystore** (`AES/GCM/NoPadding`, 256-bit key) for hardware-level photo encryption
- **Jetpack Security** (`EncryptedSharedPreferences`) for salted PIN hash storage
- **BiometricPrompt** (`androidx.biometric:biometric`) for fingerprint & facial authentication
- **Hilt** (`com.google.dagger:hilt-android`) for clean dependency injection
- **Navigation Compose** for decoupled type-safe screen navigation
- **WorkManager** (`androidx.work:work-runtime-ktx`) for daily photo retention enforcement

---

## Key Features

1. **Security Dashboard**
   - Dynamic protection shield indicator (Maximum, Protected, Partial, Action Required)
   - Failed attempts count, photos captured count, last attempt timestamp
   - Recent intruder events carousel with quick access to full records
   - Instant "Test Intruder Capture & Encryption" button for safe diagnostics

2. **Intruder Photo Capture**
   - Front-facing camera capture via CameraX without requiring a visible preview viewfinder
   - Automatic sensor rotation correction for right-side-up evidence
   - Instant AES-256-GCM encryption before writing to internal disk

3. **Secure Gallery & Photo Viewer**
   - Responsive 2-column evidence grid
   - Filter by event type: All Events, Photos Only, System Lock Screen, In-App Vault
   - Full-screen interactive viewer with pinch-to-zoom and pan gestures
   - Multi-select mode with batch deletion
   - "Delete All Evidence" with confirmation
   - Export evidence via secure `FileProvider` with explicit user confirmation

4. **App Lock & Biometrics**
   - 4-digit in-app PIN entry with animated dot indicators
   - Integrated BiometricPrompt for seamless fingerprint/face unlock
   - Automatic inactivity auto-lock (Immediate, 30s, 1m, 5m)
   - **In-App Intruder Trapping**: Entering an incorrect PIN immediately triggers the front camera and records the intruder

5. **Discreet Notifications**
   - High-priority security alert channel
   - Privacy-safe message: *"Security alert: An unauthorized authentication attempt was detected."*
   - Never exposes intruder photographs on the lock screen

6. **Retention & Storage Policies**
   - Automatic photo deletion window: 7 days, 14 days, 30 days, or Keep Forever
   - Max stored photos quota: 25, 50, 100, or Unlimited
   - Handled reliably via background `WorkManager`

---

## Getting Started & Setup

### Prerequisites
- Android Studio Ladybug / Koala / Jellyfish (or higher)
- JDK 17 (bundled with modern Android Studio)
- Android SDK 34 or higher installed
- Physical device or Emulator running Android 8.0+ (API 26 to API 35)

### Opening in Android Studio
1. Open Android Studio.
2. Select **File > Open...** and navigate to the `LockGuard` directory inside this workspace.
3. Allow Gradle to synchronize dependencies.
4. Connect an Android device or launch an Android Virtual Device (AVD).
5. Click **Run > Run 'app'** (`Shift + F10`).

### Building from Command Line
```bash
cd LockGuard
./gradlew assembleDebug
```
The compiled APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

To run unit tests:
```bash
./gradlew test
```

---

## Permissions Reference

| Permission | Purpose | Where Used |
| :--- | :--- | :--- |
| `android.permission.CAMERA` | Front camera capture of unauthorized access attempts | `CameraCaptureManager` |
| `android.permission.BIND_DEVICE_ADMIN` | Receives failed lock-screen unlock signals (`watch-login`) | `LockGuardDeviceAdminReceiver` |
| `android.permission.POST_NOTIFICATIONS` | Delivers discreet security notifications on Android 13+ | `NotificationHelper` |
| `android.permission.USE_BIOMETRIC` | Biometric fingerprint/face unlock for vault | `BiometricHelper` |
| `android.permission.RECEIVE_BOOT_COMPLETED` | Reschedules periodic maintenance after device reboot | `PhotoRetentionWorker` |

---

## Testing & Verification Guide

### 1. Diagnostic Test Simulation (Fastest)
1. Launch LockGuard and complete onboarding.
2. From the **Security Dashboard**, tap **"Test Intruder Capture & Encryption"**.
3. Allow the camera permission prompt if prompted for the first time.
4. The front camera will capture a test photo, encrypt it with AES-256-GCM, and save it to the vault.
5. Tap **"Intruder Gallery"** or the thumbnail on the dashboard to inspect the encrypted photo and forensic metadata.

### 2. In-App Intruder Trapping Test
1. Go to **Settings > Vault App Lock & PIN** and configure a 4-digit PIN (e.g. `1234`).
2. Close the app or wait for the auto-lock timeout.
3. Open LockGuard. The PIN keypad will appear.
4. Enter an incorrect PIN (e.g. `9999`).
5. LockGuard will immediately trigger the front camera and record an in-app intruder attempt.
6. Now enter the correct PIN (`1234`) to unlock.
7. Open the **Gallery** to view the intruder evidence with timestamp and "In-App Vault Lock" tag.

### 3. System Lock-Screen Detection Test (Device Administrator)
1. Go to **Settings > Screen Unlock Monitor** and tap **"Enable"**.
2. Android's system Device Administrator prompt will appear detailing the `watch-login` policy. Tap **"Activate this device admin app"**.
3. Lock your Android device using the hardware power button.
4. Turn on the screen and enter an **incorrect** PIN, pattern, or password on your Android lock screen.
5. Unlock your device with your real PIN.
6. A discreet security alert notification will appear in the notification drawer: *"Security alert: An unauthorized authentication attempt was detected."*
7. Tap the notification to open LockGuard and view the logged attempt in the dashboard.

---

## Design System & Visual Identity
- **Logo**: Minimalist geometric shield housing a glowing electric camera-eye lens.
- **Color Palette**:
  - Background: Deep Navy / Obsidian (`#070B14`, `#0B1120`)
  - Primary / Accent: Electric Blue (`#0066FF`), Electric Cyan (`#00F0FF`)
  - Status Indicators: Emerald Green (`#10B981`), Amber (`#F59E0B`), Coral Red (`#EF4444`)
- **UI Architecture**: Material 3 edge-to-edge layout, rounded cards, animated PIN keypad, and dark/light mode support.

---

## Compliance & Architecture Summary
LockGuard was built in strict adherence to:
- Official Google Play Device Administrator Policies
- Android OS Background Execution & Camera Boundaries
- OWASP Mobile Security Testing Guide (MSTG) data storage guidelines
- Hardware-backed KeyStore cryptographic standards
