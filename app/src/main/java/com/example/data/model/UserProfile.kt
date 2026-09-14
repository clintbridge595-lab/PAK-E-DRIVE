package com.example.data.model

data class UserProfile(
  val isLoggedIn: Boolean = false,
  val accountType: String = "CLIENT", // "CLIENT" or "DRIVER"
  val name: String = "",
  val phone: String = "",
  val email: String = "",
  val cnic: String = "", // Pakistani CNIC e.g. 42101-1234567-1 (Required by law)
  val isNadraVerified: Boolean = false,
  val nadraVerificationStatus: String = "UNVERIFIED",
  val city: String = "Karachi",
  val address: String = "",
  val totalTrips: Int = 0,
  val isDriverPartner: Boolean = false,
  val driverLicenseNumber: String = "",
  val isLicenseVerified: Boolean = false,
  val licenseIssuingAuthority: String = ""
)
