# SafeHer Architecture & Design Specification

## Clean Architecture Layers

SafeHer follows the official Android Architecture recommendations using Clean Architecture and MVVM.

```
+-------------------------------------------------------------------------------+
|                             Presentation Layer                                |
|   Jetpack Compose UI | Material 3 Design System | Navigation Compose | ViewModels |
+-------------------------------------------------------------------------------+
                                      | (StateFlow / UI Events)
                                      v
+-------------------------------------------------------------------------------+
|                                Domain Layer                                   |
|       Domain Models | Business Rules | Repository Interfaces | Use Cases      |
+-------------------------------------------------------------------------------+
                                      |
                                      v
+-------------------------------------------------------------------------------+
|                                 Data Layer                                    |
|   Room Database (Local Truth) | Firebase (Remote Sync) | DataStore Preferences|
|   Location Client | Audio Recorder | SMS/Call Dispatcher | Sync Worker Engine |
+-------------------------------------------------------------------------------+
                                      |
                                      v
+-------------------------------------------------------------------------------+
|                             Core & System Layer                               |
|   Foreground Services | WorkManager | Permissions Manager | Security / Crypto |
+-------------------------------------------------------------------------------+
```

### 1. Presentation Layer
- **Jetpack Compose + Material 3:** Declarative UI with state hoisting.
- **ViewModels:** Expose immutable `StateFlow<UiState>` collected via `collectAsStateWithLifecycle()`.
- **Navigation:** Single activity architecture (`MainActivity`) hosting `SafeHerNavGraph`.

### 2. Domain Layer
- **Models:** Pure Kotlin data classes (`UserProfile`, `EmergencyContact`, `Incident`, `SafetyTimer`, `Journey`, `NearbyPlace`).
- **Repositories:** Clean interfaces decoupling data sources from business logic.
- **Use Cases:** Granular business operations (`TriggerSosUseCase`, `CancelSosUseCase`, `ManageContactsUseCase`, `GetNearbyHelpUseCase`).

### 3. Data Layer
- **Room Database:** Serves as the single source of truth for all local state.
- **Firebase Firestore & Auth:** Cloud synchronization layer for cross-device persistence and live location streams.
- **DataStore:** Encrypted key-value storage for user settings, emergency numbers, and app preferences.

### 4. Background & Foreground Services
- **LocationForegroundService:** Handles continuous GPS coordinate acquisition during active SOS and Journey tracking with user-visible ongoing notification.
- **AudioRecordingService:** Dedicated microphone foreground service ensuring reliable audio evidence capture.
- **WorkManager:** Background periodic syncing (`EmergencySyncWorker`) and timer escalation resurrection (`SafetyTimerEscalationWorker`).