package com.example.util

enum class AppLanguage(val code: String, val label: String, val nativeLabel: String) {
  ENGLISH("en", "English", "English"),
  URDU("ur", "English", "English")
}

object LocaleStrings {

  fun get(key: String, lang: AppLanguage): String {
    return englishMap[key] ?: key
  }

  private val englishMap = mapOf(
    "app_name" to "PAK E DRIVE",
    "app_tagline" to "Premier Chauffeur Car Rental in Pakistan",
    "tab_home" to "Home",
    "tab_fleet" to "Cars",
    "tab_bookings" to "Bookings",
    "tab_support" to "Support",
    "tab_profile" to "Profile",
    "book_now" to "Book Now",
    "view_details" to "Details",
    "featured" to "Featured",
    "with_driver" to "With Chauffeur",
    "select_city" to "Select City",
    "all_cities" to "All Pakistan Cities",
    "search_cars" to "Search Cars...",
    "faq_support" to "FAQs & Support",
    "faq_subtitle" to "Quick customer care and common rental queries",
    "rate_review" to "Rate & Review",
    "rate_ride" to "Rate Ride",
    "customer_reviews" to "Customer Reviews",
    "submit_review" to "Submit Review",
    "car_rating_label" to "Vehicle & AC Condition",
    "driver_rating_label" to "Chauffeur Behavior & Punctuality",
    "review_placeholder" to "Write your feedback about this ride...",
    "verified_customer" to "Verified Customer",
    "login_whatsapp" to "Login via WhatsApp / SMS",
    "send_whatsapp_code" to "Send WhatsApp Code",
    "send_sms_code" to "Send SMS Code",
    "enter_otp" to "Enter 6-Digit Code",
    "verify_continue" to "Verify & Continue",
    "resend_code" to "Resend Code",
    "code_sent_notice" to "Verification code dispatched to your phone",
    "alerts_notifications" to "Alerts & Notifications",
    "mark_all_read" to "Mark All Read",
    "clear_all" to "Clear All",
    "no_alerts" to "No new alerts right now",
    "offline_notice" to "No Internet Connection • Offline Mode",
    "online_notice" to "Back Online • Internet Restored",
    "my_bookings" to "My Bookings",
    "no_bookings" to "No active bookings yet",
    "no_bookings_sub" to "Book an intercity or local car to view confirmed chauffeur details.",
    "helpline_title" to "24/7 Car Rental Support",
    "uan_1" to "UAN Helpline 1",
    "uan_2" to "UAN Helpline 2",
    "whatsapp_manager" to "WhatsApp Fleet Manager",
    "lang_toggle" to "EN",
    "active" to "Active",
    "completed" to "Completed",
    "cancelled" to "Cancelled",
    "driver_details" to "Chauffeur Details",
    "call_driver" to "Call Chauffeur",
    "whatsapp_driver" to "WhatsApp",
    "total_rent" to "Total Rent",
    "discount_applied" to "Discount Applied"
  )
}

