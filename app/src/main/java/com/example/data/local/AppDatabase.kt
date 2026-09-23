package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.Booking
import com.example.data.model.Review

@Database(entities = [Booking::class, Review::class, CrashLog::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
  abstract fun bookingDao(): BookingDao
  abstract fun reviewDao(): ReviewDao
  abstract fun crashLogDao(): CrashLogDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "pakedrive_database"
        ).fallbackToDestructiveMigration(dropAllTables = true).build()
        INSTANCE = instance
        instance
      }
    }
  }
}
