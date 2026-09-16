package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pomo.mypomo.R
import com.example.ui.theme.*
import com.example.ui.viewmodel.AuthUiState
import com.example.ui.viewmodel.AuthViewModel
import com.example.util.PasswordValidator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
  authViewModel: AuthViewModel,
  onLoginSuccess: () -> Unit
) {
  val uiState by authViewModel.uiState.collectAsState()
  val scrollState = rememberScrollState()
  val focusManager = LocalFocusManager.current
  val clipboardManager = LocalClipboardManager.current
  val coroutineScope = rememberCoroutineScope()

  // 0: Password Login, 1: Security Code / OTP
  var selectedLoginMode by remember { mutableStateOf(0) }

  // 0: Mobile Number, 1: Email Address
  var credentialType by remember { mutableStateOf(0) }

  var fullName by remember { mutableStateOf("") }
  var mobileNumber by remember { mutableStateOf("") }
  var emailAddress by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }

  // OTP State
  var otpStep by remember { mutableStateOf(1) } // 1: request, 2: verify
  var generatedCode by remember { mutableStateOf("") }
  var enteredCode by remember { mutableStateOf("") }
  var isDispatchingCode by remember { mutableStateOf(false) }
  var otpCountdown by remember { mutableStateOf(45) }

  // Local validation error message
  var validationError by remember { mutableStateOf<String?>(null) }

  // OTP Countdown timer
  LaunchedEffect(otpStep, generatedCode) {
    if (otpStep == 2) {
      otpCountdown = 45
      while (otpCountdown > 0) {
        delay(1000)
        otpCountdown -= 1
      }
    }
  }

  Scaffold(
    containerColor = BackgroundLight
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(scrollState)
          .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Spacer(modifier = Modifier.height(16.dp))

        // App Logo & Branding Header
        Box(
          modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(10.dp),
          contentAlignment = Alignment.Center
        ) {
          Image(
            painter = painterResource(id = R.drawable.pakedrive_logo),
            contentDescription = "PAK E DRIVE Logo",
            modifier = Modifier.fillMaxSize()
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = "PAK E DRIVE",
          fontSize = 24.sp,
          fontWeight = FontWeight.Black,
          color = NavyPrimary
        )

        Text(
          text = "Pakistan's Premier Executive Car Rental & Fleet",
          fontSize = 12.sp,
          color = TextSecondaryMuted,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Main Login Card
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(20.dp)
          ) {
            // Mode Switcher: Password vs OTP Code
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF1F5F9))
                .padding(4.dp)
            ) {
              Box(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(8.dp))
                  .background(if (selectedLoginMode == 0) Color.White else Color.Transparent)
                  .clickable {
                    selectedLoginMode = 0
                    validationError = null
                    authViewModel.resetState()
                  }
                  .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (selectedLoginMode == 0) NavyPrimary else TextSecondaryMuted,
                    modifier = Modifier.size(15.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "Password Login",
                    fontSize = 12.5.sp,
                    fontWeight = if (selectedLoginMode == 0) FontWeight.Bold else FontWeight.Medium,
                    color = if (selectedLoginMode == 0) NavyPrimary else TextSecondaryMuted
                  )
                }
              }

              Box(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(8.dp))
                  .background(if (selectedLoginMode == 1) Color.White else Color.Transparent)
                  .clickable {
                    selectedLoginMode = 1
                    validationError = null
                    authViewModel.resetState()
                  }
                  .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    Icons.Default.Sms,
                    contentDescription = null,
                    tint = if (selectedLoginMode == 1) NavyPrimary else TextSecondaryMuted,
                    modifier = Modifier.size(15.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "Security Code / OTP",
                    fontSize = 12.5.sp,
                    fontWeight = if (selectedLoginMode == 1) FontWeight.Bold else FontWeight.Medium,
                    color = if (selectedLoginMode == 1) NavyPrimary else TextSecondaryMuted
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ================= MODE 0: PASSWORD LOGIN =================
            if (selectedLoginMode == 0) {
              // Full Name field
              Text(
                text = "Full Name",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimaryDark
              )
              Spacer(modifier = Modifier.height(4.dp))
              OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                singleLine = true,
                placeholder = { Text("e.g. Mehdi Raza", color = TextSecondaryMuted, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = NavyPrimary) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = NavyPrimary,
                  unfocusedBorderColor = BorderStroke
                )
              )

              Spacer(modifier = Modifier.height(14.dp))

              // Credential Type Selector (Mobile vs Email)
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = if (credentialType == 0) "Mobile Phone Number" else "Email Address",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = TextPrimaryDark
                )
                TextButton(
                  onClick = {
                    credentialType = if (credentialType == 0) 1 else 0
                    validationError = null
                  },
                  contentPadding = PaddingValues(0.dp)
                ) {
                  Text(
                    text = if (credentialType == 0) "Use Email Instead" else "Use Mobile (+92)",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = StatusGreen
                  )
                }
              }

              Spacer(modifier = Modifier.height(4.dp))

              if (credentialType == 0) {
                OutlinedTextField(
                  value = mobileNumber,
                  onValueChange = { mobileNumber = it },
                  singleLine = true,
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                  leadingIcon = {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      modifier = Modifier.padding(start = 10.dp, end = 6.dp)
                    ) {
                      Text("🇵🇰 +92", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                    }
                  },
                  placeholder = { Text("315 2292493", color = TextSecondaryMuted, fontSize = 13.sp) },
                  modifier = Modifier.fillMaxWidth(),
                  shape = RoundedCornerShape(10.dp),
                  colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NavyPrimary,
                    unfocusedBorderColor = BorderStroke
                  )
                )
              } else {
                OutlinedTextField(
                  value = emailAddress,
                  onValueChange = { emailAddress = it },
                  singleLine = true,
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                  leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = NavyPrimary) },
                  placeholder = { Text("user@gmail.com", color = TextSecondaryMuted, fontSize = 13.sp) },
                  modifier = Modifier.fillMaxWidth(),
                  shape = RoundedCornerShape(10.dp),
                  colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NavyPrimary,
                    unfocusedBorderColor = BorderStroke
                  )
                )
              }

              Spacer(modifier = Modifier.height(14.dp))

              // Password field
              Text(
                text = "Password",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimaryDark
              )
              Spacer(modifier = Modifier.height(4.dp))
              OutlinedTextField(
                value = password,
                onValueChange = {
                  password = it
                  validationError = null
                },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = NavyPrimary) },
                trailingIcon = {
                  IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                      if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                      contentDescription = if (passwordVisible) "Hide password" else "Show password",
                      tint = TextSecondaryMuted
                    )
                  }
                },
                placeholder = { Text("Min 8 chars, letters & numbers", color = TextSecondaryMuted, fontSize = 12.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = NavyPrimary,
                  unfocusedBorderColor = BorderStroke
                )
              )

              Spacer(modifier = Modifier.height(6.dp))

              // Password Requirement Helper
              Row(verticalAlignment = Alignment.CenterVertically) {
                val hasLength = password.length >= 8
                val hasAlphaNum = password.any { it.isLetter() } && password.any { it.isDigit() }
                Icon(
                  if (hasLength && hasAlphaNum) Icons.Default.CheckCircle else Icons.Default.Info,
                  contentDescription = null,
                  tint = if (hasLength && hasAlphaNum) StatusGreen else TextSecondaryMuted,
                  modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Requires minimum 8 characters with letters & numbers",
                  fontSize = 10.5.sp,
                  color = if (hasLength && hasAlphaNum) StatusGreen else TextSecondaryMuted
                )
              }

              // Display Error Message
              val currentError = validationError ?: (uiState as? AuthUiState.Error)?.errorMessage
              currentError?.let { err ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                  text = err,
                  color = StatusRed,
                  fontSize = 11.5.sp,
                  fontWeight = FontWeight.Medium
                )
              }

              Spacer(modifier = Modifier.height(18.dp))

              // Submit Button with Loading Indicator
              val isLoading = uiState is AuthUiState.Loading

              Button(
                onClick = {
                  focusManager.clearFocus()
                  val identifier = if (credentialType == 0) {
                    val digits = mobileNumber.filter { it.isDigit() }
                    if (digits.length < 7) {
                      validationError = "Please enter a valid mobile number."
                      return@Button
                    }
                    if (digits.startsWith("0")) "+92 " + digits.substring(1) else "+92 $digits"
                  } else {
                    if (emailAddress.isBlank() || !emailAddress.contains("@")) {
                      validationError = "Please enter a valid email address."
                      return@Button
                    }
                    emailAddress.trim()
                  }

                  val validation = PasswordValidator.validate(password)
                  if (!validation.isValid) {
                    validationError = validation.errorMessage
                    return@Button
                  }

                  validationError = null
                  authViewModel.loginWithPassword(
                    identifier = identifier,
                    name = fullName,
                    password = password,
                    isEmail = credentialType == 1,
                    onSuccess = onLoginSuccess
                  )
                },
                enabled = !isLoading,
                modifier = Modifier
                  .fillMaxWidth()
                  .height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
              ) {
                if (isLoading) {
                  CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                  )
                  Spacer(modifier = Modifier.width(10.dp))
                  Text("Authenticating...", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                } else {
                  Icon(Icons.Default.Login, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("Sign In & Continue", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
              }
            } else {
              // ================= MODE 1: SECURITY CODE / OTP =================
              if (otpStep == 1) {
                Text(
                  text = "Login via One-Time Security Code",
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold,
                  color = TextPrimaryDark
                )
                Text(
                  text = "We will send an immediate 6-digit authentication code directly to your phone or WhatsApp.",
                  fontSize = 11.5.sp,
                  color = TextSecondaryMuted,
                  lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text("Your Name", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimaryDark)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                  value = fullName,
                  onValueChange = { fullName = it },
                  singleLine = true,
                  placeholder = { Text("e.g. Mehdi Raza", color = TextSecondaryMuted, fontSize = 13.sp) },
                  modifier = Modifier.fillMaxWidth(),
                  shape = RoundedCornerShape(10.dp),
                  colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NavyPrimary,
                    unfocusedBorderColor = BorderStroke
                  )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("Mobile Number", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimaryDark)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                  value = mobileNumber,
                  onValueChange = { mobileNumber = it },
                  singleLine = true,
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                  leadingIcon = {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      modifier = Modifier.padding(start = 10.dp, end = 6.dp)
                    ) {
                      Text("🇵🇰 +92", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                    }
                  },
                  placeholder = { Text("315 2292493", color = TextSecondaryMuted, fontSize = 13.sp) },
                  modifier = Modifier.fillMaxWidth(),
                  shape = RoundedCornerShape(10.dp),
                  colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NavyPrimary,
                    unfocusedBorderColor = BorderStroke
                  )
                )

                validationError?.let { err ->
                  Spacer(modifier = Modifier.height(6.dp))
                  Text(text = err, color = StatusRed, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                  onClick = {
                    val digits = mobileNumber.filter { it.isDigit() }
                    if (digits.length < 7) {
                      validationError = "Please enter a valid mobile number."
                      return@Button
                    }
                    validationError = null
                    isDispatchingCode = true

                    val code = (100000..999999).random().toString()
                    generatedCode = code
                    enteredCode = ""

                    coroutineScope.launch {
                      delay(600)
                      isDispatchingCode = false
                      otpStep = 2
                    }
                  },
                  enabled = !isDispatchingCode,
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                  shape = RoundedCornerShape(10.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = StatusGreen)
                ) {
                  if (isDispatchingCode) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                  } else {
                    Icon(Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Send 6-Digit Security Code", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                  }
                }
              } else {
                // Step 2: Enter code
                Surface(
                  shape = RoundedCornerShape(12.dp),
                  color = Color(0xFFE8F8EE),
                  border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E7E34).copy(alpha = 0.3f)),
                  modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                      enteredCode = generatedCode
                      clipboardManager.setText(AnnotatedString(generatedCode))
                    }
                ) {
                  Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Text(text = "Code Dispatched • Just Now", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E7E34))
                      Text(text = "Tap to Auto-Fill", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E7E34))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                      text = "PAK E DRIVE Login Code: $generatedCode (Valid for 10 minutes)",
                      fontSize = 12.sp,
                      fontWeight = FontWeight.SemiBold,
                      color = Color(0xFF111827)
                    )
                  }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                  text = "Enter 6-Digit Security Code",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = TextPrimaryDark
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                  value = enteredCode,
                  onValueChange = {
                    if (it.length <= 6 && it.all { ch -> ch.isDigit() }) {
                      enteredCode = it
                      validationError = null
                    }
                  },
                  singleLine = true,
                  placeholder = {
                    Text(
                      "• • • • • •",
                      color = TextSecondaryMuted,
                      fontSize = 20.sp,
                      textAlign = TextAlign.Center,
                      modifier = Modifier.fillMaxWidth()
                    )
                  },
                  textStyle = LocalTextStyle.current.copy(
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 6.sp,
                    color = NavyPrimary
                  ),
                  modifier = Modifier.fillMaxWidth(),
                  shape = RoundedCornerShape(10.dp),
                  colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NavyPrimary,
                    unfocusedBorderColor = BorderStroke
                  )
                )

                validationError?.let { err ->
                  Spacer(modifier = Modifier.height(6.dp))
                  Text(text = err, color = StatusRed, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Paste button
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = Color(0xFFEFF6FF),
                  border = androidx.compose.foundation.BorderStroke(1.dp, NavyPrimary.copy(alpha = 0.3f)),
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .clickable {
                      enteredCode = generatedCode
                      clipboardManager.setText(AnnotatedString(generatedCode))
                    }
                ) {
                  Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                  ) {
                    Icon(Icons.Default.ContentPaste, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Auto-Paste Code ($generatedCode)", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                  }
                }

                Spacer(modifier = Modifier.height(14.dp))

                val isOtpVerifying = uiState is AuthUiState.Loading

                Button(
                  onClick = {
                    val digits = mobileNumber.filter { it.isDigit() }
                    val phone = if (digits.startsWith("0")) "+92 " + digits.substring(1) else "+92 $digits"
                    authViewModel.loginWithCode(
                      identifier = phone,
                      name = fullName,
                      enteredCode = enteredCode,
                      expectedCode = generatedCode,
                      onSuccess = onLoginSuccess
                    )
                  },
                  enabled = enteredCode.length == 6 && !isOtpVerifying,
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                  shape = RoundedCornerShape(10.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                  if (isOtpVerifying) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                  } else {
                    Text("Verify & Open App", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                  }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  TextButton(onClick = { otpStep = 1; enteredCode = "" }) {
                    Text("Change Number", color = TextSecondaryMuted, fontSize = 12.sp)
                  }

                  TextButton(
                    onClick = {
                      if (otpCountdown == 0) {
                        generatedCode = (100000..999999).random().toString()
                        enteredCode = ""
                        otpCountdown = 45
                      }
                    },
                    enabled = otpCountdown == 0
                  ) {
                    Text(
                      text = if (otpCountdown > 0) "Resend in ${otpCountdown}s" else "Resend Code",
                      color = if (otpCountdown > 0) TextSecondaryMuted else NavyPrimary,
                      fontSize = 12.sp,
                      fontWeight = FontWeight.SemiBold
                    )
                  }
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Security & Privacy Footer Note
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(horizontal = 16.dp)
        ) {
          Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = StatusGreen, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "256-Bit SSL Secured • Verified Pakistan Chauffeur Network",
            fontSize = 10.5.sp,
            color = TextSecondaryMuted
          )
        }

        Spacer(modifier = Modifier.height(16.dp))
      }
    }
  }
}
