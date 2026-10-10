# Development Setup

## Tools
Android Studio (SDK/Gradle), Antigravity or another AI IDE, Android SDK Platform-Tools/ADB, Git, Python 3.x for evaluation, and Qualcomm-provided SDK docs/sample app. Use Gradle/JDK/SDK versions compatible with the verified vendor sample.

## Install ADB
Windows: Android Studio → SDK Manager → SDK Tools → Android SDK Platform-Tools. Alternative where supported: `winget install Google.PlatformTools`.

macOS: `brew install android-platform-tools`.

Run on the laptop: `adb version`.

## Connect board
On Android: Settings → About → tap Build number seven times → Developer options → USB debugging on → accept authorization.

On the laptop:

```bash
adb devices
adb shell getprop ro.product.model
adb shell getprop ro.build.version.release
adb shell getprop ro.build.version.sdk
```

The device should show `device`. If `unauthorized`, accept the prompt. If absent, check cable, USB mode, driver and debugging.

## Build and deploy
From repository root:

```bash
./gradlew assembleDebug
./gradlew installDebug
```

Windows: `.\gradlew.bat assembleDebug` and `.\gradlew.bat installDebug`.

## Team workflow
Each contributor uses a feature branch and tests with fixtures/fake adapters. LLM + TTS use the same laptop/workstream. Merge shared contracts before adapter integration. Do not commit model binaries unless allowed by their license and repo policy.

## Offline test
After installing all assets, enable airplane mode and test capture → STT → extraction → follow-up/review → TTS. Record results in `EXPERIMENTS.md`.

## Safety
Do not use forced USB boot/Emergency Download mode for app development. Do not change board power switches. Leave the rear camera connector alone.
