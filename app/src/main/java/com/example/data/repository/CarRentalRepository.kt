package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.model.Booking
import com.example.data.model.Car
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CarRentalRepository(context: Context) {

  private val bookingDao = AppDatabase.getDatabase(context).bookingDao()
  private val prefs: SharedPreferences = context.getSharedPreferences("pakedrive_prefs", Context.MODE_PRIVATE)

  private val _userProfile = MutableStateFlow(loadProfile())
  val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

  val allBookings: Flow<List<Booking>> = bookingDao.getAllBookings()

  // Real Authentic Pakistani Rental Fleet & Wapsi Cars
  val fleet: List<Car> = listOf(
    Car(
      id = "wapsi_civic_15",
      name = "Civic",
      variant = "1.5 Turbo",
      make = "Honda",
      category = "Sedan",
      dailyRate = 8000,
      tenHourRate = 6500,
      priceDisplay = "Rs. 8000/day",
      rateType = "/day",
      routeSnippet = "Lahore to Lahore",
      fromCity = "Lahore",
      toCity = "Lahore",
      imageRes = R.drawable.car_civic_turbo,
      engineSpec = "1.5L VTEC Turbo",
      seats = 5,
      transmission = "Automatic",
      fuelType = "Petrol",
      isFeatured = true,
      description = "Sporty, comfortable Honda Civic RS Turbo with experienced chauffeur for local city and intercity travel."
    ),
    Car(
      id = "wapsi_corolla_16",
      name = "Corolla",
      variant = "1.6",
      make = "Toyota",
      category = "Sedan",
      dailyRate = 9000,
      tenHourRate = 7500,
      priceDisplay = "Rs. 9000/ lump sum",
      rateType = "lump sum",
      routeSnippet = "Karachi to Hyderabad",
      fromCity = "Karachi",
      toCity = "Hyderabad",
      imageRes = R.drawable.car_corolla_altis,
      engineSpec = "1.6L Dual VVT-i",
      seats = 5,
      transmission = "Automatic",
      fuelType = "Petrol",
      isFeatured = true,
      description = "Reliable Toyota Corolla 1.6 Altis. Ideal for Karachi to Hyderabad intercity travel."
    ),
    Car(
      id = "wapsi_corolla_sukkur",
      name = "Corolla",
      variant = "GLI",
      make = "Toyota",
      category = "Sedan",
      dailyRate = 20000,
      tenHourRate = 18000,
      priceDisplay = "Rs. 20000/ lump sum",
      rateType = "lump sum",
      routeSnippet = "Karachi to Sukkur",
      fromCity = "Karachi",
      toCity = "Sukkur",
      imageRes = R.drawable.img_car_corolla_white,
      engineSpec = "1.3L GLI",
      seats = 5,
      transmission = "Manual/Auto",
      fuelType = "Petrol",
      isFeatured = true,
      description = "Intercity one-way wapsi car deal from Karachi to Sukkur including highway tolls."
    ),
    Car(
      id = "wapsi_apv_nawabshah",
      name = "APV",
      variant = "APV GL",
      make = "Suzuki",
      category = "Van",
      dailyRate = 8000,
      tenHourRate = 6500,
      priceDisplay = "Rs. 8000/day",
      rateType = "/day",
      routeSnippet = "Nawabshah to Local",
      fromCity = "Nawabshah",
      toCity = "Local",
      imageRes = R.drawable.car_hiace_cabin,
      engineSpec = "1.5L G15A",
      seats = 8,
      transmission = "Manual",
      fuelType = "Petrol",
      isFeatured = true,
      description = "Spacious 8-seater family van for Nawabshah local city tours and family events."
    ),
    Car(
      id = "wapsi_apv_isb",
      name = "APV",
      variant = "APV GA",
      make = "Suzuki",
      category = "Van",
      dailyRate = 8000,
      tenHourRate = 6500,
      priceDisplay = "Rs. 8000/day",
      rateType = "/day",
      routeSnippet = "Islamabad to Local",
      fromCity = "Islamabad",
      toCity = "Local",
      imageRes = R.drawable.car_hiace_cabin,
      engineSpec = "1.5L Petrol",
      seats = 8,
      transmission = "Manual",
      fuelType = "Petrol",
      isFeatured = true,
      description = "Spacious passenger van for Islamabad & Rawalpindi local travel, weddings and group tours."
    ),
    Car(
      id = "wapsi_corolla_nwb",
      name = "Corolla",
      variant = "GLI",
      make = "Toyota",
      category = "Sedan",
      dailyRate = 5500,
      tenHourRate = 4500,
      priceDisplay = "Rs. 5500/day",
      rateType = "/day",
      routeSnippet = "Nawabshah to Local",
      fromCity = "Nawabshah",
      toCity = "Local",
      imageRes = R.drawable.car_corolla_altis,
      engineSpec = "1.3L Petrol",
      seats = 5,
      transmission = "Manual",
      fuelType = "Petrol",
      isFeatured = true,
      description = "Economical executive sedan for Nawabshah and interior Sindh travel."
    ),
    Car(
      id = "wapsi_karvaan_lahore",
      name = "Karvaan",
      variant = "Premium",
      make = "Changan",
      category = "Van",
      dailyRate = 9000,
      tenHourRate = 7500,
      priceDisplay = "Rs. 9000/day",
      rateType = "/day",
      routeSnippet = "Lahore to Faisalabad",
      fromCity = "Lahore",
      toCity = "Faisalabad",
      imageRes = R.drawable.car_oshan_x7,
      engineSpec = "1.2L BlueCore",
      seats = 7,
      transmission = "Manual",
      fuelType = "Petrol",
      isFeatured = true,
      description = "Dual AC family van with comfortable captain seats from Lahore to Faisalabad via Motorway M3."
    ),
    Car(
      id = "wapsi_cross_nwb",
      name = "Cross",
      variant = "Cross 1.8 Hybrid",
      make = "Toyota",
      category = "SUV",
      dailyRate = 12000,
      tenHourRate = 10000,
      priceDisplay = "Rs. 12000/ lump sum",
      rateType = "lump sum",
      routeSnippet = "Nawabshah to Karachi",
      fromCity = "Nawabshah",
      toCity = "Karachi",
      imageRes = R.drawable.img_car_fortuner_white,
      engineSpec = "1.8L Hybrid",
      seats = 5,
      transmission = "Automatic",
      fuelType = "Hybrid",
      isFeatured = true,
      description = "Modern luxury crossover SUV. Comfortable and highly fuel efficient from Nawabshah to Karachi."
    ),
    Car(
      id = "wapsi_alto_multan",
      name = "Alto",
      variant = "VXR",
      make = "Suzuki",
      category = "Hatchback",
      dailyRate = 10000,
      tenHourRate = 8000,
      priceDisplay = "Rs. 10000/ lump sum",
      rateType = "lump sum",
      routeSnippet = "Multan to Lahore",
      fromCity = "Multan",
      toCity = "Lahore",
      imageRes = R.drawable.img_car_yaris_studio,
      engineSpec = "660cc R06A",
      seats = 4,
      transmission = "Manual",
      fuelType = "Petrol",
      isFeatured = true,
      description = "Budget friendly one-way wapsi car ride from Multan to Lahore via Motorway M4."
    ),
    Car(
      id = "wapsi_civic_skt",
      name = "Civic",
      variant = "VTI",
      make = "Honda",
      category = "Sedan",
      dailyRate = 12000,
      tenHourRate = 10000,
      priceDisplay = "Rs. 12000/ lump sum",
      rateType = "lump sum",
      routeSnippet = "Sialkot to Islamabad",
      fromCity = "Sialkot",
      toCity = "Islamabad",
      imageRes = R.drawable.img_car_civic_white,
      engineSpec = "1.8L i-VTEC",
      seats = 5,
      transmission = "Automatic",
      fuelType = "Petrol",
      isFeatured = true,
      description = "Smooth executive ride from Sialkot Sambrial Airport / Cantt to Islamabad via Motorway."
    ),
    Car(
      id = "wapsi_wagonr_abbottabad",
      name = "Wagon R",
      variant = "VXR",
      make = "Suzuki",
      category = "Hatchback",
      dailyRate = 8000,
      tenHourRate = 6500,
      priceDisplay = "Rs. 8000/day",
      rateType = "/day",
      routeSnippet = "Abbottabad to Islamabad",
      fromCity = "Abbottabad",
      toCity = "Islamabad",
      imageRes = R.drawable.img_car_yaris_studio,
      engineSpec = "1.0L K-Series",
      seats = 5,
      transmission = "Manual",
      fuelType = "Petrol",
      isFeatured = true,
      description = "Reliable hatchback for Hazara expressway from Abbottabad to Islamabad Airport."
    ),
    Car(
      id = "car_fortuner_legender",
      name = "Fortuner",
      variant = "Legender 2.8",
      make = "Toyota",
      category = "SUV",
      dailyRate = 18000,
      tenHourRate = 15000,
      priceDisplay = "Rs. 18000/day",
      rateType = "/day",
      routeSnippet = "Islamabad to Murree",
      fromCity = "Islamabad",
      toCity = "Murree",
      imageRes = R.drawable.car_fortuner,
      engineSpec = "2.8L Turbo Diesel 4x4",
      seats = 7,
      transmission = "Automatic 4x4",
      fuelType = "Diesel",
      isFeatured = true,
      description = "Top of the line 4x4 SUV with chauffeur. Unmatched comfort for Northern Areas, Murree, and Swat."
    ),
    Car(
      id = "car_yaris_ativ",
      name = "Yaris",
      variant = "ATIV X 1.5",
      make = "Toyota",
      category = "Sedan",
      dailyRate = 7000,
      tenHourRate = 5800,
      priceDisplay = "Rs. 7000/day",
      rateType = "/day",
      routeSnippet = "Lahore to Gujranwala",
      fromCity = "Lahore",
      toCity = "Gujranwala",
      imageRes = R.drawable.img_car_yaris_studio,
      engineSpec = "1.5L Dual VVT-i",
      seats = 5,
      transmission = "CVT Automatic",
      fuelType = "Petrol",
      isFeatured = true,
      description = "Quiet and economical compact sedan for daily corporate commute and family visits."
    ),
    Car(
      id = "car_city_aspire",
      name = "City",
      variant = "1.5 Aspire",
      make = "Honda",
      category = "Sedan",
      dailyRate = 6000,
      tenHourRate = 5000,
      priceDisplay = "Rs. 6000/day",
      rateType = "/day",
      routeSnippet = "Karachi to Local",
      fromCity = "Karachi",
      toCity = "Local",
      imageRes = R.drawable.car_civic_turbo,
      engineSpec = "1.5L i-VTEC",
      seats = 5,
      transmission = "Prosmatec Automatic",
      fuelType = "Petrol",
      isFeatured = true,
      description = "Chilled AC and comfortable ride across Karachi Clifton, DHA, Gulshan, and Airport pick/drop."
    ),
    Car(
      id = "car_prado_tx",
      name = "Prado",
      variant = "TX 2.7",
      make = "Toyota",
      category = "SUV",
      dailyRate = 22000,
      tenHourRate = 19000,
      priceDisplay = "Rs. 22000/day",
      rateType = "/day",
      routeSnippet = "Peshawar to Islamabad",
      fromCity = "Peshawar",
      toCity = "Islamabad",
      imageRes = R.drawable.car_fortuner,
      engineSpec = "2.7L Petrol 4WD",
      seats = 7,
      transmission = "Automatic",
      fuelType = "Petrol",
      isFeatured = true,
      description = "Executive protocol SUV for VIP transfers between KP and Capital territory."
    ),
    Car(
      id = "car_oshan_x7",
      name = "Oshan X7",
      variant = "FutureSense",
      make = "Changan",
      category = "SUV",
      dailyRate = 14000,
      tenHourRate = 12000,
      priceDisplay = "Rs. 14000/day",
      rateType = "/day",
      routeSnippet = "Karachi to Gwadar",
      fromCity = "Karachi",
      toCity = "Gwadar",
      imageRes = R.drawable.car_oshan_x7,
      engineSpec = "1.5L Turbo BlueCore",
      seats = 7,
      transmission = "7-Speed DCT",
      fuelType = "Petrol",
      isFeatured = true,
      description = "Panoramic sunroof 7-seater crossover for Coastal Highway and Kund Malir tours."
    ),
    Car(
      id = "car_hiace_cabin",
      name = "HiAce",
      variant = "Grand Cabin",
      make = "Toyota",
      category = "Van",
      dailyRate = 16000,
      tenHourRate = 13500,
      priceDisplay = "Rs. 16000/day",
      rateType = "/day",
      routeSnippet = "Rawalpindi to Skardu",
      fromCity = "Rawalpindi",
      toCity = "Skardu",
      imageRes = R.drawable.car_hiace_cabin,
      engineSpec = "3.0L Diesel High Roof",
      seats = 14,
      transmission = "Automatic",
      fuelType = "Diesel",
      isFeatured = true,
      description = "14-passenger luxury executive tour van with dual blower AC and reclining seats."
    ),
    Car(
      id = "car_sportage_awd",
      name = "Sportage",
      variant = "AWD Alpha",
      make = "Kia",
      category = "SUV",
      dailyRate = 11000,
      tenHourRate = 9500,
      priceDisplay = "Rs. 11000/day",
      rateType = "/day",
      routeSnippet = "Lahore to Islamabad",
      fromCity = "Lahore",
      toCity = "Islamabad",
      imageRes = R.drawable.car_oshan_x7,
      engineSpec = "2.0L Nu MPI",
      seats = 5,
      transmission = "6-Speed Automatic",
      fuelType = "Petrol",
      isFeatured = true,
      description = "Comfortable motorway ride on M2 with cruise control and chilled climate AC."
    ),
    Car(
      id = "car_swift_glx",
      name = "Swift",
      variant = "GLX CVT",
      make = "Suzuki",
      category = "Hatchback",
      dailyRate = 6500,
      tenHourRate = 5200,
      priceDisplay = "Rs. 6500/day",
      rateType = "/day",
      routeSnippet = "Faisalabad to Lahore",
      fromCity = "Faisalabad",
      toCity = "Lahore",
      imageRes = R.drawable.img_car_yaris_studio,
      engineSpec = "1.2L K12M",
      seats = 5,
      transmission = "CVT Automatic",
      fuelType = "Petrol",
      isFeatured = true,
      description = "Zippy, stylish hatchback with alloy rims, push start, and high mileage."
    ),
    Car(
      id = "car_cultus_vxl",
      name = "Cultus",
      variant = "VXL",
      make = "Suzuki",
      category = "Hatchback",
      dailyRate = 5000,
      tenHourRate = 4200,
      priceDisplay = "Rs. 5000/day",
      rateType = "/day",
      routeSnippet = "Multan to Local",
      fromCity = "Multan",
      toCity = "Local",
      imageRes = R.drawable.img_car_yaris_studio,
      engineSpec = "1.0L K10B",
      seats = 5,
      transmission = "Manual",
      fuelType = "Petrol",
      isFeatured = true,
      description = "Affordable city runner for Multan Cantt, Gulgasht, and Bosan Road."
    ),
    Car(
      id = "car_audi_a6",
      name = "Audi A6",
      variant = "Wedding VIP",
      make = "Audi",
      category = "Luxury Wedding",
      dailyRate = 25000,
      tenHourRate = 22000,
      priceDisplay = "Rs. 25000/day",
      rateType = "/day",
      routeSnippet = "Karachi Wedding VIP",
      fromCity = "Karachi",
      toCity = "Karachi",
      imageRes = R.drawable.car_audi_wedding,
      engineSpec = "2.0L TFSI Turbo Quattro",
      seats = 5,
      transmission = "S-Tronic Automatic",
      fuelType = "Petrol",
      isFeatured = true,
      description = "VIP wedding car package with fresh floral decorations and suited chauffeur."
    ),
    Car(
      id = "car_landcruiser_zx",
      name = "Land Cruiser",
      variant = "ZX V8",
      make = "Toyota",
      category = "SUV",
      dailyRate = 35000,
      tenHourRate = 30000,
      priceDisplay = "Rs. 35000/day",
      rateType = "/day",
      routeSnippet = "Islamabad Protocol",
      fromCity = "Islamabad",
      toCity = "Islamabad",
      imageRes = R.drawable.img_car_fortuner_white,
      engineSpec = "4.6L V8 Dual VVT-i",
      seats = 7,
      transmission = "Automatic 4WD",
      fuelType = "Petrol",
      isFeatured = true,
      description = "Prestige V8 for diplomats, VIP delegations, and executive security protocol."
    ),
    Car(
      id = "car_mira_es",
      name = "Mira",
      variant = "ES Economy",
      make = "Daihatsu",
      category = "Hatchback",
      dailyRate = 4500,
      tenHourRate = 3800,
      priceDisplay = "Rs. 4500/day",
      rateType = "/day",
      routeSnippet = "Karachi to Local",
      fromCity = "Karachi",
      toCity = "Local",
      imageRes = R.drawable.img_car_yaris_studio,
      engineSpec = "660cc Eco Idle",
      seats = 4,
      transmission = "CVT Automatic",
      fuelType = "Petrol",
      isFeatured = true,
      description = "Super fuel efficient Japanese hatchback for Karachi traffic and daily errands."
    ),
    Car(
      id = "car_brv_ivtec",
      name = "BR-V",
      variant = "1.5 i-VTEC",
      make = "Honda",
      category = "SUV",
      dailyRate = 8500,
      tenHourRate = 7000,
      priceDisplay = "Rs. 8500/day",
      rateType = "/day",
      routeSnippet = "Lahore to Sialkot",
      fromCity = "Lahore",
      toCity = "Sialkot",
      imageRes = R.drawable.car_civic_turbo,
      engineSpec = "1.5L i-VTEC 7-Seater",
      seats = 7,
      transmission = "Automatic",
      fuelType = "Petrol",
      isFeatured = true,
      description = "7-seater family MPV/SUV with roof AC for comfortable motorway travel."
    )
  )

  val pakistaniCities = listOf(
    "All Cities",
    "Abbottabad",
    "Ahmadpur East",
    "Attock",
    "Astore",
    "Bahawalpur",
    "Bannu",
    "Burewala",
    "Chiniot",
    "Dadu",
    "Dera Ghazi Khan",
    "Dera Ismail Khan",
    "Faisalabad",
    "Gilgit",
    "Gujranwala",
    "Gujrat",
    "Gwadar",
    "Hafizabad",
    "Haripur",
    "Hyderabad",
    "Islamabad",
    "Jacobabad",
    "Jhang",
    "Jhelum",
    "Karachi",
    "Kasur",
    "Khanewal",
    "Kohat",
    "Lahore",
    "Larkana",
    "Mardan",
    "Mirpur Khas",
    "Multan",
    "Muzaffargarh",
    "Nawabshah",
    "Nowshera",
    "Okara",
    "Peshawar",
    "Quetta",
    "Rahim Yar Khan",
    "Rawalpindi",
    "Sahiwal",
    "Sargodha",
    "Sialkot",
    "Sukkur",
    "Swat",
    "Vehari",
    "Wah Cantonment"
  )

  suspend fun insertBooking(booking: Booking) {
    bookingDao.insertBooking(booking)
    // Update total trips count in profile
    val current = _userProfile.value
    val updated = current.copy(totalTrips = current.totalTrips + 1)
    saveProfile(updated)
  }

  suspend fun updateBookingStatus(id: String, status: String) {
    bookingDao.updateBookingStatus(id, status)
  }

  suspend fun cancelBooking(id: String) {
    bookingDao.updateBookingStatus(id, "Cancelled")
  }

  suspend fun deleteBooking(id: String) {
    bookingDao.deleteBooking(id)
  }

  fun updateProfile(name: String, phone: String, email: String, city: String, address: String) {
    val updated = _userProfile.value.copy(
      name = name,
      phone = phone,
      email = email,
      city = city,
      address = address,
      isLoggedIn = true
    )
    saveProfile(updated)
  }

  fun loginUser(phone: String, name: String) {
    val updated = _userProfile.value.copy(
      isLoggedIn = true,
      phone = phone,
      name = name.ifBlank { "Valued Customer" }
    )
    saveProfile(updated)
  }

  fun logout() {
    val updated = _userProfile.value.copy(isLoggedIn = false)
    saveProfile(updated)
  }

  suspend fun deleteAccount() {
    // Clear all bookings from database
    bookingDao.clearAllBookings()
    // Reset user profile to default logged-out guest
    val guest = UserProfile(
      isLoggedIn = false,
      name = "Guest User",
      phone = "",
      email = "",
      city = "Karachi",
      address = "",
      totalTrips = 0
    )
    saveProfile(guest)
  }

  private fun loadProfile(): UserProfile {
    val loggedIn = prefs.getBoolean("is_logged_in", true)
    val name = prefs.getString("name", "Mehdi Raza") ?: "Mehdi Raza"
    val phone = prefs.getString("phone", "+92 315 2292493") ?: "+92 315 2292493"
    val email = prefs.getString("email", "clintbridge595@gmail.com") ?: "clintbridge595@gmail.com"
    val city = prefs.getString("city", "Karachi") ?: "Karachi"
    val address = prefs.getString("address", "Korangi 5, Sector 35 F Model Park, Karachi") ?: "Korangi 5, Sector 35 F Model Park, Karachi"
    val trips = prefs.getInt("total_trips", 0)
    return UserProfile(loggedIn, name, phone, email, city, address, trips)
  }

  private fun saveProfile(profile: UserProfile) {
    prefs.edit()
      .putBoolean("is_logged_in", profile.isLoggedIn)
      .putString("name", profile.name)
      .putString("phone", profile.phone)
      .putString("email", profile.email)
      .putString("city", profile.city)
      .putString("address", profile.address)
      .putInt("total_trips", profile.totalTrips)
      .apply()
    _userProfile.value = profile
  }
}
