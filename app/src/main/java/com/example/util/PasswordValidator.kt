package com.example.util

/**
 * Validates password criteria:
 * - Minimum 8 characters
 * - Alphanumeric (contains at least one letter and at least one number)
 */
object PasswordValidator {

  data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
  )

  fun validate(password: String): ValidationResult {
    if (password.isBlank()) {
      return ValidationResult(false, "Password cannot be empty.")
    }
    if (password.length < 8) {
      return ValidationResult(false, "Password must be at least 8 characters long.")
    }
    val hasLetter = password.any { it.isLetter() }
    val hasDigit = password.any { it.isDigit() }
    if (!hasLetter || !hasDigit) {
      return ValidationResult(false, "Password must be alphanumeric (contain both letters and numbers).")
    }
    return ValidationResult(true)
  }
}
