package com.example.data.model

/**
 * Verified production pricing data model for PAK E DRIVE fleet.
 * Defines standard 10-hour rates, daily rates, and intercity lump sum packages.
 */
data class VehicleCategoryPricing(
  val modelName: String,
  val category: String,
  val tenHourRate: Int,
  val dailyRate: Int,
  val defaultRateDisplay: String,
  val defaultVariant: String,
  val defaultRoute: String
)

object FleetPricingRegistry {
  val ALTO = VehicleCategoryPricing(
    modelName = "Alto",
    category = "Hatchback",
    tenHourRate = 4000,
    dailyRate = 5000,
    defaultRateDisplay = "Rs. 4000/ 10h",
    defaultVariant = "VXR",
    defaultRoute = "Multan to Lahore"
  )

  val COROLLA = VehicleCategoryPricing(
    modelName = "Corolla",
    category = "Sedan",
    tenHourRate = 8000,
    dailyRate = 8000,
    defaultRateDisplay = "Rs. 8000/ 10h",
    defaultVariant = "1.6",
    defaultRoute = "Karachi to Hyderabad"
  )

  val REVO = VehicleCategoryPricing(
    modelName = "Hilux Revo",
    category = "SUV",
    tenHourRate = 10000,
    dailyRate = 12000,
    defaultRateDisplay = "Rs. 10000/ 10h",
    defaultVariant = "Double Cabin",
    defaultRoute = "Karachi to Local"
  )

  val FORTUNER = VehicleCategoryPricing(
    modelName = "Fortuner",
    category = "SUV",
    tenHourRate = 25000,
    dailyRate = 28000,
    defaultRateDisplay = "Rs. 25000/ 10h",
    defaultVariant = "Legender",
    defaultRoute = "Islamabad to Local"
  )

  val LANDCRUISER = VehicleCategoryPricing(
    modelName = "Land Cruiser",
    category = "SUV",
    tenHourRate = 28000,
    dailyRate = 32000,
    defaultRateDisplay = "Rs. 28000/ 10h",
    defaultVariant = "ZX V8",
    defaultRoute = "Islamabad to Peshawar"
  )

  val OSHAN_X7 = VehicleCategoryPricing(
    modelName = "Changan Oshan X7",
    category = "SUV",
    tenHourRate = 14000,
    dailyRate = 16000,
    defaultRateDisplay = "Rs. 14000/ 10h",
    defaultVariant = "FutureSense 300T",
    defaultRoute = "Rawalpindi to Lahore"
  )

  val CIVIC = VehicleCategoryPricing(
    modelName = "Civic",
    category = "Sedan",
    tenHourRate = 8000,
    dailyRate = 8000,
    defaultRateDisplay = "Rs. 8000/day",
    defaultVariant = "1.5 Turbo",
    defaultRoute = "Lahore to Lahore"
  )

  val KARVAAN = VehicleCategoryPricing(
    modelName = "Karvaan",
    category = "Van",
    tenHourRate = 7500,
    dailyRate = 9000,
    defaultRateDisplay = "Rs. 9000/day",
    defaultVariant = "Premium",
    defaultRoute = "Lahore to Faisalabad"
  )

  val APV = VehicleCategoryPricing(
    modelName = "APV",
    category = "Van",
    tenHourRate = 7000,
    dailyRate = 8000,
    defaultRateDisplay = "Rs. 8000/day",
    defaultVariant = "APV GL",
    defaultRoute = "Nawabshah to Local"
  )

  val CROSS = VehicleCategoryPricing(
    modelName = "Cross",
    category = "SUV",
    tenHourRate = 10000,
    dailyRate = 12000,
    defaultRateDisplay = "Rs. 12000/ lump sum",
    defaultVariant = "Cross 1.8 H...",
    defaultRoute = "Nawabshah to Karachi"
  )

  // B6+ Bulletproof Fleet Certified Pricing
  val BP_REVO_B6 = VehicleCategoryPricing(
    modelName = "BULLET PROOF RIVO B6+",
    category = "Bullet Proof B6+",
    tenHourRate = 10000,
    dailyRate = 55000,
    defaultRateDisplay = "Rs. 10000/ 10h",
    defaultVariant = "Armored B6+ Double Cabin",
    defaultRoute = "Rawalpindi to Islamabad"
  )

  val BP_FORTUNER_B6 = VehicleCategoryPricing(
    modelName = "BULLET PROOF FORTUNER B6+",
    category = "Bullet Proof B6+",
    tenHourRate = 25000,
    dailyRate = 65000,
    defaultRateDisplay = "Rs. 25000/ 10h",
    defaultVariant = "Armored B6+ VIP Escort",
    defaultRoute = "Islamabad to Peshawar"
  )

  val BP_PRADO_B6 = VehicleCategoryPricing(
    modelName = "BULLET PROOF PRADO B6+",
    category = "Bullet Proof B6+",
    tenHourRate = 60000,
    dailyRate = 75000,
    defaultRateDisplay = "Rs. 60000/ 10h",
    defaultVariant = "Armored B6+ Executive",
    defaultRoute = "Rawalpindi to Islamabad"
  )

  val BP_LANDCRUISER_B6 = VehicleCategoryPricing(
    modelName = "BULLET PROOF LANDCRUISER V8 B6+",
    category = "Bullet Proof B6+",
    tenHourRate = 28000,
    dailyRate = 95000,
    defaultRateDisplay = "Rs. 28000/ 10h",
    defaultVariant = "Armored B6+ Presidential",
    defaultRoute = "Islamabad to Lahore"
  )
}
