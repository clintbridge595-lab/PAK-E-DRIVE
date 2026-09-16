package com.example

import android.app.Application
import android.os.Bundle
import android.util.Log
import com.pomo.mypomo.BuildConfig
import com.example.data.repository.CrashLogRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PakEDriveApplication : Application() {

  companion object {
    @Volatile
    private var analyticsInstance: FirebaseAnalytics? = null
    @Volatile
    private var crashLogRepoInstance: CrashLogRepository? = null

    fun getCrashLogRepository(): CrashLogRepository? = crashLogRepoInstance

    fun logCrash(throwable: Throwable, tag: String = "AppException", severity: String = "CRITICAL") {
      crashLogRepoInstance?.let { repo ->
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
          repo.logException(throwable, tag, severity)
        }
      }
    }

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
    // Initialize offline Room crash repository
    try {
      val repo = CrashLogRepository.getInstance(this)
      crashLogRepoInstance = repo

      // Global uncaught exception handler to store crashes in Room when offline
      val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
      Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        try {
          kotlinx.coroutines.runBlocking(Dispatchers.IO) {
            repo.logException(
              throwable = throwable,
              tag = "UncaughtCrash_${thread.name}",
              severity = "CRITICAL",
              customMessage = "Fatal uncaught crash on thread ${thread.name}: ${throwable.message}"
            )
          }
        } catch (_: Exception) {}
        defaultHandler?.uncaughtException(thread, throwable)
      }
    } catch (e: Exception) {
      Log.w("PakEDriveApp", "Error setting up Room CrashLogRepository: ${e.message}")
    }

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
    } catch (t: Throwable) {
      Log.w("PakEDriveApp", "Firebase initialization skipped safely: ${t.message}")
    }
  }
}
