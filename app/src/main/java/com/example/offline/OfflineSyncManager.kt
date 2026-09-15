package com.example.offline

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

object OfflineSyncManager {

  /**
   * Schedule persistent background synchronization of a booking request
   * using Android WorkManager with network constraints.
   */
  fun enqueueOfflineBooking(
    context: Context,
    pickup: String,
    dropoff: String,
    carName: String,
    fare: Double,
    clientName: String,
    phone: String
  ) {
    val constraints = Constraints.Builder()
      .setRequiredNetworkType(NetworkType.CONNECTED)
      .build()

    val inputData = Data.Builder()
      .putString("pickup", pickup)
      .putString("dropoff", dropoff)
      .putString("carName", carName)
      .putDouble("fare", fare)
      .putString("clientName", clientName)
      .putString("phone", phone)
      .build()

    val syncWorkRequest = OneTimeWorkRequestBuilder<OfflineBookingWorker>()
      .setConstraints(constraints)
      .setInputData(inputData)
      .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
      .addTag("offline_car_booking")
      .build()

    WorkManager.getInstance(context).enqueue(syncWorkRequest)
  }
}
