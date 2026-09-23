package com.example.data.repository

import android.content.Context
import android.os.Build
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.CrashLog
import com.example.data.local.CrashLogDao
import com.example.util.NetworkMonitor
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CrashLogRepository(private val context: Context) {
  private val crashLogDao: CrashLogDao = AppDatabase.getDatabase(context).crashLogDao()
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
  private val networkMonitor = NetworkMonitor(context)

  companion object {
    private const val TAG = "CrashLogRepository"

    @Volatile
    private var INSTANCE: CrashLogRepository? = null

    fun getInstance(context: Context): CrashLogRepository {
      return INSTANCE ?: synchronized(this) {
        INSTANCE ?: CrashLogRepository(context.applicationContext).also { INSTANCE = it }
      }
    }
  }

  init {
    // Monitor connectivity changes to trigger automatic sync when back online
    scope.launch {
      networkMonitor.isOnline.collect { isOnline ->
        if (isOnline) {
          Log.d(TAG, "Network connection restored. Syncing offline crash logs to Firestore...")
          syncOfflineLogsToFirestore()
        }
      }
    }
  }

  fun getAllCrashLogs(): Flow<List<CrashLog>> = crashLogDao.getAllCrashLogs()

  fun getUnsyncedCount(): Flow<Int> = crashLogDao.getUnsyncedCount()

  suspend fun logException(
    throwable: Throwable,
    tag: String = "AppException",
    severity: String = "CRITICAL",
    customMessage: String? = null
  ): Long = withContext(Dispatchers.IO) {
    val sw = StringWriter()
    throwable.printStackTrace(PrintWriter(sw))
    val stackTrace = sw.toString()
    val message = customMessage ?: throwable.localizedMessage ?: throwable.javaClass.simpleName

    val metadata = buildDeviceMetadata()
    val crashLog = CrashLog(
      tag = tag,
      message = message,
      stackTrace = stackTrace,
      timestamp = System.currentTimeMillis(),
      severity = severity,
      isSynced = false,
      metadata = metadata
    )

    val id = crashLogDao.insertCrashLog(crashLog)
    Log.w(TAG, "Recorded crash log #$id in local Room database (isSynced=false): $message")

    // If online, immediately try to sync
    if (networkMonitor.isCurrentlyOnline()) {
      scope.launch { syncOfflineLogsToFirestore() }
    }
    id
  }

  suspend fun logManualEvent(
    tag: String,
    message: String,
    stackTrace: String = "",
    severity: String = "ERROR"
  ): Long = withContext(Dispatchers.IO) {
    val metadata = buildDeviceMetadata()
    val crashLog = CrashLog(
      tag = tag,
      message = message,
      stackTrace = stackTrace.ifBlank { "Manual event triggered at ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}" },
      timestamp = System.currentTimeMillis(),
      severity = severity,
      isSynced = false,
      metadata = metadata
    )
    val id = crashLogDao.insertCrashLog(crashLog)
    Log.d(TAG, "Manual log event #$id recorded in Room: $message")
    if (networkMonitor.isCurrentlyOnline()) {
      scope.launch { syncOfflineLogsToFirestore() }
    }
    id
  }

  suspend fun syncOfflineLogsToFirestore(): Result<Int> = withContext(Dispatchers.IO) {
    val unsynced = crashLogDao.getUnsyncedCrashLogs()
    if (unsynced.isEmpty()) {
      Log.d(TAG, "No unsynced crash logs to upload.")
      return@withContext Result.success(0)
    }

    try {
      if (com.google.firebase.FirebaseApp.getApps(context).isEmpty()) {
        Log.d(TAG, "FirebaseApp not initialized, skipping remote sync.")
        return@withContext Result.success(0)
      }
      val firestore = FirebaseFirestore.getInstance()
      var syncedCount = 0

      for (log in unsynced) {
        val docData = hashMapOf(
          "localId" to log.id,
          "tag" to log.tag,
          "message" to log.message,
          "stackTrace" to log.stackTrace,
          "timestamp" to log.timestamp,
          "severity" to log.severity,
          "metadata" to log.metadata,
          "syncedAt" to System.currentTimeMillis(),
          "deviceModel" to "${Build.MANUFACTURER} ${Build.MODEL}",
          "osVersion" to "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
        )

        // Upload to 'crash_logs' collection in Firestore
        firestore.collection("crash_logs")
          .document("crash_${log.id}_${log.timestamp}")
          .set(docData, SetOptions.merge())
          .await()

        crashLogDao.markAsSynced(log.id)
        syncedCount++
        Log.i(TAG, "Crash log #${log.id} successfully synced to Firestore.")
      }

      Result.success(syncedCount)
    } catch (e: Exception) {
      Log.w(TAG, "Could not sync offline crash logs to Firestore (will retry when online): ${e.message}")
      Result.failure(e)
    }
  }

  suspend fun clearAll() = withContext(Dispatchers.IO) {
    crashLogDao.clearAllLogs()
  }

  private fun buildDeviceMetadata(): String {
    return "Device: ${Build.MANUFACTURER} ${Build.MODEL} | OS: Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT}) | App: 1.0.0.0"
  }
}
