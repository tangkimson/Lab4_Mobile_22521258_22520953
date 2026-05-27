# Capture real screenshots from Android emulator/device.
$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
$Sdk = "$env:LOCALAPPDATA\Android\Sdk"
$Adb = "$Sdk\platform-tools\adb.exe"
$Emulator = "$Sdk\emulator\emulator.exe"
$Apk = Join-Path $Root "app\build\outputs\apk\debug\app-debug.apk"
$OutDir = Join-Path $Root "docs\screenshots"
$Flag = Join-Path $OutDir ".real_captures"
$Avd = "Medium_Phone_API_36.1"
$Package = "com.ex.myapplication"
$Activity = "$Package/.MainActivity"

New-Item -ItemType Directory -Force -Path $OutDir | Out-Null

function Get-AdbDevice {
    $lines = & $Adb devices 2>&1 | Out-String
    return ($lines -match "\tdevice")
}

function Wait-Boot {
    param([int]$MaxSeconds = 180)
    $elapsed = 0
    while ($elapsed -lt $MaxSeconds) {
        if (Get-AdbDevice) {
            $boot = & $Adb shell getprop sys.boot_completed 2>&1
            if ($boot -match "1") { return $true }
        }
        Start-Sleep -Seconds 5
        $elapsed += 5
        Write-Host "Waiting for boot... ${elapsed}s"
    }
    return $false
}

function Capture-Screen {
    param([string]$FileName)
    $path = Join-Path $OutDir $FileName
    & $Adb exec-out screencap -p | Set-Content -Path $path -Encoding Byte
    if (Test-Path $path) { Write-Host "Captured $FileName" }
}

function Tap {
    param([int]$X, [int]$Y)
    & $Adb shell input tap $X $Y 2>&1 | Out-Null
}

# Build APK if missing
if (-not (Test-Path $Apk)) {
    Write-Host "Building APK..."
    Push-Location $Root
    & .\gradlew.bat assembleDebug
    Pop-Location
}

# Start emulator if no device
if (-not (Get-AdbDevice)) {
    Write-Host "Starting emulator $Avd..."
    Start-Process -FilePath $Emulator -ArgumentList "-avd", $Avd, "-no-snapshot-load" -WindowStyle Minimized
}

if (-not (Wait-Boot)) {
    Write-Host "Emulator did not boot in time. Using composite screenshots."
    python (Join-Path $Root "scripts\generate_screenshots.py")
    if (Test-Path $Flag) { Remove-Item $Flag -Force }
    exit 0
}

Write-Host "Installing APK..."
& $Adb install -r $Apk 2>&1 | Out-Null
& $Adb shell am force-stop $Package 2>&1 | Out-Null
Start-Sleep -Seconds 1
& $Adb shell am start -n $Activity 2>&1 | Out-Null
Start-Sleep -Seconds 4

# Gameplay + HUD
Tap 540 1700
Start-Sleep -Milliseconds 500
Tap 300 1700
Start-Sleep -Milliseconds 500
Tap 780 1700
Start-Sleep -Seconds 2
Capture-Screen "01_gameplay.png"
Capture-Screen "02_hud.png"

# Boss + minions
Start-Sleep -Seconds 6
Capture-Screen "03_boss_minions.png"

# Game over — wait for lives to deplete
Write-Host "Waiting for game over..."
Start-Sleep -Seconds 55
Capture-Screen "04_game_over.png"

# Mark real captures
Set-Content -Path $Flag -Value (Get-Date -Format "o")
Write-Host "Real screenshots captured successfully."
