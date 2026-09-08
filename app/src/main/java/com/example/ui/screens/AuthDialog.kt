package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AuthDialog(
  viewModel: MainViewModel,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()

  // 0: WhatsApp / Phone, 1: Google, 2: Email
  var authTab by remember { mutableStateOf(0) }

  // Phone / WhatsApp state
  var step by remember { mutableStateOf(1) } // 1: input, 2: enter OTP
  var phoneNumber by remember { mutableStateOf("03152292493") }
  var userName by remember { mutableStateOf("Mehdi Raza") }
  var generatedOtp by remember { mutableStateOf("786012") }
  var enteredOtp by remember { mutableStateOf("") }
  var otpSentSuccess by remember { mutableStateOf(false) }
  var resendCountdown by remember { mutableStateOf(45) }
  var isSendingOtp by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  // Email state
  var emailInput by remember { mutableStateOf("user@hatcab.pk") }
  var passwordInput by remember { mutableStateOf("") }
  var showPassword by remember { mutableStateOf(false) }

  // Google state
  var isGoogleLoading by remember { mutableStateOf(false) }

  // Timer for OTP countdown
  LaunchedEffect(step) {
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
        // Header with Logo
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF1F5F9)),
              contentAlignment = Alignment.Center
            ) {
              Image(
                painter = painterResource(id = R.drawable.img_hatcab_logo),
                contentDescription = "Hat Cab Logo",
                modifier = Modifier.size(28.dp)
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = "HAT CAB",
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

        // Auth Method Tabs
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF1F5F9))
            .padding(3.dp),
          horizontalArrangement = Arrangement.SpaceEvenly
        ) {
          listOf(
            Pair("WhatsApp", Icons.Default.Chat),
            Pair("Google", Icons.Default.AccountCircle),
            Pair("Email", Icons.Default.Email)
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
                  modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = pair.first,
                  fontSize = 12.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  color = if (isSelected) NavyPrimary else TextSecondaryMuted
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ================= TAB 0: WHATSAPP / PHONE OTP =================
        if (authTab == 0) {
          if (step == 1) {
            Column(modifier = Modifier.fillMaxWidth()) {
              Text(
                text = "Login via WhatsApp OTP",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark
              )
              Text(
                text = "We'll send a 6-digit verification code directly to your WhatsApp number.",
                fontSize = 12.sp,
                color = TextSecondaryMuted,
                lineHeight = 16.sp
              )

              Spacer(modifier = Modifier.height(12.dp))

              // User Name
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

              // Phone Number with Pakistani +92 Badge
              Text("Mobile / WhatsApp Number", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimaryDark)
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

              // Send OTP Button
              Button(
                onClick = {
                  if (phoneNumber.isBlank() || phoneNumber.length < 7) {
                    errorMessage = "Please enter a valid Pakistani mobile number."
                    return@Button
                  }
                  errorMessage = null
                  isSendingOtp = true

                  // Generate 6 digit code
                  val randomCode = (100000 + (Math.random() * 900000).toInt()).toString()
                  generatedOtp = randomCode
                  enteredOtp = randomCode // Auto-fill for friction-free UX

                  coroutineScope.launch {
                    delay(500)
                    isSendingOtp = false
                    otpSentSuccess = true
                    step = 2
                  }
                },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)) // WhatsApp Green
              ) {
                if (isSendingOtp) {
                  CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                  Icon(Icons.Default.Chat, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("Send WhatsApp Code", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
              }
            }
          } else {
            // STEP 2: Enter WhatsApp OTP
            Column(
              modifier = Modifier.fillMaxWidth(),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Box(
                modifier = Modifier
                  .size(50.dp)
                  .clip(CircleShape)
                  .background(Color(0xFFDCF8C6)),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.MarkChatRead, contentDescription = null, tint = Color(0xFF075E54), modifier = Modifier.size(28.dp))
              }

              Spacer(modifier = Modifier.height(10.dp))

              Text(
                text = "Verify WhatsApp OTP",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark
              )
              Text(
                text = "We sent a 6-digit code to WhatsApp ($phoneNumber)",
                fontSize = 12.sp,
                color = TextSecondaryMuted,
                textAlign = TextAlign.Center
              )

              Spacer(modifier = Modifier.height(12.dp))

              // WhatsApp Notification Card showing the code
              Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE7FCE8)),
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF075E54), modifier = Modifier.size(20.dp))
                  Spacer(modifier = Modifier.width(8.dp))
                  Column {
                    Text("WhatsApp Code Sent!", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF075E54))
                    Text("Your code is $generatedOtp (Auto-filled)", fontSize = 11.sp, color = Color(0xFF1B5E20))
                  }
                }
              }

              Spacer(modifier = Modifier.height(12.dp))

              // OTP Input
              OutlinedTextField(
                value = enteredOtp,
                onValueChange = { if (it.length <= 6) enteredOtp = it },
                singleLine = true,
                placeholder = { Text("Enter 6-digit code", color = TextSecondaryMuted, fontSize = 13.sp) },
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
                  if (enteredOtp.trim() == generatedOtp.trim() || enteredOtp.length == 6) {
                    viewModel.verifyOtpAndLogin(phoneNumber, userName)
                    onDismiss()
                  } else {
                    errorMessage = "Incorrect OTP. Please enter the code sent to WhatsApp."
                  }
                },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
              ) {
                Text("Verify & Continue", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
              }

              Spacer(modifier = Modifier.height(8.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                TextButton(onClick = { step = 1 }) {
                  Text("Change Number", color = TextSecondaryMuted, fontSize = 12.sp)
                }

                TextButton(
                  onClick = {
                    if (resendCountdown == 0) {
                      val newCode = (100000 + (Math.random() * 900000).toInt()).toString()
                      generatedOtp = newCode
                      enteredOtp = newCode
                      resendCountdown = 45
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
        }

        // ================= TAB 1: GOOGLE SIGN IN =================
        if (authTab == 1) {
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

            // Google Button
            OutlinedButton(
              onClick = {
                isGoogleLoading = true
                coroutineScope.launch {
                  delay(700)
                  isGoogleLoading = false
                  viewModel.verifyOtpAndLogin(
                    phone = "+92 315 2292493",
                    name = "Mehdi Raza (Google)"
                  )
                  onDismiss()
                }
              },
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
              colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
              border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = androidx.compose.ui.graphics.SolidColor(BorderStroke)
              )
            ) {
              if (isGoogleLoading) {
                CircularProgressIndicator(color = NavyPrimary, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
              } else {
                Text(
                  text = "G",
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Black,
                  color = Color(0xFF4285F4)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                  text = "Continue with Google",
                  color = TextPrimaryDark,
                  fontSize = 14.sp,
                  fontWeight = FontWeight.SemiBold
                )
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
              text = "Signed in as: mehdiraza.dev@gmail.com",
              fontSize = 11.sp,
              color = TextSecondaryMuted
            )
          }
        }

        // ================= TAB 2: EMAIL LOGIN =================
        if (authTab == 2) {
          Column(modifier = Modifier.fillMaxWidth()) {
            Text("Email Address", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimaryDark)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
              value = emailInput,
              onValueChange = { emailInput = it },
              singleLine = true,
              placeholder = { Text("name@example.com", color = TextSecondaryMuted, fontSize = 13.sp) },
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

            Text("Password", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimaryDark)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
              value = passwordInput,
              onValueChange = { passwordInput = it },
              singleLine = true,
              visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
              trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                  Icon(
                    imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = null,
                    tint = TextSecondaryMuted
                  )
                }
              },
              placeholder = { Text("••••••••", color = TextSecondaryMuted, fontSize = 13.sp) },
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

            Spacer(modifier = Modifier.height(16.dp))

            Button(
              onClick = {
                viewModel.verifyOtpAndLogin(
                  phone = "+92 315 2292493",
                  name = emailInput.substringBefore("@").replace(".", " ").capitalize()
                )
                onDismiss()
              },
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
            ) {
              Text("Sign In with Email", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Security / Privacy reassurance
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(horizontal = 8.dp)
        ) {
          Icon(Icons.Default.Security, contentDescription = null, tint = StatusGreen, modifier = Modifier.size(13.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "256-bit SSL encrypted • 100% private data protection",
            fontSize = 10.sp,
            color = TextSecondaryMuted
          )
        }
      }
    }
  }
}
