package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "crash_logs")
data class CrashLog(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val tag: String,
  val message: String,
  val stackTrace: String,
  val timestamp: Long = System.currentTimeMillis(),
  val severity: String = "ERROR", // CRITICAL, ERROR, WARNING, INFO
  val isSynced: Boolean = false,
  val metadata: String = "" // device details, app version, network status
)
