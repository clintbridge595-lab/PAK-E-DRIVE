package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.Review
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDao {
  @Query("SELECT * FROM reviews ORDER BY id DESC")
  fun getAllReviews(): Flow<List<Review>>

  @Query("SELECT * FROM reviews WHERE carId = :carId ORDER BY id DESC")
  fun getReviewsForCar(carId: String): Flow<List<Review>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertReview(review: Review)

  @Query("DELETE FROM reviews WHERE id = :id")
  suspend fun deleteReview(id: String)
}
