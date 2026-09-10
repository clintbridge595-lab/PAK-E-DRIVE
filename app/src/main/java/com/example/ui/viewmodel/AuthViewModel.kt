package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.UserProfile
import com.example.data.repository.CarRentalRepository
import com.example.util.PasswordValidator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
  object Idle : AuthUiState()
  object Loading : AuthUiState()
  data class Success(val message: String) : AuthUiState()
  data class Error(val errorMessage: String) : AuthUiState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {
  private val repository = CarRentalRepository(application)

  val userProfile: StateFlow<UserProfile> = repository.userProfile

  private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
  val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

  fun resetState() {
    _uiState.value = AuthUiState.Idle
  }

  fun loginWithPassword(
    identifier: String,
    name: String,
    password: String,
    isEmail: Boolean,
    onSuccess: () -> Unit
  ) {
    val cleanId = identifier.trim()
    if (cleanId.isEmpty()) {
      _uiState.value = AuthUiState.Error(
        if (isEmail) "Please enter your email address." else "Please enter your mobile number."
      )
      return
    }

    if (isEmail && (!cleanId.contains("@") || !cleanId.contains("."))) {
      _uiState.value = AuthUiState.Error("Please enter a valid email address.")
      return
    }

    val passwordValidation = PasswordValidator.validate(password)
    if (!passwordValidation.isValid) {
      _uiState.value = AuthUiState.Error(passwordValidation.errorMessage ?: "Invalid password.")
      return
    }

    _uiState.value = AuthUiState.Loading
    viewModelScope.launch {
      // Simulate secure authentication roundtrip
      delay(700)
      repository.loginWithPassword(cleanId, name.trim(), isEmail)
      _uiState.value = AuthUiState.Success("Authentication successful! Welcome to PAK E DRIVE.")
      onSuccess()
    }
  }

  fun loginWithCode(
    identifier: String,
    name: String,
    enteredCode: String,
    expectedCode: String,
    onSuccess: () -> Unit
  ) {
    val cleanCode = enteredCode.trim()
    if (cleanCode.length != 6) {
      _uiState.value = AuthUiState.Error("Please enter the complete 6-digit verification code.")
      return
    }

    if (cleanCode != expectedCode.trim()) {
      _uiState.value = AuthUiState.Error("Invalid verification code. Please check the code dispatched to you.")
      return
    }

    _uiState.value = AuthUiState.Loading
    viewModelScope.launch {
      delay(500)
      val cleanId = identifier.trim()
      val isEmail = cleanId.contains("@")
      repository.loginWithPassword(cleanId, name.trim(), isEmail)
      _uiState.value = AuthUiState.Success("Verification code confirmed!")
      onSuccess()
    }
  }

  fun logout() {
    repository.logout()
    _uiState.value = AuthUiState.Idle
  }
}
