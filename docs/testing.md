# SafeHer Testing Strategy & Test Execution

## Test Suites

1. **Unit Tests (`src/test/java/com/safeher/app/`):**
   - `PhoneValidatorTest`: Tests international E.164 formats, edge cases, whitespace sanitization, and invalid inputs.
   - `SecurityUtilsTest`: Verifies SOS SMS formatting, Google Maps link generation, and test alert disclaimers.
   - `HaversineDistanceTest`: Tests distance calculation accuracy between coordinates.

2. **Running Unit Tests:**
   ```bash
   ./gradlew testDebugUnitTest
   ```

3. **Running Instrumentation UI Tests:**
   ```bash
   ./gradlew connectedDebugAndroidTest
   ```