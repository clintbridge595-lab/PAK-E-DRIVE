# ProGuard / R8 Rules for Hat Cab

# Preserve line numbers and source files for readable stack traces in Crashlytics
-keepattributes SourceFile,LineNumberTable,*Annotation*,Signature,InnerClasses,EnclosingMethod

# ---------------------------------------------------------------------------
# Moshi Rules
# ---------------------------------------------------------------------------
-dontwarn com.squareup.moshi.**
-keep class com.squareup.moshi.** { *; }
-keep interface com.squareup.moshi.** { *; }

# Keep all classes with Moshi JsonClass annotation
-keep @com.squareup.moshi.JsonClass class * { *; }
-keep class *JsonAdapter { *; }

# Keep data models so serialization / reflection doesn't strip fields
-keep class com.example.data.model.** { *; }

# ---------------------------------------------------------------------------
# Retrofit & OkHttp Rules
# ---------------------------------------------------------------------------
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# ---------------------------------------------------------------------------
# Firebase & Crashlytics Rules
# ---------------------------------------------------------------------------
-keepattributes *Annotation*,Signature
-dontwarn com.google.firebase.**
-keep class com.google.firebase.** { *; }
-keep class com.google.firebase.crashlytics.** { *; }

# ---------------------------------------------------------------------------
# Room Database Rules
# ---------------------------------------------------------------------------
-dontwarn androidx.room.**
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Dao interface * { *; }
-keep @androidx.room.Entity class * { *; }

# ---------------------------------------------------------------------------
# Kotlin Coroutines
# ---------------------------------------------------------------------------
-dontwarn kotlinx.coroutines.**
-keepclassmembers class kotlinx.coroutines.** { *; }
