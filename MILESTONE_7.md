# Milestone 7: Firebase Authentication & User Onboarding

**Date:** August 15, 2026  
**Status:** Complete ✅

## Overview

Implemented comprehensive Firebase Authentication system with first-launch onboarding, supporting Guest, Google, and Email sign-in methods. Added password reset, guest-to-email linking, and Firestore-backed cloud backup/restore.

## Features Implemented

### 1. Firebase Authentication Integration
- **Guest mode** — Firebase Anonymous Authentication for instant play
- **Google Sign-In** — Real OAuth integration using `GoogleSignInClient` + `ActivityResultLauncher`
- **Email sign-up/sign-in** — Full Firebase Email/Password authentication
- **Email verification** — Sent via `ActionCodeSettings` on sign-up
- **Password reset** — Firebase `sendPasswordResetEmail()` with user-friendly error messages
- **Sign-out** — Clean Firebase sign-out with state cleanup

### 2. First-Launch Authentication Flow
- `MainActivity` observes `AuthRepository.currentUser` on launch
- Unauthenticated users → `AuthScreen`
- Authenticated users → `HomeScreen`
- `AuthScreen` auto-navigates to Home once `AuthState.Authenticated` is observed

### 3. AuthScreen UI
- **Guest button** — "Continue as Guest" (enabled)
- **Google button** — "Continue with Google" (enabled, requires OAuth client ID in `strings.xml`)
- **Email form** — Toggle between Sign In and Sign Up modes
- **Forgot Password** — Dedicated mode for password reset requests
- **Link guest progress** — Available for guest users to link to email
- **Error display** — User-friendly Firebase error code mapping
- **Loading states** — Progress indicator during auth operations

### 4. Guest-to-Email Linking
- Guest users can link their anonymous account to an email/password
- Uses `user.linkWithCredential(EmailAuthProvider.getCredential(email, password))`
- Preserves existing local profile data during linking
- Backs up merged account to Firestore

### 5. Cloud Backup & Restore
- **Firestore collection:** `user_backups`
- **Document ID:** Firebase UID
- **Backed up fields:** nickname, avatarId, totalGamesPlayed, totalTokensEarned, createdAt, lastPlayedAt, authProvider, email
- **Automatic backup** on Google sign-in and guest-to-email linking
- **Automatic restore** on email sign-in

### 6. Local Auth Profile Storage
- `PlayerProfileEntity` stored in DataStore (`user_preferences`)
- Fields: id, nickname, avatarId, totalGamesPlayed, totalTokensEarned, createdAt, lastPlayedAt, authProvider, firebaseUid, email
- Auth provider constants: `guest`, `google`, `email`

### 7. Improved Error Handling
- Firebase auth exceptions mapped to user-friendly messages:
  - `ERROR_INVALID_EMAIL` → "Please enter a valid email address."
  - `ERROR_WRONG_PASSWORD` → "Incorrect password. Please try again."
  - `ERROR_USER_NOT_FOUND` → "No account found with this email address."
  - `ERROR_EMAIL_ALREADY_IN_USE` → "An account already exists with this email address."
  - `ERROR_WEAK_PASSWORD` → "Password is too weak. Please use at least 6 characters."
  - `ERROR_NETWORK_REQUEST_FAILED` → "Network error. Please check your connection."

### 8. Level Titles & Progress (Carried Forward)
- Expanded `PlayerLevel.titleFor()` with custom titles:
  - Level 1: Beginner → Level 2: Newbie → Level 3: Novice → Level 5: Noob → Level 7: Skilled
  - Level 10: Pro → Level 15: Expert → Level 20: Master → Level 25: Champion
  - Level 30: Thronebreaker → Level 40: Almighty → Level 50: Legend
- Level badge and XP progress bar displayed in ProfileScreen

## Architecture

### New Domain Layer
- `AuthState.kt` — sealed class: Loading, Unauthenticated, Authenticated, Error
- `AuthResult.kt` — sealed class: Success, Error, Data
- `AuthRepository.kt` — interface with 10 auth operations
- Use cases: `SignInWithGoogleUseCase`, `SignInWithEmailUseCase`, `SignUpWithEmailUseCase`, `SignInAsGuestUseCase`, `LinkGuestToEmailUseCase`, `SignOutUseCase`, `SendEmailVerificationUseCase`, `ReloadUserUseCase`, `BackupUserDataUseCase`, `RestoreUserDataUseCase`, `SendPasswordResetUseCase`

### New Data Layer
- `AuthRepositoryImpl.kt` — Firebase Auth + Firestore implementation
- `PlayerProfileEntity.kt` — local auth profile model
- `AuthProfileLocalDataSource.kt` — DataStore-backed profile persistence

### New UI Layer
- `AuthScreen.kt` — complete authentication UI with Google Sign-In launcher
- `AuthViewModel.kt` — state management for auth flows
- Updated `ProfileScreen.kt` — level progress, guest linking, sign-out
- Updated `MainActivity.kt` — first-launch auth routing

### DI Updates
- `DatabaseModule.kt` — provides `FirebaseAuth`, `FirebaseFirestore`
- `RepositoryModule.kt` — binds `AuthRepositoryImpl`, `SendPasswordResetUseCase`

## Files Created (8 New)
- AuthState.kt
- AuthResult.kt
- AuthRepository.kt
- AuthRepositoryImpl.kt
- PlayerProfileEntity.kt
- AuthProfileLocalDataSource.kt
- AuthViewModel.kt
- AuthScreen.kt
- 8 use case files

## Files Modified (10+)
- MainActivity.kt
- ProfileScreen.kt
- PlayerLevel.kt
- DatabaseModule.kt
- RepositoryModule.kt
- strings.xml
- build.gradle.kts (Firebase dependencies)
- libs.versions.toml (Firebase BOM, play-services-auth)

## Firebase Dependencies Added
```gradle
firebase-bom = "33.4.0"
firebase-auth-ktx
firebase-firestore-ktx
play-services-auth = "21.2.0"
```

**Package:** `com.LetterQuest`

## Build Information

**Latest Build:** August 15, 2026  
**APK:** `app/build/outputs/apk/debug/app-debug.apk` (88 MB)  
**Build Status:** ✅ SUCCESSFUL  
**Kotlin:** 2.0.21  
**Build Time:** ~1m

## Configuration Required

1. Replace `YOUR_WEB_CLIENT_ID` in `app/src/main/res/values/strings.xml` with your Firebase OAuth web client ID
2. Ensure `google-services.json` is present in `app/` with package name `com.LetterQuest` and Google Sign-In enabled

## Known Limitations

- Google Sign-In requires valid OAuth 2.0 web client ID configuration
- Firestore security rules not included (app currently uses default/development rules)
- Password reset email template uses Firebase default

## Testing Status

✅ Build successful  
✅ All auth flows compile  
✅ Error handling covers common Firebase auth failures  
✅ Local profile persistence via DataStore  
✅ Firestore backup/restore logic implemented  

## Next Steps

1. Configure OAuth client ID in Firebase Console
2. Set up Firestore security rules for production
3. Test auth flows on physical device
4. Add auth state persistence testing
5. Implement account deletion flow

---

**Status:** COMPLETE ✅
