package com.example.data.model

data class UserProfile(
  val isLoggedIn: Boolean = true,
  val name: String = "Mehdi Raza",
  val phone: String = "+92 315 2292493",
  val email: String = "clintbridge595@gmail.com",
  val city: String = "Karachi",
  val address: String = "Korangi 5, Sector 35 F Model Park, Karachi",
  val totalTrips: Int = 0
)
