package com.example.data.model

data class Car(
  val id: String,
  val name: String,
  val variant: String = "",
  val make: String = "Toyota",
  val category: String = "Sedan", // Sedan, SUV, Hatchback, Van, Luxury Wedding
  val dailyRate: Int = 8000,
  val tenHourRate: Int = 6500,
  val priceDisplay: String = "Rs. 8000/day",
  val rateType: String = "/day",
  val routeSnippet: String = "Karachi to Local",
  val fromCity: String = "Karachi",
  val toCity: String = "Local",
  val imageRes: Int,
  val engineSpec: String = "1.8L",
  val seats: Int = 5,
  val transmission: String = "Automatic",
  val fuelType: String = "Petrol",
  val serviceType: String = "With Driver",
  val isFeatured: Boolean = true,
  val rating: Double = 4.9,
  val reviewCount: Int = 14,
  val description: String = "",
  val features: List<String> = listOf("Chauffeur Driven", "Full Chill AC", "Clean Interior", "Toll Tax Support"),
  val isPartnerCar: Boolean = false,
  val partnerDriverName: String = "",
  val partnerCnic: String = "",
  val partnerPhone: String = ""
)
