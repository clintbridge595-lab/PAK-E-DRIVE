package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookings")
data class Booking(
  @PrimaryKey
  val id: String,
  val carId: String,
  val carName: String,
  val carCategory: String,
  val carImageRes: Int,
  val tripType: String, // "Local Karachi", "Intercity Tour", "Airport Transfer", "Wedding Special"
  val pickupCity: String,
  val pickupAddress: String,
  val dropCity: String,
  val dropAddress: String,
  val dateText: String,
  val timeText: String,
  val rentalDurationText: String,
  val withDriver: Boolean,
  val totalEstimatedPrice: Int,
  val customerName: String,
  val customerPhone: String,
  val status: String, // "Requested", "Confirmed", "Driver Assigned", "On the Way", "Completed", "Cancelled"
  val driverName: String = "Muhammad Aslam",
  val driverPhone: String = "+92 315 2292493",
  val vehiclePlateNumber: String = "PED-786",
  val createdAt: Long = System.currentTimeMillis()
)
