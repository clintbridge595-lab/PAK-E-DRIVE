package com.example

import android.app.Application
import android.os.Bundle
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics

class PakEDriveApplication : Application() {

  companion object {
    @Volatile
    private var analyticsInstance: FirebaseAnalytics? = null

    fun logBookingRoute(carName: String, fromCity: String, toCity: String, rate: Long) {
      try {
        analyticsInstance?.let { analytics ->
          val bundle = Bundle().apply {
            putString("car_name", carName)
            putString("from_city", fromCity)
            putString("to_city", toCity)
            putString("route_pair", "$fromCity to $toCity")
            putLong("estimated_rate_pkr", rate)
            putString(FirebaseAnalytics.Param.ITEM_NAME, carName)
            putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "car_rental")
          }
          analytics.logEvent("booking_route_selected", bundle)
        }
      } catch (e: Exception) {
        Log.w("PakEDriveApp", "Analytics tracking skipped: ${e.message}")
      }
    }

    fun logUserEngagement(action: String, label: String) {
      try {
        analyticsInstance?.let { analytics ->
          val bundle = Bundle().apply {
            putString("action", action)
            putString("label", label)
          }
          analytics.logEvent("user_engagement_event", bundle)
        }
      } catch (_: Exception) {}
    }
  }

  override fun onCreate() {
    super.onCreate()
    try {
      if (FirebaseApp.getApps(this).isNotEmpty()) {
        val crashlytics = FirebaseCrashlytics.getInstance()
        // Collect crash reports in release builds while avoiding clutter during dev testing
        crashlytics.setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
        crashlytics.setCustomKey("app_name", "PAK E DRIVE")
        crashlytics.setCustomKey("version", BuildConfig.VERSION_NAME)
        Log.i("PakEDriveApp", "Firebase Crashlytics initialized successfully.")

        // Initialize Firebase Analytics for route tracking and engagement
        analyticsInstance = FirebaseAnalytics.getInstance(this).apply {
          setAnalyticsCollectionEnabled(!BuildConfig.DEBUG)
          setUserProperty("region", "Pakistan")
        }
        Log.i("PakEDriveApp", "Firebase Analytics initialized successfully.")
      } else {
        Log.i("PakEDriveApp", "FirebaseApp not initialized (no google-services.json). Firebase skipped gracefully.")
      }
    } catch (e: Exception) {
      Log.w("PakEDriveApp", "Firebase initialization skipped: ${e.message}")
    }
  }
}
