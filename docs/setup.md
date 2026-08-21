# SafeHer Setup & Real-Device Testing Guide

## Prerequisites

1. Android Studio Ladybug / Koala or newer.
2. JDK 17 (Recommended).
3. Android SDK Platforms: `android-34` or `android-36`.

## Building the Project

1. Open Android Studio and select **Open** -> `SafeHer/`.
2. Sync Project with Gradle Files.
3. Build APK:
   ```bash
   ./gradlew assembleDebug
   ```

## Firebase Setup (Optional for Cloud Sync)

1. Create a Firebase project in the [Firebase Console](https://console.firebase.google.com/).
2. Enable **Email/Password Authentication** and **Cloud Firestore**.
3. Download `google-services.json` and place it in the `SafeHer/app/` directory.
4. Deploy the Firestore rules from `SafeHer/firestore.rules`.

## Real-Device Testing Instructions

### 1. SOS Hold & SMS Dispatch
- Add a trusted phone number in **Emergency Contacts**.
- Long press the SOS button on the Home dashboard for 3 seconds.
- Verify tactile vibration on countdown ticks.
- Confirm SMS dispatch containing Google Maps link and distress message.

### 2. Safety Timer & Auto-Escalation
- Set a 1-minute test timer in **Safety Timer**.
- Let the timer reach 0:00.
- Verify that the loud 30-second warning modal triggers.
- Do not confirm safety, and observe automatic escalation to the Emergency SOS workflow.

### 3. Discreet Mode
- Tap **Discreet Mode** on the Home screen.
- Perform calculations on the Calculator interface (e.g. `12 + 8 = 20`).
- Enter the secret PIN `9999` and tap `=` to instantly return to SafeHer.