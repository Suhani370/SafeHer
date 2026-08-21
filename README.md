# SafeHer - Production-Grade Women Safety Android Application

> **"Your safety, one tap away."**

SafeHer is a comprehensive, production-quality Android application designed to provide reliable, multi-layered personal safety protection for women. Built with **Kotlin, Jetpack Compose, Material 3, Clean Architecture, MVVM, Room Database, Firebase, WorkManager, and Google Play Location Services**, SafeHer is designed to operate seamlessly across varying network conditions, battery levels, and Android OS background restrictions.

---

## Key Features

1. **One-Tap Emergency SOS**
   - 3-second tactile hold activation with haptic feedback to prevent false alarms.
   - High-accuracy Fused Location capture with automatic fallback to last known coordinates.
   - Multi-channel alert dispatch: Cloud (Firestore) + SMS Fallback with Google Maps links + Direct Emergency Calling (`112` / configurable).
   - SOS Active distress dashboard showing live elapsed duration, coordinates, audio evidence recorder, and secure safety pin deactivation.

2. **Emergency Contacts Management**
   - Add, edit, prioritize (Primary/Secondary), and categorize contacts (Family, Friends, Helplines).
   - Built-in **"Test Safety Alert"** mechanism allowing users to test SMS alerts without triggering a real emergency.
   - Comprehensive phone number validation (E.164 standard).

3. **Autonomous Safety Timer & Escalation**
   - Set transit timers (15m, 30m, 45m, 60m, custom) with destination note.
   - Loud check-in alarm and full-screen grace countdown when timer runs out.
   - Automatically escalates to full Emergency SOS if safety is not confirmed.

4. **Live Journey Monitoring**
   - Foreground service tracking with route status and arrival monitoring.
   - Local waypoint buffer and cloud synchronization.

5. **Discreet Mode ("Quick Exit")**
   - Disguises the screen into a functional, interactive Calculator.
   - Secret PIN gesture (`9999=`) or long press instantly restores the safety dashboard.

6. **Incident Audio Evidence Recording**
   - Background audio evidence capture using `MediaRecorder` in AAC/M4A format.
   - Stored in private app storage with secure `FileProvider` export and sharing.

7. **Nearby Emergency Help**
   - Proximity finder for Police Stations, Hospitals, and 24/7 Pharmacies using OpenStreetMap / Overpass API and offline cached emergency directories.
   - 1-Tap direct dialing and turn-by-turn Google Maps navigation.

8. **Configurable Emergency Helplines**
   - Built-in directory for official national helplines: `112` (Emergency), `1091` (Women Helpline), `181` (Domestic Abuse), `1930` (Cyber Crime), `1098` (Childline).
   - Custom emergency number configuration in settings.

9. **Offline-First Architecture & WorkManager Sync**
   - Room Database as single source of truth.
   - Automatic background retry with exponential backoff via `EmergencySyncWorker` upon network reconnection.

---

## Tech Stack & Architecture

- **Language:** 100% Kotlin
- **UI Framework:** Jetpack Compose with Material 3 Design System
- **Architecture:** MVVM + Clean Architecture (Presentation, Domain, Data, Core)
- **Dependency Injection:** Hilt (Dagger)
- **Local Persistence:** Room Database (User, Contacts, Incidents, Waypoints, Sync Queue)
- **Preferences:** Jetpack DataStore Preferences
- **Cloud Backend:** Firebase Authentication & Cloud Firestore
- **Location Services:** Google Play Services FusedLocationProviderClient
- **Background Execution:** Android Foreground Services (`location`, `microphone`) & WorkManager
- **Networking:** OkHttp, Retrofit, Gson
- **Testing:** JUnit4, MockK, Kotlinx Coroutines Test, Turbine

---

## Documentation Index

- [Architecture & Design Decisions](docs/architecture.md)
- [Database Schema & Entities](docs/database.md)
- [API & Cloud Synchronization](docs/api.md)
- [Security, Privacy & Permissions](docs/security.md)
- [Testing Strategy & Edge Cases](docs/testing.md)
- [Setup & Real-Device Testing Guide](docs/setup.md)