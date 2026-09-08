package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics

class HatCabApplication : Application() {
  override fun onCreate() {
    super.onCreate()
    try {
      if (FirebaseApp.getApps(this).isNotEmpty()) {
        val crashlytics = FirebaseCrashlytics.getInstance()
        // Collect crash reports in release builds while avoiding clutter during dev testing
        crashlytics.setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
        crashlytics.setCustomKey("app_name", "Hat Cab")
        crashlytics.setCustomKey("version", BuildConfig.VERSION_NAME)
        Log.i("HatCabApp", "Firebase Crashlytics initialized successfully.")
      } else {
        Log.i("HatCabApp", "FirebaseApp not initialized (no google-services.json). Crashlytics skipped gracefully.")
      }
    } catch (e: Exception) {
      Log.w("HatCabApp", "Crashlytics initialization skipped: ${e.message}")
    }
  }
}
