package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reviews")
data class Review(
  @PrimaryKey
  val id: String,
  val carId: String,
  val carName: String,
  val bookingId: String,
  val userName: String,
  val userCity: String,
  val carRating: Float, // 1 to 5 stars
  val driverRating: Float, // 1 to 5 stars
  val overallRating: Float, // average
  val comment: String,
  val date: String,
  val verifiedRental: Boolean = true
)
