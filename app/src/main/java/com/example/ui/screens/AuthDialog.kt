package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import com.example.util.PasswordValidator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AuthDialog(
  viewModel: MainViewModel,
  onDismiss: () -> Unit
) {
  val coroutineScope = rememberCoroutineScope()

  // 0: Mobile Number, 1: Email Address
  var contactMode by remember { mutableStateOf(0) }

  var userName by remember { mutableStateOf("") }
  var phoneNumber by remember { mutableStateOf("") }
  var emailInput by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }

  var errorMessage by remember { mutableStateOf<String?>(null) }
  var isLoading by remember { mutableStateOf(false) }

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
                .size(36.dp)
                .clip(CircleShape)
                .background(NavyPrimary.copy(alpha = 0.1f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                Icons.Default.DirectionsCar,
                contentDescription = null,
                tint = NavyPrimary,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "PAK E DRIVE",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = NavyPrimary
              )
              Text(
                text = "Member Sign In / Sign Up",
                fontSize = 11.sp,
                color = TextSecondaryMuted
              )
            }
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(32.dp)
          ) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondaryMuted)
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = "Sign in or create your account to book rental cars across Pakistan with full chauffeur service.",
          fontSize = 12.sp,
          color = TextSecondaryMuted,
          textAlign = TextAlign.Start,
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Full Name Field
        Text(
          text = "Full Name",
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          color = TextPrimaryDark,
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = userName,
          onValueChange = {
            userName = it
            errorMessage = null
          },
          placeholder = { Text("e.g. Mehdi Raza", fontSize = 12.sp, color = TextSecondaryMuted) },
          singleLine = true,
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

        Spacer(modifier = Modifier.height(12.dp))

        // Touchable Switcher: Mobile Phone vs Email Address
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF1F5F9))
            .padding(3.dp)
        ) {
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(8.dp))
              .background(if (contactMode == 0) Color.White else Color.Transparent)
              .clickable {
                contactMode = 0
                errorMessage = null
              }
              .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                Icons.Default.Phone,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = if (contactMode == 0) NavyPrimary else TextSecondaryMuted
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Phone (+92)",
                fontSize = 12.sp,
                fontWeight = if (contactMode == 0) FontWeight.Bold else FontWeight.Medium,
                color = if (contactMode == 0) NavyPrimary else TextSecondaryMuted
              )
            }
          }

          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(8.dp))
              .background(if (contactMode == 1) Color.White else Color.Transparent)
              .clickable {
                contactMode = 1
                errorMessage = null
              }
              .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                Icons.Default.Email,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = if (contactMode == 1) NavyPrimary else TextSecondaryMuted
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Email Address",
                fontSize = 12.sp,
                fontWeight = if (contactMode == 1) FontWeight.Bold else FontWeight.Medium,
                color = if (contactMode == 1) NavyPrimary else TextSecondaryMuted
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Phone or Email Input
        if (contactMode == 0) {
          OutlinedTextField(
            value = phoneNumber,
            onValueChange = {
              phoneNumber = it
              errorMessage = null
            },
            placeholder = { Text("315 2292493", fontSize = 12.sp, color = TextSecondaryMuted) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
            leadingIcon = {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 10.dp, end = 4.dp)
              ) {
                Text("🇵🇰 +92", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
              }
            },
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
        } else {
          OutlinedTextField(
            value = emailInput,
            onValueChange = {
              emailInput = it
              errorMessage = null
            },
            placeholder = { Text("user@gmail.com", fontSize = 12.sp, color = TextSecondaryMuted) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            leadingIcon = {
              Icon(Icons.Default.Email, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(18.dp))
            },
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
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Password Field
        Text(
          text = "Password",
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          color = TextPrimaryDark,
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = password,
          onValueChange = {
            password = it
            errorMessage = null
          },
          placeholder = { Text("Min 8 chars, letters & numbers", fontSize = 12.sp, color = TextSecondaryMuted) },
          singleLine = true,
          visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
          leadingIcon = {
            Icon(Icons.Default.Lock, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(18.dp))
          },
          trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
              Icon(
                if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                tint = TextSecondaryMuted,
                modifier = Modifier.size(18.dp)
              )
            }
          },
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

        val hasLength = password.length >= 8
        val hasAlphaNum = password.any { it.isLetter() } && password.any { it.isDigit() }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            if (hasLength && hasAlphaNum) Icons.Default.CheckCircle else Icons.Default.Info,
            contentDescription = null,
            tint = if (hasLength && hasAlphaNum) StatusGreen else TextSecondaryMuted,
            modifier = Modifier.size(12.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "Requires min 8 alphanumeric characters (letters + numbers)",
            fontSize = 10.5.sp,
            color = if (hasLength && hasAlphaNum) StatusGreen else TextSecondaryMuted
          )
        }

        errorMessage?.let { err ->
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = err,
            color = StatusRed,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth()
          )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Sign In & Continue Button
        Button(
          onClick = {
            if (userName.isBlank()) {
              errorMessage = "Please enter your full name."
              return@Button
            }

            val identifier = if (contactMode == 0) {
              val digits = phoneNumber.filter { it.isDigit() }
              if (digits.length < 7) {
                errorMessage = "Please enter a valid mobile number."
                return@Button
              }
              if (digits.startsWith("0")) "+92 " + digits.substring(1) else "+92 $digits"
            } else {
              if (emailInput.isBlank() || !emailInput.contains("@")) {
                errorMessage = "Please enter a valid email address."
                return@Button
              }
              emailInput.trim()
            }

            val valRes = PasswordValidator.validate(password)
            if (!valRes.isValid) {
              errorMessage = valRes.errorMessage
              return@Button
            }

            errorMessage = null
            isLoading = true

            coroutineScope.launch {
              delay(350)
              viewModel.loginWithPassword(
                identifier = identifier,
                name = userName.trim(),
                isEmail = contactMode == 1
              )
              isLoading = false
              onDismiss()
            }
          },
          enabled = !isLoading,
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
        ) {
          if (isLoading) {
            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Signing In...", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
          } else {
            Icon(Icons.Default.Login, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Sign In & Continue",
              color = Color.White,
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Footer Trust indicators
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceEvenly
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Verified, contentDescription = null, tint = StatusGreen, modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text("Verified Fleet", fontSize = 10.5.sp, color = TextSecondaryMuted)
          }
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Shield, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text("Secure Account", fontSize = 10.5.sp, color = TextSecondaryMuted)
          }
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Headphones, contentDescription = null, tint = OrangeAccent, modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text("24/7 Chauffeur", fontSize = 10.5.sp, color = TextSecondaryMuted)
          }
        }
      }
    }
  }
}
