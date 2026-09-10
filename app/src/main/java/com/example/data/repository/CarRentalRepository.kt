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
  private val reviewDao = AppDatabase.getDatabase(context).reviewDao()
  private val prefs: SharedPreferences = context.getSharedPreferences("pakedrive_prefs", Context.MODE_PRIVATE)

  private val _userProfile = MutableStateFlow(loadProfile())
  val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

  val allBookings: Flow<List<Booking>> = bookingDao.getAllBookings()
  val allReviews: Flow<List<com.example.data.model.Review>> = reviewDao.getAllReviews()

  // Real Authentic Pakistani Rental Fleet - Each vehicle has its own distinct photo
  // Real Authentic Pakistani Rental Fleet - Client Verified Pricing & Lineup
  val fleet: List<Car> = listOf(
    // 1. CLIENT'S SPECIAL HIGHLIGHT FLAGSHIP: Glossy Black Changan Oshan X7 300T FutureSense
    Car(
      id = "car_oshan_x7_black",
      name = "Changan Oshan X7",
      variant = "FutureSense 300T (Black Edition)",
      make = "Changan",
      category = "SUV",
      dailyRate = 16000,
      tenHourRate = 14000,
      priceDisplay = "Rs. 14,000 / 10h",
      rateType = "/ 10h",
      routeSnippet = "Karachi • Pindi • Lahore VIP",
      fromCity = "Rawalpindi",
      toCity = "Lahore",
      imageRes = R.drawable.car_changan_x7_black,
      engineSpec = "1.5L Turbo BlueCore 300T",
      seats = 7,
      transmission = "7-Speed Wet DCT",
      fuelType = "Petrol",
      rating = 5.0,
      reviewCount = 48,
      isFeatured = true,
      description = "Flagship Black Changan Oshan X7 300T with cascading chrome waterfall grille, full panoramic skyroof, ventilated leather seats, and executive motorway protocol chauffeur."
    ),

    // 2. Toyota Corolla Altis (Client Rate: 10 Hours Rs. 8,000/=)
    Car(
      id = "wapsi_corolla_16",
      name = "Corolla",
      variant = "Altis Grande 1.6",
      make = "Toyota",
      category = "Sedan",
      dailyRate = 8000,
      tenHourRate = 8000,
      priceDisplay = "Rs. 8,000 / 10h",
      rateType = "/ 10h",
      routeSnippet = "Pindi • Lahore • Karachi",
      fromCity = "Rawalpindi",
      toCity = "Lahore",
      imageRes = R.drawable.car_corolla_altis,
      engineSpec = "1.6L Dual VVT-i",
      seats = 5,
      transmission = "Automatic",
      fuelType = "Petrol",
      rating = 4.9,
      reviewCount = 56,
      isFeatured = true,
      description = "Toyota Corolla Altis Grande. 10-hour client rate Rs. 8,000/= with chilled AC and motorway experienced chauffeur."
    ),

    // 3. Toyota Hilux Revo Dala (Client Rate: 10 Hours Rs. 10,000/=)
    Car(
      id = "car_hilux_revo",
      name = "Hilux Revo Dala",
      variant = "Rocco 2.8 4x4 Double Cabin",
      make = "Toyota",
      category = "SUV",
      dailyRate = 10000,
      tenHourRate = 10000,
      priceDisplay = "Rs. 10,000 / 10h",
      rateType = "/ 10h",
      routeSnippet = "Karachi City & Escort",
      fromCity = "Karachi",
      toCity = "Karachi",
      imageRes = R.drawable.car_revo_b6,
      engineSpec = "2.8L 1GD-FTV Turbo Diesel",
      seats = 5,
      transmission = "Automatic 4x4",
      fuelType = "Diesel",
      rating = 4.9,
      reviewCount = 41,
      isFeatured = true,
      description = "Heavy-duty Toyota Hilux Revo Double Cabin Dala. 10-hour rate Rs. 10,000/= for Karachi city protocol and field escort."
    ),

    // 4. Suzuki Alto VXR (Client Rate: 10 Hours Rs. 4,000/=)
    Car(
      id = "wapsi_alto_multan",
      name = "Alto",
      variant = "VXR 660cc Fuel Saver",
      make = "Suzuki",
      category = "Hatchback",
      dailyRate = 4000,
      tenHourRate = 4000,
      priceDisplay = "Rs. 4,000 / 10h",
      rateType = "/ 10h",
      routeSnippet = "Local City Errands",
      fromCity = "Rawalpindi",
      toCity = "Islamabad",
      imageRes = R.drawable.car_suzuki_alto,
      engineSpec = "660cc R06A Engine",
      seats = 4,
      transmission = "Manual",
      fuelType = "Petrol",
      rating = 4.8,
      reviewCount = 63,
      isFeatured = true,
      description = "Economical Suzuki Alto. Client rate Rs. 4,000/= for 10 hours of comfortable local city travel with AC."
    ),

    // 5. Toyota Fortuner (Client Rate: 10 Hours Rs. 25,000/=)
    Car(
      id = "car_fortuner_legender",
      name = "Fortuner",
      variant = "Legender 2.8 4x4",
      make = "Toyota",
      category = "SUV",
      dailyRate = 25000,
      tenHourRate = 25000,
      priceDisplay = "Rs. 25,000 / 10h",
      rateType = "/ 10h",
      routeSnippet = "Islamabad • Murree • VIP Escort",
      fromCity = "Islamabad",
      toCity = "Murree",
      imageRes = R.drawable.car_fortuner,
      engineSpec = "2.8L 1GD Turbo Diesel 4x4",
      seats = 7,
      transmission = "Automatic 4x4",
      fuelType = "Diesel",
      rating = 5.0,
      reviewCount = 44,
      isFeatured = true,
      description = "Toyota Fortuner Legender 4x4. Client rate Rs. 25,000/= for 10 hours. High ground clearance and VIP status."
    ),

    // 6. Toyota Land Cruiser V8 (Client Rate: 10 Hours Rs. 28,000/=)
    Car(
      id = "car_landcruiser_v8",
      name = "Land Cruiser V8",
      variant = "ZX V8 4.6L Executive",
      make = "Toyota",
      category = "SUV",
      dailyRate = 28000,
      tenHourRate = 28000,
      priceDisplay = "Rs. 28,000 / 10h",
      rateType = "/ 10h",
      routeSnippet = "Islamabad • Peshawar VIP Protocol",
      fromCity = "Islamabad",
      toCity = "Peshawar",
      imageRes = R.drawable.car_landcruiser_v8,
      engineSpec = "4.6L V8 Dual VVT-i",
      seats = 7,
      transmission = "Automatic 4WD",
      fuelType = "Petrol",
      rating = 5.0,
      reviewCount = 37,
      isFeatured = true,
      description = "Presidency & VIP Protocol Toyota Land Cruiser V8. Client rate Rs. 28,000/= for 10 hours with verified chauffeur."
    ),

    // 7. Honda BR-V (Requested by Client: 1 BRV)
    Car(
      id = "car_honda_brv",
      name = "Honda BR-V",
      variant = "i-VTEC S 7-Seater",
      make = "Honda",
      category = "SUV",
      dailyRate = 10000,
      tenHourRate = 9000,
      priceDisplay = "Rs. 9,000 / 10h",
      rateType = "/ 10h",
      routeSnippet = "Pindi • Lahore • Faisalabad",
      fromCity = "Rawalpindi",
      toCity = "Lahore",
      imageRes = R.drawable.car_honda_brv,
      engineSpec = "1.5L i-VTEC",
      seats = 7,
      transmission = "CVT Automatic",
      fuelType = "Petrol",
      rating = 4.8,
      reviewCount = 29,
      isFeatured = true,
      description = "Spacious 7-seater Honda BR-V. Versatile family crossover for intercity tours from Rawalpindi to Northern Areas."
    ),

    // 8. Toyota HiAce 224 Grand Cabin (Client: 224 G Cabin pindi say)
    Car(
      id = "car_hiace_cabin",
      name = "HiAce 224 Grand Cabin",
      variant = "224 G Cabin 14-Seat High Roof",
      make = "Toyota",
      category = "Van",
      dailyRate = 18000,
      tenHourRate = 16000,
      priceDisplay = "Rs. 16,000 / 10h",
      rateType = "/ 10h",
      routeSnippet = "Pindi to Lahore 38k / Karachi 110k",
      fromCity = "Rawalpindi",
      toCity = "Lahore",
      imageRes = R.drawable.car_hiace_cabin,
      engineSpec = "3.0L Turbo Diesel High Roof",
      seats = 14,
      transmission = "Automatic",
      fuelType = "Diesel",
      rating = 5.0,
      reviewCount = 59,
      isFeatured = true,
      description = "14-passenger luxury 224 Grand Cabin with dual blower AC and high roof comfort for long-distance family tours."
    ),

    // 9. Changan Karvaan Plus (Client: Changan pindi say)
    Car(
      id = "wapsi_karvaan_lahore",
      name = "Changan Karvaan Plus",
      variant = "Plus Dual AC 7-Seat",
      make = "Changan",
      category = "Van",
      dailyRate = 9000,
      tenHourRate = 7500,
      priceDisplay = "Rs. 7,500 / 10h",
      rateType = "/ 10h",
      routeSnippet = "Pindi to Lahore 20k / Karachi 80k",
      fromCity = "Rawalpindi",
      toCity = "Lahore",
      imageRes = R.drawable.car_changan_karvaan,
      engineSpec = "1.2L BlueCore",
      seats = 7,
      transmission = "Manual",
      fuelType = "Petrol",
      rating = 4.7,
      reviewCount = 25,
      isFeatured = true,
      description = "7-seater dual AC Changan Karvaan for family travel between Rawalpindi, Lahore, Faisalabad, and Multan."
    ),

    // ================= CERTIFIED BULLET PROOF B6+ FLEET =================
    // 10. BULLET PROOF RIVO B6+
    Car(
      id = "bp_revo_b6",
      name = "BULLET PROOF RIVO B6+",
      variant = "Armored B6+ Double Cabin",
      make = "Toyota",
      category = "Bullet Proof B6+",
      dailyRate = 55000,
      tenHourRate = 45000,
      priceDisplay = "Contact for B6+",
      rateType = "B6+ Certified",
      routeSnippet = "Security Escort & Protocol",
      fromCity = "Rawalpindi",
      toCity = "Islamabad",
      imageRes = R.drawable.car_revo_b6,
      engineSpec = "2.8L Turbo Diesel Armored",
      seats = 5,
      transmission = "Automatic 4x4",
      fuelType = "Diesel",
      rating = 5.0,
      reviewCount = 19,
      isFeatured = true,
      description = "Certified CEN Level B6+ Ballistic Armored Toyota Hilux Revo double cabin. Multi-hit 7.62x51mm NATO protection, blast-resistant floor, and run-flat tyres."
    ),

    // 11. BULLET PROOF FORTUNER B6+
    Car(
      id = "bp_fortuner_b6",
      name = "BULLET PROOF FORTUNER B6+",
      variant = "Armored B6+ VIP Escort",
      make = "Toyota",
      category = "Bullet Proof B6+",
      dailyRate = 65000,
      tenHourRate = 25000,
      priceDisplay = "Contact for B6+",
      rateType = "B6+ Certified",
      routeSnippet = "VIP Protocol Convoy",
      fromCity = "Islamabad",
      toCity = "Peshawar",
      imageRes = R.drawable.car_fortuner,
      engineSpec = "2.8L Turbo Diesel 4x4 Armored",
      seats = 7,
      transmission = "Automatic 4x4",
      fuelType = "Diesel",
      rating = 5.0,
      reviewCount = 22,
      isFeatured = true,
      description = "High-status CEN B6+ Ballistic Armored Toyota Fortuner. Reinforced suspension, ballistic steel overlapping doors, and anti-ambush trained chauffeur."
    ),

    // 12. BULLET PROOF PRADO B6+
    Car(
      id = "bp_prado_b6",
      name = "BULLET PROOF PRADO B6+",
      variant = "Armored B6+ Executive",
      make = "Toyota",
      category = "Bullet Proof B6+",
      dailyRate = 75000,
      tenHourRate = 60000,
      priceDisplay = "Contact for B6+",
      rateType = "B6+ Certified",
      routeSnippet = "Diplomatic & VIP Movement",
      fromCity = "Rawalpindi",
      toCity = "Islamabad",
      imageRes = R.drawable.car_toyota_prado,
      engineSpec = "4.0L V6 Petrol Armored 4WD",
      seats = 7,
      transmission = "Automatic",
      fuelType = "Petrol",
      rating = 5.0,
      reviewCount = 17,
      isFeatured = true,
      description = "Executive Bullet Proof Toyota Prado B6+. Heavy-duty ballistic glass, battery & fuel tank armored shielding, and emergency siren system."
    ),

    // 13. BULLET PROOF LANDCRUISER V8 B6+
    Car(
      id = "bp_landcruiser_b6",
      name = "BULLET PROOF LANDCRUISER V8 B6+",
      variant = "Armored B6+ Presidential Fortress",
      make = "Toyota",
      category = "Bullet Proof B6+",
      dailyRate = 95000,
      tenHourRate = 28000,
      priceDisplay = "Contact for B6+",
      rateType = "B6+ Certified",
      routeSnippet = "Presidency & VIP Movement",
      fromCity = "Islamabad",
      toCity = "Lahore",
      imageRes = R.drawable.car_landcruiser_v8,
      engineSpec = "4.6L V8 Twin Armored Shielding",
      seats = 7,
      transmission = "Automatic 4WD",
      fuelType = "Petrol",
      rating = 5.0,
      reviewCount = 33,
      isFeatured = true,
      description = "Diplomatic & Presidential Standard CEN B6+ Armored Land Cruiser V8. Complete perimeter ballistic protection, armored radiator, and run-flat system."
    ),

    // 14. Honda Civic RS Turbo
    Car(
      id = "wapsi_civic_15",
      name = "Civic",
      variant = "1.5 RS Turbo",
      make = "Honda",
      category = "Sedan",
      dailyRate = 8000,
      tenHourRate = 6500,
      priceDisplay = "Rs. 8,000 / 10h",
      rateType = "/ 10h",
      routeSnippet = "Lahore to Islamabad",
      fromCity = "Lahore",
      toCity = "Islamabad",
      imageRes = R.drawable.car_civic_turbo,
      engineSpec = "1.5L VTEC Turbo",
      seats = 5,
      transmission = "Automatic",
      fuelType = "Petrol",
      rating = 4.9,
      reviewCount = 28,
      isFeatured = true,
      description = "Sporty, comfortable Honda Civic RS Turbo with experienced motorway chauffeur for local city and intercity travel."
    ),

    // 15. Toyota Prado TX
    Car(
      id = "car_prado_tx",
      name = "Prado",
      variant = "TX 2.7 4WD",
      make = "Toyota",
      category = "SUV",
      dailyRate = 22000,
      tenHourRate = 19000,
      priceDisplay = "Rs. 19,000 / 10h",
      rateType = "/ 10h",
      routeSnippet = "Peshawar to Islamabad",
      fromCity = "Peshawar",
      toCity = "Islamabad",
      imageRes = R.drawable.car_toyota_prado,
      engineSpec = "2.7L Petrol 4WD",
      seats = 7,
      transmission = "Automatic",
      fuelType = "Petrol",
      rating = 4.9,
      reviewCount = 19,
      isFeatured = true,
      description = "Executive protocol SUV for VIP transfers between KP and Capital territory with verified senior chauffeur."
    ),

    // 16. Audi A6 Wedding VIP
    Car(
      id = "car_audi_a6",
      name = "Audi A6",
      variant = "Wedding VIP Quattro",
      make = "Audi",
      category = "Luxury Wedding",
      dailyRate = 25000,
      tenHourRate = 22000,
      priceDisplay = "Rs. 22,000 / 10h",
      rateType = "/ 10h",
      routeSnippet = "Karachi Wedding VIP",
      fromCity = "Karachi",
      toCity = "Karachi",
      imageRes = R.drawable.car_audi_wedding,
      engineSpec = "2.0L TFSI Turbo Quattro",
      seats = 5,
      transmission = "S-Tronic Automatic",
      fuelType = "Petrol",
      rating = 5.0,
      reviewCount = 16,
      isFeatured = true,
      description = "VIP wedding car package with fresh floral decorations and suited chauffeur for Barat and Walima."
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

  suspend fun insertReview(review: com.example.data.model.Review) {
    reviewDao.insertReview(review)
  }

  fun getReviewsForCar(carId: String): Flow<List<com.example.data.model.Review>> {
    return reviewDao.getReviewsForCar(carId)
  }

  fun getSavedLanguage(): com.example.util.AppLanguage {
    val code = prefs.getString("app_language", "en") ?: "en"
    return if (code == "ur") com.example.util.AppLanguage.URDU else com.example.util.AppLanguage.ENGLISH
  }

  fun saveLanguage(language: com.example.util.AppLanguage) {
    prefs.edit().putString("app_language", language.code).apply()
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
