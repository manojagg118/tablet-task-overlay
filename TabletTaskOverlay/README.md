# TabletTaskOverlay

Android app built with Kotlin + Jetpack Compose for tablet-first task capture using stylus and voice commands.

## Features Implemented
- Lock-screen mode activity (`LockScreenTaskActivity`) with `showWhenLocked` + `turnScreenOn`
- Foreground overlay service (`TaskOverlayService`) with always-on-top task panel (`SYSTEM_ALERT_WINDOW`)
- Room database persistence for task lifecycle:
  - `id`
  - `text`
  - `created_at`
  - `closed_at` (nullable)
  - `status` (`OPEN` / `CLOSED`)
- Stylus handwriting canvas with ML Kit Digital Ink recognition
- Voice commands via `SpeechRecognizer`:
  - `write a task`
  - `close task <id or name>`
- Daily automation with WorkManager:
  - 8:00 AM open-task reminder
  - 8:00 PM closure prompt
- Tablet-friendly Compose UI (large text, large buttons, swipe-to-close)
- Debug mode behavior (no strict lock-screen enforcement, fake seed tasks)
- Bonus:
  - Dark mode (Material3 DayNight)
  - Swipe task to close
  - Export tasks to JSON

## Project Structure
- `app/src/main/java/com/example/tablettaskoverlay/ui` - screens, view model, handwriting UI
- `app/src/main/java/com/example/tablettaskoverlay/db` - Room entities/DAO/database
- `app/src/main/java/com/example/tablettaskoverlay/repo` - repository layer
- `app/src/main/java/com/example/tablettaskoverlay/voice` - speech parsing and recognizer manager
- `app/src/main/java/com/example/tablettaskoverlay/handwriting` - ML Kit handwriting recognizer
- `app/src/main/java/com/example/tablettaskoverlay/overlay` - persistent overlay service
- `app/src/main/java/com/example/tablettaskoverlay/worker` - scheduled morning/evening workers

## Run on Emulator (Testing Mode)
1. Open project in Android Studio.
2. Let Gradle sync and install SDK packages.
3. Run `app` on a tablet emulator (Android 13+ recommended).
4. In app:
   - Tap `Start Overlay` and grant overlay permission when prompted.
   - Tap `Voice command` and grant microphone permission.
   - Tap `Write a task` to open handwriting canvas.

Notes:
- Emulator stylus can be simulated with mouse drag in handwriting canvas.
- Lock-screen behavior on emulator is limited compared to real device security layers.

## Run on Real Tablet
1. Install debug APK from Android Studio.
2. Grant permissions:
   - Overlay (`Draw over other apps`)
   - Microphone
   - Notifications
3. Enable lock screen mode from app (`Lock Screen Mode`).
4. Start overlay service (`Start Overlay`).

## Voice Commands
- `Write a task`
- `Close task 3`
- `Close task vendor follow-up`

## Permission Handling
- Overlay: checked via `Settings.canDrawOverlays`
- Lock-screen display: handled via activity flags and manifest attributes
- Microphone: runtime `RECORD_AUDIO`
- Notifications: runtime `POST_NOTIFICATIONS` (Android 13+)

## Important Platform Constraints
Android does not guarantee unrestricted third-party overlays over every OEM lock screen/security surface. This project implements the strongest supported combination:
- lock-screen activity +
- overlay service +
- foreground notification

## Build note
This workspace includes Gradle config and source files. If wrapper binaries are missing, Android Studio sync will regenerate/manage them.
