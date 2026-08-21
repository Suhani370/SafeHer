# SafeHer Security, Privacy & Permissions Guide

## Security Architecture

1. **Local Data Protection:**
   - All sensitive data (emergency contacts, user profile, incident logs) is stored within private app storage (`/data/data/com.safeher.app/`).
   - Recorded audio evidence is placed in `context.filesDir/incidents/` and shared only via Android's secure `FileProvider`.

2. **No Hardcoded Secrets:**
   - Google Maps API key and backend configurations use Gradle `buildConfigField` and environment variables / `secrets.properties`.

3. **Cloud Firestore Security Rules (`firestore.rules`):**
   - Strictly scopes all document reads and writes to authenticated users (`request.auth.uid == userId`). No open rules (`allow read, write: if true;`) exist.

4. **Permissions Architecture:**
   - **Contextual Permission Requests:** Permissions are requested at the moment a feature is used rather than in a bulk request on initial app launch.
   - **Graceful Fallbacks:** If Location permission is denied, SafeHer continues to provide emergency dialing, helpline resources, and offline SMS fallback.