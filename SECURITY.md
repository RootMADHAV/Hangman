# Firestore Security Model

## Overview

This document describes the Firestore security rules and the trust model for the LetterQuest application. The guiding principle is: **do not trust the Android client**. All sensitive operations must be validated server-side or blocked entirely.

---

## Collections & Access Matrix

| Collection | Read | Write | Notes |
|------------|------|-------|-------|
| `leaderboards/{metric}/entries/{uid}` | Public | **Blocked for clients** | Must be written by trusted backend only |
| `user_profiles/{uid}` | Public | Owner only | Field types and lengths validated |
| `user_game_history/{uid}/entries/{entryId}` | Owner only | Owner only | Field types and lengths validated |
| `user_achievements/{uid}/entries/{achievementId}` | Owner only | Owner only | Field types and lengths validated |
| `user_backups/{uid}` | Owner only | Owner only | Used for account linking |
| `user_sync_meta/{uid}` | Owner only | Owner only | Sync timestamps, queue state |

---

## Leaderboard Security

### Problem: Direct Client Writes Are Insecure

The original leaderboard design allowed the Android client to write scores directly to `leaderboards/{metric}/entries/{uid}`. This is fundamentally insecure because:

1. **Arbitrary value injection**: A modified APK, rooted device, or proxy can send any `value`, `gamesWon`, `gamesPlayed`, or `updatedAt` the attacker wants.
2. **No server-side verification**: Firestore rules cannot verify that a score is truthful because the client controls all the data it submits.
3. **Rule validation is insufficient**: Even with rules like `request.resource.data.value is number`, a cheater can set `value` to `999999999`.

### Solution: Trusted Backend Only

Leaderboard writes are **blocked for all clients** in `firestore.rules`:

```javascript
match /leaderboards/{metric}/entries/{uid} {
  allow read: if true;
  allow write: if false;  // No client writes allowed
}
```

#### Recommended Architecture

```
┌─────────────┐     ┌──────────────────┐     ┌─────────────────┐
│   Android   │────▶│  Cloud Function  │────▶│  leaderboards/  │
│   Client    │     │  (Node.js / TS)  │     │  {metric}/...   │
└─────────────┘     └──────────────────┘     └─────────────────┘
       │                       │
       │  1. Write verified     │  3. Admin SDK write
       │     game history       │     (bypasses rules)
       │  2. Trigger function   │
       ▼                       ▼
┌─────────────────────────────────────────┐
│  user_game_history/{uid}/entries/{id}   │
│  user_profiles/{uid}                    │
└─────────────────────────────────────────┘
```

**Steps:**

1. **Client writes game results** to `user_game_history/{uid}/entries/{entryId}` (already authenticated and validated by rules).
2. **Cloud Function triggers** on new game history writes.
3. **Cloud Function aggregates** verified statistics:
   - Counts total games played
   - Counts games won
   - Calculates win rate (with minimum game threshold)
   - Derives total score from verified profile data
4. **Cloud Function writes** aggregated scores to `leaderboards/{metric}/entries/{uid}` using the Firebase Admin SDK, which **bypasses Firestore security rules**.
5. **Leaderboard reads remain public** so all users can view rankings.

### Why This Works

- The Cloud Function runs in a trusted Google Cloud environment with a service account.
- The Admin SDK has full access and bypasses Firestore rules.
- The function derives scores from **verified** game history, not from client-submitted values.
- Even if a client modifies their local game history before upload, the function can implement anti-cheat logic (e.g., detect impossible win rates, flag suspicious patterns).

---

## User Data Protection

### Ownership Validation

All private user collections validate `request.auth.uid == {documentUserId}`:

```javascript
allow write: if request.auth != null && request.auth.uid == uid;
```

This ensures:
- A user can only write to their own profile, game history, achievements, backups, and sync metadata.
- One user cannot overwrite another user's private data.
- Unauthenticated users cannot write to any user data.

### Field Validation

Every write to user data is validated for:
- **Required fields present**: All expected keys must exist.
- **Type safety**: `string`, `int`, `long`, `bool` types are enforced.
- **Length limits**: Prevents oversized payloads (e.g., `nickname.size() <= 64`).
- **Value bounds**: Non-negative scores, valid timestamps, etc.

#### Example: Profile Validation

```javascript
function isValidProfile(data) {
  return data.keys().hasAll(['playerId', 'nickname', 'username', 'avatarId',
                             'totalGamesPlayed', 'totalTokensEarned',
                             'createdAt', 'updatedAt'])
      && data.playerId is string && data.playerId.size() <= 128
      && data.nickname is string && data.nickname.size() <= 64
      && data.username is string && data.username.size() <= 32
      && data.avatarId is string && data.avatarId.size() <= 32
      && data.totalGamesPlayed is int && data.totalGamesPlayed >= 0
      && data.totalTokensEarned is int && data.totalTokensEarned >= 0
      && data.createdAt is long
      && data.updatedAt is long;
}
```

### Public Profile Reads

`user_profiles/{uid}` allows public reads (`allow read: if true`) because:
- Leaderboards display username, nickname, and avatar.
- No sensitive PII (email, password) is stored in profiles.
- If future requirements change to hide profiles, change this to `request.auth != null`.

---

## Anti-Cheat Considerations

### Current Limitations

Without a Cloud Function, the app cannot fully prevent:
- **Modified APKs**: A user can patch the app to submit fake game results or inflated scores.
- **Rooted/jailbroken devices**: System-level hooks can intercept and modify network requests.
- **Replay attacks**: Uploading the same winning game result multiple times.

### Mitigations (Client-Side)

- **UUID uniqueness**: Each game history entry has a UUID. The local DB enforces uniqueness, preventing duplicate uploads of the same game.
- **Timestamp validation**: Rules require `updatedAt` to be a valid `long`, making it harder to submit stale data.
- **Sync queue**: Offline changes are queued and drained in order, reducing the chance of race conditions.

### Mitigations (Server-Side - Requires Cloud Functions)

- **Impossible score detection**: Flag users with win rates > 95% or average scores that exceed theoretical maximums.
- **Rate limiting**: Limit how many game results a user can submit per hour.
- **Statistical anomaly detection**: Identify accounts that always win in exactly N seconds (bot behavior).
- **Hash verification**: Store a server-side hash of game results and verify consistency on sync.

---

## Deployment Checklist

Before deploying to production:

1. **Deploy `firestore.rules`**:
   ```bash
   firebase deploy --only firestore:rules
   ```

2. **Deploy `firestore.indexes.json`**:
   ```bash
   firebase deploy --only firestore:indexes
   ```

3. **Deploy Cloud Functions** (for secure leaderboard):
   ```bash
   cd functions
   npm install
   npm run build
   firebase deploy --only functions
   ```

4. **Verify rules in Firebase Console**:
   - Go to Firestore > Rules
   - Run the Rules Playground to test common scenarios
   - Ensure `allow write: if false` is active for leaderboards

5. **Monitor Firebase Usage**:
   - Enable Firestore Usage dashboard
   - Set up alerts for unusual write patterns (potential cheating)

---

## Rule Maintenance

- **Do not** weaken rules for convenience (e.g., `allow write: if true`).
- **Do not** add client-side leaderboard writes without a security review.
- **Do** add new collections with ownership validation from the start.
- **Do** update `isValid*` functions when adding new fields to documents.
