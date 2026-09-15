package com.example.services

import android.app.*
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.google.firebase.database.FirebaseDatabase

/**
 * Real-time Foreground Location Tracking Service for PAK E DRIVE Drivers.
 * Transmits GPS coordinates (Latitude, Longitude, Heading, Timestamp)
 * to Firebase Realtime Database for 1-second live ride tracking.
 */
class LocationTrackingService : Service() {

  private lateinit var fusedLocationClient: FusedLocationProviderClient
  private lateinit var locationCallback: LocationCallback
  private var isTracking = false

  override fun onCreate() {
    super.onCreate()
    fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

    startForegroundService()

    locationCallback = object : LocationCallback() {
      override fun onLocationResult(locationResult: LocationResult) {
        for (location in locationResult.locations) {
          val driverId = "DRIVER_PAKEDRIVE_001"
          val locationMap = mapOf(
            "latitude" to location.latitude,
            "longitude" to location.longitude,
            "heading" to location.bearing,
            "speed" to location.speed,
            "timestamp" to System.currentTimeMillis()
          )
          try {
            val databaseRef = FirebaseDatabase.getInstance().getReference("drivers_locations")
            databaseRef.child(driverId).setValue(locationMap)
          } catch (_: Exception) {
            // Graceful offline fallback
          }
        }
      }
    }
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    if (!isTracking) {
      startLocationUpdates()
      isTracking = true
    }
    return START_STICKY
  }

  private fun startLocationUpdates() {
    val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000)
      .setMinUpdateIntervalMillis(1500)
      .build()

    try {
      fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    } catch (_: SecurityException) {
      stopSelf()
    }
  }

  private fun startForegroundService() {
    val channelId = "location_tracking_channel"
    val channelName = "PAK E DRIVE Live Tracking"
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
      getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }

    val notification: Notification = NotificationCompat.Builder(this, channelId)
      .setContentTitle("PAK E DRIVE Online")
      .setContentText("Transmitting real-time GPS location...")
      .setSmallIcon(android.R.drawable.ic_menu_compass)
      .setOngoing(true)
      .build()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      startForeground(101, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
    } else {
      startForeground(101, notification)
    }
  }

  override fun onBind(intent: Intent?): IBinder? = null

  override fun onDestroy() {
    super.onDestroy()
    if (::fusedLocationClient.isInitialized && ::locationCallback.isInitialized) {
      fusedLocationClient.removeLocationUpdates(locationCallback)
    }
    isTracking = false
  }
}
