# LockGuard Proguard Rules

# Keep Room entities and DAOs
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Keep Hilt generated classes
-keep class * extends dagger.hilt.internal.GeneratedComponentManager
-keep class * extends dagger.hilt.internal.ComponentManager
-keepclasseswithmembers class * {
    @dagger.hilt.android.qualifiers.* <fields>;
}

# Keep CameraX
-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**

# Keep BiometricPrompt
-keep class androidx.biometric.** { *; }

# Keep domain models
-keep class com.lockguard.app.domain.model.** { *; }
-keep class com.lockguard.app.data.database.** { *; }
