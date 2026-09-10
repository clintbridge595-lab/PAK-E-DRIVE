package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Booking
import com.example.data.model.Car
import com.example.data.model.Review
import com.example.data.model.UserProfile
import com.example.data.repository.CarRentalRepository
import com.example.util.AppLanguage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
  val allReviews: StateFlow<List<Review>> = repository.allReviews
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  private val _currentLanguage = MutableStateFlow(repository.getSavedLanguage())
  val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

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

  private val _showFaqSupport = MutableStateFlow(false)
  val showFaqSupport: StateFlow<Boolean> = _showFaqSupport.asStateFlow()

  private val _showBuildLogAnalyzer = MutableStateFlow(false)
  val showBuildLogAnalyzer: StateFlow<Boolean> = _showBuildLogAnalyzer.asStateFlow()

  private val _reviewBooking = MutableStateFlow<Booking?>(null)
  val reviewBooking: StateFlow<Booking?> = _reviewBooking.asStateFlow()

  private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
  val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

  val unreadNotificationCount: StateFlow<Int> = _notifications.map { list ->
    list.count { !it.isRead }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

  private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
    listOf(
      ChatMessage(
        sender = "agent",
        text = "Assalam-o-Alaikum! Welcome to PAK E DRIVE Official Assistance.\n\nLooking for a Civic RS Turbo, Altis Grande, Fortuner Legender or HiAce Grand Cabin? Ask about rates, chauffeur policies, or intercity routes!",
        timestamp = "Online"
      )
    )
  )
  val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

  fun setLanguage(language: AppLanguage) {
    _currentLanguage.value = language
    repository.saveLanguage(language)
  }

  fun toggleLanguage() {
    val next = if (_currentLanguage.value == AppLanguage.URDU) AppLanguage.ENGLISH else AppLanguage.URDU
    setLanguage(next)
  }

  fun setShowFaqSupport(show: Boolean) {
    _showFaqSupport.value = show
  }

  fun setShowBuildLogAnalyzer(show: Boolean) {
    _showBuildLogAnalyzer.value = show
  }

  fun openReviewDialog(booking: Booking) {
    _reviewBooking.value = booking
  }

  fun closeReviewDialog() {
    _reviewBooking.value = null
  }

  fun submitReview(carRating: Float, driverRating: Float, comment: String, booking: Booking) {
    viewModelScope.launch {
      val avg = (carRating + driverRating) / 2f
      val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
      val review = Review(
        id = UUID.randomUUID().toString(),
        carId = booking.carId,
        carName = booking.carName,
        bookingId = booking.id,
        userName = userProfile.value.name.ifBlank { "Verified Renter" },
        userCity = userProfile.value.city.ifBlank { "Karachi" },
        carRating = carRating,
        driverRating = driverRating,
        overallRating = avg,
        comment = comment.ifBlank { "Excellent condition and verified polite driver." },
        date = dateStr,
        verifiedRental = true
      )
      repository.insertReview(review)
      _reviewBooking.value = null

      addNotification(
        title = "Review Submitted",
        message = "Thank you for rating ${booking.carName} and driver ${booking.driverName} ($avg ★)."
      )
    }
  }

  fun markAllNotificationsAsRead() {
    _notifications.value = _notifications.value.map { it.copy(isRead = true) }
  }

  fun clearAllNotifications() {
    _notifications.value = emptyList()
  }

  fun deleteNotification(id: String) {
    _notifications.value = _notifications.value.filter { it.id != id }
  }

  fun addNotification(title: String, message: String) {
    val notif = AppNotification(
      title = title,
      message = message,
      time = "Just now",
      isRead = false
    )
    _notifications.value = listOf(notif) + _notifications.value
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
        "Thank you for contacting PAK E DRIVE! We offer Honda Civic RS, Altis Grande, Fortuner 4x4, Changan Oshan X7, HiAce Grand Cabin, and Wedding Cars. You can also tap 'Chat on WhatsApp' below to speak directly with our fleet manager."
    }
  }

  fun updateProfile(name: String, phone: String, email: String, city: String, address: String) {
    repository.updateProfile(name, phone, email, city, address)
  }

  fun verifyOtpAndLogin(phone: String, name: String) {
    repository.loginUser(phone, name)
    addNotification(
      title = "Login Verified",
      message = "Welcome to PAK E DRIVE, $name! Your phone $phone has been verified."
    )
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
