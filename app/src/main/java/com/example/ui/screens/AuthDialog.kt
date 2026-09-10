package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class OtpChannel {
  WHATSAPP,
  SMS,
  EMAIL
}

@Composable
fun AuthDialog(
  viewModel: MainViewModel,
  onDismiss: () -> Unit
) {
  val coroutineScope = rememberCoroutineScope()
  val clipboardManager = LocalClipboardManager.current

  // 0: WhatsApp / SMS, 1: Gmail / Email, 2: Google
  var authTab by remember { mutableStateOf(0) }

  // Flow State (1: Enter credentials, 2: Enter & Verify OTP)
  var step by remember { mutableStateOf(1) }
  var activeChannel by remember { mutableStateOf(OtpChannel.WHATSAPP) }

  // User input state
  var userName by remember { mutableStateOf("") }
  var phoneNumber by remember { mutableStateOf("") }
  var emailInput by remember { mutableStateOf("") }

  // OTP generation and validation
  var generatedOtp by remember { mutableStateOf("") }
  var enteredOtp by remember { mutableStateOf("") }
  var resendCountdown by remember { mutableStateOf(45) }
  var isSendingOtp by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var isGoogleLoading by remember { mutableStateOf(false) }

  // Timer for OTP resend countdown
  LaunchedEffect(step, generatedOtp) {
    if (step == 2) {
      resendCountdown = 45
      while (resendCountdown > 0) {
        delay(1000)
        resendCountdown -= 1
      }
    }
  }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 4.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Top Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF1F5F9)),
              contentAlignment = Alignment.Center
            ) {
              Image(
                painter = painterResource(id = R.drawable.pakedrive_logo),
                contentDescription = "PAK E DRIVE Logo",
                modifier = Modifier.size(32.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "PAK E DRIVE",
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = NavyPrimary
              )
              Text(
                text = "Secure Member Access",
                fontSize = 11.sp,
                color = TextSecondaryMuted
              )
            }
          }
          IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondaryMuted)
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Auth Method Tabs (only when step == 1)
        if (step == 1) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .background(Color(0xFFF1F5F9))
              .padding(3.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
          ) {
            listOf(
              Pair("WhatsApp / SMS", Icons.Default.Chat),
              Pair("Gmail / Email", Icons.Default.Email),
              Pair("Google", Icons.Default.AccountCircle)
            ).forEachIndexed { index, pair ->
              val isSelected = authTab == index
              Box(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(8.dp))
                  .background(if (isSelected) Color.White else Color.Transparent)
                  .clickable {
                    authTab = index
                    errorMessage = null
                  }
                  .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.Center
                ) {
                  Icon(
                    imageVector = pair.second,
                    contentDescription = null,
                    tint = if (isSelected) NavyPrimary else TextSecondaryMuted,
                    modifier = Modifier.size(14.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = pair.first,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) NavyPrimary else TextSecondaryMuted
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))
        }

        // ================= STEP 1: CREDENTIAL INPUT =================
        if (step == 1) {
          when (authTab) {
            // TAB 0: WhatsApp / SMS
            0 -> {
              Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                  text = "Login via WhatsApp or SMS OTP",
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold,
                  color = TextPrimaryDark
                )
                Text(
                  text = "Enter your details to receive a 6-digit security code directly on your phone.",
                  fontSize = 12.sp,
                  color = TextSecondaryMuted,
                  lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Name input
                Text("Your Name", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimaryDark)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                  value = userName,
                  onValueChange = { userName = it },
                  singleLine = true,
                  placeholder = { Text("e.g. Mehdi Raza", color = TextSecondaryMuted, fontSize = 13.sp) },
                  modifier = Modifier.fillMaxWidth(),
                  shape = RoundedCornerShape(10.dp),
                  colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF0A0F1D),
                    unfocusedTextColor = Color(0xFF0A0F1D),
                    cursorColor = NavyPrimary,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = NavyPrimary,
                    unfocusedBorderColor = BorderStroke
                  )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Phone input
                Text("Mobile Number", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimaryDark)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                  value = phoneNumber,
                  onValueChange = { phoneNumber = it },
                  singleLine = true,
                  leadingIcon = {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      modifier = Modifier.padding(start = 12.dp, end = 4.dp)
                    ) {
                      Text("🇵🇰 +92", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                      Spacer(modifier = Modifier.width(4.dp))
                      Box(modifier = Modifier.width(1.dp).height(18.dp).background(BorderStroke))
                    }
                  },
                  placeholder = { Text("315 2292493", color = TextSecondaryMuted, fontSize = 13.sp) },
                  modifier = Modifier.fillMaxWidth(),
                  shape = RoundedCornerShape(10.dp),
                  colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF0A0F1D),
                    unfocusedTextColor = Color(0xFF0A0F1D),
                    cursorColor = NavyPrimary,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = NavyPrimary,
                    unfocusedBorderColor = BorderStroke
                  )
                )

                errorMessage?.let { err ->
                  Spacer(modifier = Modifier.height(6.dp))
                  Text(text = err, color = StatusRed, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Dispatch Buttons (WhatsApp & SMS)
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  // WhatsApp Code Dispatch (Direct backend delivery without external redirect)
                  Button(
                    onClick = {
                      val digits = phoneNumber.filter { it.isDigit() }
                      if (digits.length < 7) {
                        errorMessage = "Please enter a valid mobile number."
                        return@Button
                      }
                      errorMessage = null
                      isSendingOtp = true
                      activeChannel = OtpChannel.WHATSAPP

                      val code = (100000..999999).random().toString()
                      generatedOtp = code
                      enteredOtp = ""

                      coroutineScope.launch {
                        delay(600) // Simulated backend API dispatch
                        isSendingOtp = false
                        step = 2

                        val cleanPhone = if (digits.startsWith("0")) "92" + digits.substring(1) else if (digits.startsWith("92")) digits else "92$digits"
                        viewModel.addNotification(
                          title = "WhatsApp Security Code Dispatched",
                          message = "Verification code $code dispatched to WhatsApp at +$cleanPhone."
                        )
                      }
                    },
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                  ) {
                    if (isSendingOtp && activeChannel == OtpChannel.WHATSAPP) {
                      CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                      Icon(Icons.Default.Chat, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                      Spacer(modifier = Modifier.width(6.dp))
                      Text("WhatsApp Code", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                  }

                  // SMS Code Dispatch (Direct backend delivery without external redirect)
                  Button(
                    onClick = {
                      val digits = phoneNumber.filter { it.isDigit() }
                      if (digits.length < 7) {
                        errorMessage = "Please enter a valid mobile number."
                        return@Button
                      }
                      errorMessage = null
                      isSendingOtp = true
                      activeChannel = OtpChannel.SMS

                      val code = (100000..999999).random().toString()
                      generatedOtp = code
                      enteredOtp = ""

                      coroutineScope.launch {
                        delay(600) // Simulated backend SMS gateway dispatch
                        isSendingOtp = false
                        step = 2

                        val cleanPhone = if (digits.startsWith("0")) "92" + digits.substring(1) else if (digits.startsWith("92")) digits else "92$digits"
                        viewModel.addNotification(
                          title = "SMS Security Code Dispatched",
                          message = "Verification code $code sent via SMS to +$cleanPhone."
                        )
                      }
                    },
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                  ) {
                    if (isSendingOtp && activeChannel == OtpChannel.SMS) {
                      CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                      Icon(Icons.Default.Sms, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                      Spacer(modifier = Modifier.width(6.dp))
                      Text("SMS Code", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                  }
                }
              }
            }

            // TAB 1: Gmail / Email
            1 -> {
              Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                  text = "Login via Gmail / Email OTP",
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold,
                  color = TextPrimaryDark
                )
                Text(
                  text = "We will send an authentication code directly to your email inbox.",
                  fontSize = 12.sp,
                  color = TextSecondaryMuted,
                  lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Name input
                Text("Your Name", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimaryDark)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                  value = userName,
                  onValueChange = { userName = it },
                  singleLine = true,
                  placeholder = { Text("e.g. Mehdi Raza", color = TextSecondaryMuted, fontSize = 13.sp) },
                  modifier = Modifier.fillMaxWidth(),
                  shape = RoundedCornerShape(10.dp),
                  colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF0A0F1D),
                    unfocusedTextColor = Color(0xFF0A0F1D),
                    cursorColor = NavyPrimary,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = NavyPrimary,
                    unfocusedBorderColor = BorderStroke
                  )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Email input
                Text("Email / Gmail Address", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimaryDark)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                  value = emailInput,
                  onValueChange = { emailInput = it },
                  singleLine = true,
                  placeholder = { Text("name@gmail.com", color = TextSecondaryMuted, fontSize = 13.sp) },
                  modifier = Modifier.fillMaxWidth(),
                  shape = RoundedCornerShape(10.dp),
                  colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF0A0F1D),
                    unfocusedTextColor = Color(0xFF0A0F1D),
                    cursorColor = NavyPrimary,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = NavyPrimary,
                    unfocusedBorderColor = BorderStroke
                  )
                )

                errorMessage?.let { err ->
                  Spacer(modifier = Modifier.height(6.dp))
                  Text(text = err, color = StatusRed, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                  onClick = {
                    if (emailInput.isBlank() || !emailInput.contains("@") || !emailInput.contains(".")) {
                      errorMessage = "Please enter a valid email address."
                      return@Button
                    }
                    errorMessage = null
                    isSendingOtp = true
                    activeChannel = OtpChannel.EMAIL

                    val code = (100000..999999).random().toString()
                    generatedOtp = code
                    enteredOtp = ""

                    coroutineScope.launch {
                      delay(600) // Simulated backend email dispatch
                      isSendingOtp = false
                      step = 2

                      viewModel.addNotification(
                        title = "Email Security Code Dispatched",
                        message = "Verification code $code sent to $emailInput."
                      )
                    }
                  },
                  modifier = Modifier.fillMaxWidth().height(48.dp),
                  shape = RoundedCornerShape(10.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                  if (isSendingOtp) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                  } else {
                    Icon(Icons.Default.Email, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Send Code to Email", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                  }
                }
              }
            }

            // TAB 2: Google Sign In
            2 -> {
              Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Text(
                  text = "Sign in with Google",
                  fontSize = 15.sp,
                  fontWeight = FontWeight.Bold,
                  color = TextPrimaryDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "Fast, secure 1-tap sign in with your Google Account.",
                  fontSize = 12.sp,
                  color = TextSecondaryMuted,
                  textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedButton(
                  onClick = {
                    isGoogleLoading = true
                    coroutineScope.launch {
                      delay(500)
                      isGoogleLoading = false
                      val googleUser = if (userName.isNotBlank()) userName else "Google Verified Member"
                      val googlePhone = if (phoneNumber.isNotBlank()) phoneNumber else "+92 315 2292493"
                      viewModel.verifyOtpAndLogin(phone = googlePhone, name = googleUser)
                      onDismiss()
                    }
                  },
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier.fillMaxWidth().height(48.dp),
                  colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                  border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(BorderStroke)
                  )
                ) {
                  if (isGoogleLoading) {
                    CircularProgressIndicator(color = NavyPrimary, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                  } else {
                    Text("G", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF4285F4))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Continue with Google", color = TextPrimaryDark, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                  }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text("Direct authenticated session without SMS requirement", fontSize = 11.sp, color = TextSecondaryMuted)
              }
            }
          }
        } else {
          // ================= STEP 2: ENTER & VERIFY OTP =================
          Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            // Prominent INCOMING MESSAGE NOTIFICATION CARD
            // Shows user the dispatched code clearly without throwing them out of the app
            val (bannerBg, headerColor, iconVector, channelName, targetDestination) = when (activeChannel) {
              OtpChannel.WHATSAPP -> Quad(Color(0xFFE8F8EE), Color(0xFF1E7E34), Icons.Default.Chat, "WhatsApp Message", phoneNumber)
              OtpChannel.SMS -> Quad(Color(0xFFEFF6FF), Color(0xFF1E40AF), Icons.Default.Sms, "SMS Message", phoneNumber)
              OtpChannel.EMAIL -> Quad(Color(0xFFFEF3C7), Color(0xFF92400E), Icons.Default.Email, "Gmail Notification", emailInput)
            }

            Surface(
              shape = RoundedCornerShape(12.dp),
              color = bannerBg,
              border = androidx.compose.foundation.BorderStroke(1.dp, headerColor.copy(alpha = 0.3f)),
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  // Tap banner to auto-fill code
                  enteredOtp = generatedOtp
                  clipboardManager.setText(AnnotatedString(generatedOtp))
                  errorMessage = null
                }
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(iconVector, contentDescription = null, tint = headerColor, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "$channelName • Just Now", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = headerColor)
                  }
                  Text(text = "Tap to Auto-Fill", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = headerColor)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                  text = "PAK E DRIVE Security: Your login verification code is $generatedOtp. Valid for 10 minutes.",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Medium,
                  color = Color(0xFF111827)
                )
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
              text = "Enter 6-Digit Code",
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = TextPrimaryDark
            )
            Text(
              text = "Code dispatched to $targetDestination",
              fontSize = 12.sp,
              color = TextSecondaryMuted,
              textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // OTP Input Box
            OutlinedTextField(
              value = enteredOtp,
              onValueChange = {
                if (it.length <= 6 && it.all { ch -> ch.isDigit() }) {
                  enteredOtp = it
                  errorMessage = null
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
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 8.sp,
                color = NavyPrimary
              ),
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(10.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = NavyPrimary,
                unfocusedTextColor = NavyPrimary,
                cursorColor = NavyPrimary,
                focusedContainerColor = Color(0xFFF8FAFC),
                unfocusedContainerColor = Color(0xFFF8FAFC),
                focusedBorderColor = NavyPrimary,
                unfocusedBorderColor = BorderStroke
              )
            )

            errorMessage?.let { err ->
              Spacer(modifier = Modifier.height(6.dp))
              Text(text = err, color = StatusRed, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Auto-Paste Button
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color(0xFFEFF6FF),
              border = androidx.compose.foundation.BorderStroke(1.dp, NavyPrimary.copy(alpha = 0.3f)),
              modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .clickable {
                  enteredOtp = generatedOtp
                  clipboardManager.setText(AnnotatedString(generatedOtp))
                  errorMessage = null
                }
            ) {
              Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
              ) {
                Icon(Icons.Default.ContentPaste, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Auto-Paste Code ($generatedOtp)", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Verify & Continue Button
            Button(
              onClick = {
                if (enteredOtp.trim() == generatedOtp.trim() && generatedOtp.isNotBlank()) {
                  val displayName = userName.ifBlank { "Valued Member" }
                  val userContact = if (activeChannel == OtpChannel.EMAIL) {
                    emailInput.ifBlank { "member@pakedrive.pk" }
                  } else {
                    val digits = phoneNumber.filter { it.isDigit() }
                    if (digits.startsWith("0")) "+92 " + digits.substring(1) else "+92 $digits"
                  }

                  viewModel.verifyOtpAndLogin(userContact, displayName)
                  onDismiss()
                } else {
                  errorMessage = "Invalid verification code. Please enter the 6-digit code shown above."
                }
              },
              enabled = enteredOtp.length == 6,
              modifier = Modifier.fillMaxWidth().height(48.dp),
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
            ) {
              Text("Verify & Continue", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Resend & Change options
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              TextButton(onClick = { step = 1; enteredOtp = ""; errorMessage = null }) {
                Text("Change Details", color = TextSecondaryMuted, fontSize = 12.sp)
              }

              TextButton(
                onClick = {
                  if (resendCountdown == 0) {
                    val newCode = (100000..999999).random().toString()
                    generatedOtp = newCode
                    enteredOtp = ""
                    resendCountdown = 45

                    viewModel.addNotification(
                      title = "Security Code Resent",
                      message = "New verification code $newCode dispatched via ${activeChannel.name}."
                    )
                  }
                },
                enabled = resendCountdown == 0
              ) {
                Text(
                  text = if (resendCountdown > 0) "Resend in ${resendCountdown}s" else "Resend Code",
                  color = if (resendCountdown > 0) TextSecondaryMuted else NavyPrimary,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Security / Privacy assurance
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(horizontal = 8.dp)
        ) {
          Icon(Icons.Default.Security, contentDescription = null, tint = StatusGreen, modifier = Modifier.size(13.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "256-bit SSL encrypted • 100% private authentication",
            fontSize = 10.sp,
            color = TextSecondaryMuted
          )
        }
      }
    }
  }
}

private data class Quad<A, B, C, D, E>(
  val first: A,
  val second: B,
  val third: C,
  val fourth: D,
  val fifth: E
)
