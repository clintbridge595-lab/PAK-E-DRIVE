package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.Booking
import kotlinx.coroutines.flow.Flow

@Dao
interface BookingDao {
  @Query("SELECT * FROM bookings ORDER BY createdAt DESC")
  fun getAllBookings(): Flow<List<Booking>>

  @Query("SELECT * FROM bookings WHERE id = :id LIMIT 1")
  suspend fun getBookingById(id: String): Booking?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBooking(booking: Booking)

  @Query("UPDATE bookings SET status = :status WHERE id = :id")
  suspend fun updateBookingStatus(id: String, status: String)

  @Query("DELETE FROM bookings WHERE id = :id")
  suspend fun deleteBooking(id: String)

  @Query("DELETE FROM bookings")
  suspend fun clearAllBookings()
}
