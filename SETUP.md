# Development Setup

This guide starts from the laptop side. Android/ADB setup is new for this project, so commands specify where they run.

## Prerequisites

- Qualcomm development board powered by its supplied 12V adapter.
- USB-C connection between board and laptop.
- Android SDK Platform-Tools (`adb`).
- Android Studio/SDK and a compatible JDK/Gradle setup for the Android project.
- Project source and the real Qualcomm runtime documentation/sample apps.

The laptop operating system is not yet confirmed; use the matching installation option below.

## 1. Install ADB / Platform-Tools

### Windows
Install Android Studio, then open **SDK Manager → SDK Tools → Android SDK Platform-Tools**. Alternatively, if supported in your environment:

```powershell
winget install Google.PlatformTools
```

### macOS
```bash
brew install android-platform-tools
```

Confirm installation on the **laptop terminal**:

```bash
adb version
```

## 2. Enable debugging on the board

On the board's Android UI:

1. Open **Settings → About**.
2. Tap **Build number** seven times to enable Developer options.
3. Open **Developer options** and enable **USB debugging**.
4. Accept the authorization prompt if it appears.

Windows may require the Google USB driver.

## 3. Verify the connection

Run these commands on the laptop, with the board powered and connected:

```bash
adb devices
```

The device should appear with status `device`. If it says `unauthorized`, unlock the board and accept the prompt. If the list is empty, check the cable, USB mode, driver, and USB debugging.

Then capture basic device information:

```bash
adb shell getprop ro.product.model
adb shell getprop ro.build.version.release
adb shell getprop ro.build.version.sdk
```

Save these outputs in the team's development notes. Do not assume the model/runtime from the board name alone.

## 4. Inspect the device carefully

Useful first-pass commands:

```bash
adb shell
```

This opens a shell on the board. Type `exit` to return to the laptop terminal.

```bash
adb logcat
```

This streams device logs; stop it with Ctrl+C.

Do not delete or modify unknown files. Ask the Qualcomm mentor before inspecting protected locations or changing runtime assets. Candidate model locations mentioned during planning (`/data/local/tmp`, `/sdcard`) are unconfirmed, not guaranteed paths.

## 5. Build and deploy

Once the Android project exists and Gradle is configured, run from the repository root:

```bash
./gradlew assembleDebug
./gradlew installDebug
```

On Windows, the wrapper may be `gradlew.bat`:

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat installDebug
```

Use Android Studio if Gradle/JDK/SDK configuration fails. Do not guess the required Android Gradle Plugin or SDK versions; use the sample project or environment provided by Qualcomm.

## 6. Debug the app

```bash
adb logcat
```

Filter by package/tag once the package name and logging tags are known. Keep logs free of private incident details in any shared submission.

## 7. Offline verification

After all model assets and app dependencies are installed:

1. Enable airplane mode on the board.
2. Run the full capture → transcription → extraction → follow-up/review workflow.
3. Confirm that STT, LLM, and TTS continue working.
4. Inspect network usage if tooling is available.
5. Record the exact test procedure and result in `EVALUATION.md`.

## Safety notes

- Do not use Emergency Download / forced USB boot mode for ordinary app development.
- Do not change power configuration or DIP switches.
- Leave the rear camera connector alone; it is not needed for this project.
- Use ADB for app development; UART/JTAG are not part of the initial workflow.
