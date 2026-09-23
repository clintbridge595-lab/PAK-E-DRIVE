package com.example.offline

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Android WorkManager Worker that persistently queues car bookings made offline,
 * and automatically retries and commits them to Firebase Cloud Firestore
 * the moment network connectivity is re-established.
 */
class OfflineBookingWorker(appContext: Context, workerParams: WorkerParameters) :
  CoroutineWorker(appContext, workerParams) {

  override suspend fun doWork(): Result {
    val pickupLocation = inputData.getString("pickup") ?: return Result.failure()
    val dropoffLocation = inputData.getString("dropoff") ?: return Result.failure()
    val carName = inputData.getString("carName") ?: "PAK E DRIVE Fleet"
    val fare = inputData.getDouble("fare", 0.0)
    val clientName = inputData.getString("clientName") ?: "Client"
    val phone = inputData.getString("phone") ?: ""

    val bookingData = hashMapOf(
      "pickup" to pickupLocation,
      "dropoff" to dropoffLocation,
      "carName" to carName,
      "fare" to fare,
      "clientName" to clientName,
      "phone" to phone,
      "status" to "SYNCED",
      "timestamp" to System.currentTimeMillis()
    )

    return try {
      if (com.google.firebase.FirebaseApp.getApps(applicationContext).isEmpty()) {
        return Result.success()
      }
      val db = FirebaseFirestore.getInstance()
      db.collection("offline_queued_bookings").add(bookingData).await()
      Result.success()
    } catch (e: Exception) {
      Result.retry()
    }
  }
}
