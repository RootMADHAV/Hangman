# ── Google Mobile Ads ─────────────────────────────────────────────────────────
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.android.gms.common.** { *; }
-dontnote com.google.**
-dontwarn com.google.**

# ── UMP / Consent SDK ─────────────────────────────────────────────────────────
-keep class com.google.android.ump.** { *; }

# ── Firebase ──────────────────────────────────────────────────────────────────
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.internal.firebase** { *; }
-dontwarn com.google.firebase.**

# ── Hilt ──────────────────────────────────────────────────────────────────────
# Hilt resolves its generated component/factory/injector classes by name at
# runtime; R8 must not rename or remove them.
-keep class **_HiltModules { *; }
-keep class **_HiltModules$* { *; }
-keep class **_Factory { *; }
-keep class **_MembersInjector { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-keepclassmembers @dagger.hilt.android.lifecycle.HiltViewModel class * {
    <init>(...);
}

# ── Room ──────────────────────────────────────────────────────────────────────
# Entity field names drive DB column mapping; renaming them corrupts the schema.
-keep @androidx.room.Entity class * { *; }
-keepclassmembers @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keep @androidx.room.Database class * { *; }

# ── Kotlin data classes in UI state / navigation ─────────────────────────────
# Keep copy(), equals(), hashCode(), toString() so StateFlow diffing and
# debugging work correctly after obfuscation.
-keepclassmembers class com.hangman.ui.viewmodel.** {
    public ** copy(...);
    public java.lang.String toString();
    public int hashCode();
    public boolean equals(java.lang.Object);
}
-keepclassmembers class com.hangman.domain.model.** {
    public ** copy(...);
    public java.lang.String toString();
    public int hashCode();
    public boolean equals(java.lang.Object);
}

# ── Coroutines / Flow ─────────────────────────────────────────────────────────
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# ── Misc suppression ──────────────────────────────────────────────────────────
-dontwarn org.bouncycastle.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**
