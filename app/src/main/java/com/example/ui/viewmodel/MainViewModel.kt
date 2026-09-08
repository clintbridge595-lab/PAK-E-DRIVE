package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Booking
import com.example.data.model.Car
import com.example.data.model.UserProfile
import com.example.data.repository.CarRentalRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class ChatMessage(
  val id: String = UUID.randomUUID().toString(),
  val sender: String, // "user" or "agent"
  val text: String,
  val timestamp: String = "Just now",
  val isUrgent: Boolean = false
)

data class AppNotification(
  val id: String = UUID.randomUUID().toString(),
  val title: String,
  val message: String,
  val time: String,
  val isRead: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

  private val repository = CarRentalRepository(application)

  val fleet: List<Car> = repository.fleet
  val cities: List<String> = repository.pakistaniCities

  val userProfile: StateFlow<UserProfile> = repository.userProfile
  val bookings: StateFlow<List<Booking>> = repository.allBookings
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  private val _selectedTab = MutableStateFlow(0)
  val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

  private val _selectedCity = MutableStateFlow("All Cities")
  val selectedCity: StateFlow<String> = _selectedCity.asStateFlow()

  private val _selectedCategory = MutableStateFlow("All")
  val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  private val _detailCar = MutableStateFlow<Car?>(null)
  val detailCar: StateFlow<Car?> = _detailCar.asStateFlow()

  private val _bookingCar = MutableStateFlow<Car?>(null)
  val bookingCar: StateFlow<Car?> = _bookingCar.asStateFlow()

  private val _confirmedBooking = MutableStateFlow<Booking?>(null)
  val confirmedBooking: StateFlow<Booking?> = _confirmedBooking.asStateFlow()

  private val _showAuthDialog = MutableStateFlow(false)
  val showAuthDialog: StateFlow<Boolean> = _showAuthDialog.asStateFlow()

  private val _showNotifications = MutableStateFlow(false)
  val showNotifications: StateFlow<Boolean> = _showNotifications.asStateFlow()

  private val _notifications = MutableStateFlow<List<AppNotification>>(
    listOf(
      AppNotification(
        title = "Welcome to Hat Cab",
        message = "Karachi, Lahore & Islamabad intercity & local bookings now live with chauffeur service.",
        time = "10 mins ago"
      ),
      AppNotification(
        title = "Fleet Special Offer",
        message = "Get 10% fuel concession on Toyota Corolla Altis Grande for Karachi-Hyderabad tour.",
        time = "2 hours ago"
      )
    )
  )
  val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

  private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
    listOf(
      ChatMessage(
        sender = "agent",
        text = "Assalam-o-Alaikum! Welcome to Hat Cab Official Assistance.\n\nLooking for a Civic RS Turbo, Altis Grande, Fortuner Legender or HiAce Grand Cabin? Ask about rates, chauffeur policies, or intercity routes!",
        timestamp = "Online"
      )
    )
  )
  val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

  init {
    // Seed initial booking if empty
    viewModelScope.launch {
      delay(300)
      if (bookings.value.isEmpty()) {
        val sampleBooking = Booking(
          id = "PED-78601",
          carId = "car_corolla_altis",
          carName = "Toyota Corolla Altis Grande",
          carCategory = "Sedan",
          carImageRes = com.example.R.drawable.car_corolla_altis,
          tripType = "Local Karachi (10 Hours)",
          pickupCity = "Karachi",
          pickupAddress = "Clifton Block 4, Karachi",
          dropCity = "Karachi",
          dropAddress = "Jinnah International Airport, Karachi",
          dateText = "Tomorrow",
          timeText = "10:00 AM",
          rentalDurationText = "1 Day (10 Hours)",
          withDriver = true,
          totalEstimatedPrice = 6500,
          customerName = userProfile.value.name,
          customerPhone = userProfile.value.phone,
          status = "Driver Assigned",
          driverName = "Muhammad Aslam",
          driverPhone = "+92 315 2292493",
          vehiclePlateNumber = "BLF-256 (Sindh)"
        )
        repository.insertBooking(sampleBooking)
      }
    }
  }

  fun setTab(index: Int) {
    _selectedTab.value = index
  }

  fun selectCity(city: String) {
    _selectedCity.value = city
  }

  fun selectCategory(category: String) {
    _selectedCategory.value = category
  }

  fun setSearchQuery(query: String) {
    _searchQuery.value = query
  }

  fun openCarDetail(car: Car) {
    _detailCar.value = car
  }

  fun closeCarDetail() {
    _detailCar.value = null
  }

  fun startBookingFlow(car: Car) {
    _bookingCar.value = car
  }

  fun closeBookingFlow() {
    _bookingCar.value = null
  }

  fun completeBooking(booking: Booking) {
    viewModelScope.launch {
      repository.insertBooking(booking)
      _bookingCar.value = null
      _confirmedBooking.value = booking
      // Add notification
      val newNotif = AppNotification(
        title = "Booking Confirmed #${booking.id}",
        message = "${booking.carName} booked for ${booking.pickupCity}. Chauffeur will contact you shortly.",
        time = "Just now"
      )
      _notifications.value = listOf(newNotif) + _notifications.value
    }
  }

  fun closeConfirmedModal() {
    _confirmedBooking.value = null
  }

  fun cancelBooking(bookingId: String) {
    viewModelScope.launch {
      repository.cancelBooking(bookingId)
    }
  }

  fun deleteBooking(bookingId: String) {
    viewModelScope.launch {
      repository.deleteBooking(bookingId)
    }
  }

  fun setShowAuthDialog(show: Boolean) {
    _showAuthDialog.value = show
  }

  fun setShowNotifications(show: Boolean) {
    _showNotifications.value = show
  }

  fun sendChatMessage(text: String) {
    if (text.isBlank()) return
    val userMsg = ChatMessage(sender = "user", text = text)
    _chatMessages.value = _chatMessages.value + userMsg

    viewModelScope.launch {
      delay(900)
      val reply = generateAssistantReply(text)
      val agentMsg = ChatMessage(sender = "agent", text = reply)
      _chatMessages.value = _chatMessages.value + agentMsg
    }
  }

  private fun generateAssistantReply(query: String): String {
    val q = query.lowercase()
    return when {
      q.contains("civic") ->
        "The Honda Civic RS Turbo is available for PKR 8,000 / 10 hours or PKR 10,000 / full day with seasoned executive chauffeur. Suitable for Karachi city & intercity highway travel."
      q.contains("altis") || q.contains("corolla") ->
        "Toyota Corolla Altis Grande is PKR 6,500 / 10 hours or PKR 8,000 / day. Exceptional comfort, chilled dual AC, and high reliability for Hyderabad or local Karachi."
      q.contains("fortuner") ->
        "Toyota Fortuner Legender 4x4 (7 Seater) is PKR 13,000 / 10 hours or PKR 15,000 / day with driver. Ideal for family tours and intercity motorways."
      q.contains("hiace") || q.contains("van") || q.contains("14") ->
        "Toyota HiAce Grand Cabin (14 seater) is PKR 12,000 / 10 hours or PKR 15,000 / day. Perfect for wedding guest pick-ups and northern trips."
      q.contains("audi") || q.contains("wedding") || q.contains("shadi") ->
        "Audi A6 Luxury Wedding Edition is PKR 22,000 - 25,000 per event. Comes with full fresh rose floral decor and a suited protocol driver."
      q.contains("driver") || q.contains("chauffeur") ->
        "All our rentals include a polite, verified, uniformed driver with verified NADRA CNIC & commercial driving license. For intercity overnight trips, driver meal & stay allowance is PKR 2,000/night."
      q.contains("rate") || q.contains("price") || q.contains("kiraya") ->
        "Our fleet rates start from PKR 6,500 (Altis Grande) up to PKR 25,000 (Audi A6). Fuel, toll taxes, and parking are payable as per actual consumption."
      q.contains("karachi") || q.contains("lahore") || q.contains("islamabad") ->
        "We operate 24/7 in Karachi, Lahore, and Islamabad/Rawalpindi with doorstep car delivery and airport meet & greet."
      q.contains("contact") || q.contains("call") || q.contains("number") || q.contains("whatsapp") ->
        "You can reach our Central Helpline directly at +92 315 2292493 or +92 315 2398490 on Call & WhatsApp."
      else ->
        "Thank you for contacting Hat Cab! We offer Honda Civic RS, Altis Grande, Fortuner 4x4, Changan Oshan X7, HiAce Grand Cabin, and Wedding Cars. You can also tap 'Chat on WhatsApp' below to speak directly with our fleet manager."
    }
  }

  fun updateProfile(name: String, phone: String, email: String, city: String, address: String) {
    repository.updateProfile(name, phone, email, city, address)
  }

  fun verifyOtpAndLogin(phone: String, name: String) {
    repository.loginUser(phone, name)
    _showAuthDialog.value = false
  }

  fun logout() {
    repository.logout()
  }

  fun deleteAccount() {
    viewModelScope.launch {
      repository.deleteAccount()
    }
  }
}
