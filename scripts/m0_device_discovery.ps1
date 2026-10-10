# M0 - QIDK Snapdragon 8 Elite Device Discovery
$ErrorActionPreference = "Continue"

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "  M0: Qualcomm Innovators Development Kit (QIDK) Discovery" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# 1. Check ADB
$cmd = Get-Command adb -ErrorAction SilentlyContinue
if ($cmd) {
    $adbPath = $cmd.Source
} else {
    $fallbackAdb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
    if (Test-Path $fallbackAdb) {
        $adbPath = $fallbackAdb
    } else {
        Write-Error "ADB executable not found. Please verify Android Platform Tools are installed."
        exit 1
    }
}
Write-Host "[OK] Using ADB: $adbPath" -ForegroundColor Green

# 2. Check connected devices
$deviceOutput = & $adbPath devices -l
Write-Host "`nAttached Devices:" -ForegroundColor Yellow
Write-Host ($deviceOutput -join "`n")

$devices = & $adbPath devices | Where-Object { $_ -match "\t(device|unauthorized|offline)" }

if (-not $devices) {
    Write-Host "`n[!] No ADB device detected." -ForegroundColor Red
    Write-Host "Please perform the following steps to connect the QIDK board:" -ForegroundColor Yellow
    Write-Host "  1. Plug in 12V DC power supply to the QIDK board."
    Write-Host "  2. Connect USB-C data cable between laptop and the board USB-C port."
    Write-Host "  3. On board Android UI: Settings -> About -> Tap Build number 7 times."
    Write-Host "  4. Developer options -> Enable USB Debugging."
    Write-Host "  5. Re-run this script: .\scripts\m0_device_discovery.ps1"
    exit 0
}

$unauthorized = $devices | Where-Object { $_ -match "\tunauthorized" }
if ($unauthorized) {
    Write-Host "`n[!] Device is attached but UNAUTHORIZED." -ForegroundColor Red
    Write-Host "Please check the QIDK display screen and accept the USB Debugging RSA authorization prompt." -ForegroundColor Yellow
    exit 0
}

Write-Host "`n[OK] Device connected and authorized. Querying hardware details..." -ForegroundColor Green

# 3. Query Device Properties
$model = ((& $adbPath shell getprop ro.product.model) -join "").Trim()
$manufacturer = ((& $adbPath shell getprop ro.product.manufacturer) -join "").Trim()
$androidVer = ((& $adbPath shell getprop ro.build.version.release) -join "").Trim()
$sdkVer = ((& $adbPath shell getprop ro.build.version.sdk) -join "").Trim()
$soc = ((& $adbPath shell getprop ro.soc.model) -join "").Trim()
$board = ((& $adbPath shell getprop ro.board.platform) -join "").Trim()
$chipset = ((& $adbPath shell getprop ro.hardware.chipname) -join "").Trim()
$fingerprint = ((& $adbPath shell getprop ro.build.fingerprint) -join "").Trim()

Write-Host "`nDevice Profile:" -ForegroundColor Cyan
Write-Host "  - Manufacturer : $manufacturer"
Write-Host "  - Model        : $model"
Write-Host "  - Android Ver  : $androidVer (API $sdkVer)"
Write-Host "  - SoC Model    : $soc"
Write-Host "  - Platform     : $board"
Write-Host "  - Chipset      : $chipset"
Write-Host "  - Fingerprint  : $fingerprint"

# 4. Check Hexagon / QNN Libraries
Write-Host "`nChecking On-Device Qualcomm / NPU Libraries..." -ForegroundColor Cyan
$qnnLibs = & $adbPath shell "ls -l /vendor/lib64/libQnn* /system/lib64/libQnn* 2>/dev/null"
if ($qnnLibs) {
    Write-Host "[OK] Found QNN runtime libraries on device:" -ForegroundColor Green
    $qnnLibs | Select-Object -First 5 | ForEach-Object { Write-Host "    $_" }
} else {
    Write-Host "[-] Standard QNN system libraries not in default /vendor/lib64 path (bundled in app/assets)." -ForegroundColor Yellow
}

# 5. Check Audio Microphones
Write-Host "`nChecking Audio Subsystem..." -ForegroundColor Cyan
$audioDevices = & $adbPath shell "dumpsys audio"
if ($audioDevices) {
    $audioSummary = $audioDevices | Select-String -Pattern "Devices:|AudioDevicePort" | Select-Object -First 5
    Write-Host ($audioSummary -join "`n")
} else {
    Write-Host "[*] Audio service queried."
}

# 6. Check AI Hub status on Host
Write-Host "`nChecking Qualcomm AI Hub Setup on Laptop..." -ForegroundColor Cyan
$clientIni = "$env:USERPROFILE\.qai_hub\client.ini"
if (Test-Path $clientIni) {
    Write-Host "[OK] Qualcomm AI Hub client.ini found at $clientIni" -ForegroundColor Green
} else {
    Write-Host "[!] Qualcomm AI Hub is not configured on this machine." -ForegroundColor Yellow
    Write-Host "    To configure:"
    Write-Host "    1. Sign in to https://aihub.qualcomm.com/"
    Write-Host "    2. Copy your API token from Account Settings"
    Write-Host "    3. Run: qai-hub configure --api_token <YOUR_TOKEN>"
}

Write-Host "`n==========================================================" -ForegroundColor Cyan
Write-Host "  M0 Discovery Complete!" -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Cyan
