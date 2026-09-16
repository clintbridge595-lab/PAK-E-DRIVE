package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CrashLogDao {
  @Query("SELECT * FROM crash_logs ORDER BY timestamp DESC")
  fun getAllCrashLogs(): Flow<List<CrashLog>>

  @Query("SELECT * FROM crash_logs WHERE isSynced = 0 ORDER BY timestamp ASC")
  suspend fun getUnsyncedCrashLogs(): List<CrashLog>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCrashLog(crashLog: CrashLog): Long

  @Query("UPDATE crash_logs SET isSynced = 1 WHERE id = :id")
  suspend fun markAsSynced(id: Long)

  @Query("DELETE FROM crash_logs WHERE isSynced = 1")
  suspend fun deleteSyncedLogs()

  @Query("DELETE FROM crash_logs")
  suspend fun clearAllLogs()

  @Query("SELECT COUNT(*) FROM crash_logs WHERE isSynced = 0")
  fun getUnsyncedCount(): Flow<Int>
}
