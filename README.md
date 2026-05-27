# Lab 4 — Real-Time Mobile Application Development

**Students:** Tăng Kim Sơn 22521258, Hồng Bảo Ngọc 22520953  
**Repository:** https://github.com/tangkimson/Lab4_Mobile_22521258_22520953

## Description

Kotlin Android game similar to a classic chicken/space shooter. The app uses `SurfaceView` with a dedicated `GameThread` running at ~60 FPS. All seven lab exercises are implemented:

1. Highest score display (persisted with SharedPreferences)
2. 3 lives per session
3. Player spaceship graphic that moves to touch position
4. Spreading bullets (more bullets as score increases)
5. Enemy health bars with random health
6. Enemies move across lanes
7. Boss alien at top spawns minions instead of random enemies

## Prerequisites

- Windows 10/11
- [Android Studio](https://developer.android.com/studio) (latest stable)
- JDK 17 (bundled with Android Studio)
- Android SDK API 34

## How to Open and Run

1. Clone the repository:
   ```bash
   git clone https://github.com/tangkimson/Lab4_Mobile_22521258_22520953.git
   cd Lab4_Mobile_22521258_22520953
   ```
2. Open the project folder in Android Studio.
3. Wait for Gradle sync to finish. Android Studio will create `local.properties` with your SDK path automatically.
4. Connect an emulator (API 24+) or a physical device.
5. Click **Run** (green play button) or use:
   ```bash
   .\gradlew.bat assembleDebug
   ```
6. Install/run the debug APK from `app/build/outputs/apk/debug/app-debug.apk`.

## Build from Command Line

```bash
.\gradlew.bat clean assembleDebug
```

## Project Structure

```
app/src/main/java/com/ex/myapplication/
  MainActivity.kt    - Entry point, sets GameView as content
  GameView.kt        - Central game controller and HUD
  GameThread.kt      - 60 FPS game loop
  GameManager.kt     - Enemy/boss/player bitmap creation
  Opponent.kt        - Enemies with lanes and health bars
  FiringObject.kt    - Player bullets
  Boss.kt            - Top boss that spawns minions
```

## Lab Report

See `Lab4_Report.docx` in the repository root for implementation details and screenshots.

## Notes

- `local.properties` is machine-specific and is **not** committed to Git.
- Touch the screen to move the ship and fire. Tap again after Game Over to restart.
