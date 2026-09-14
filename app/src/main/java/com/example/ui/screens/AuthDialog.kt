package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.example.util.CnicValidator
import com.example.util.PasswordValidator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Dedicated Client / Passenger Sign-Up & Sign-In Dialog.
 * Exclusively collects: Full Name, Email, Phone Number, NADRA CNIC, and Password.
 * Does NOT require driving licenses or vehicle details (reserved for Driver Partners).
 */
@Composable
fun AuthDialog(
  viewModel: MainViewModel,
  onDismiss: () -> Unit
) {
  val coroutineScope = rememberCoroutineScope()
  val scrollState = rememberScrollState()

  // Mode: 0 = Sign Up (New Client), 1 = Sign In (Existing Client)
  var authMode by remember { mutableStateOf(0) }

  var userName by remember { mutableStateOf("") }
  var emailInput by remember { mutableStateOf("") }
  var phoneNumber by remember { mutableStateOf("") }
  var cnicInput by remember { mutableStateOf("") }
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
        .padding(horizontal = 4.dp, vertical = 16.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(scrollState)
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
                .clip(CircleShape)
                .background(Color(0xFFF1F5F9))
                .border(1.dp, Color(0xFFE2E8F0), CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                Icons.Default.Person,
                contentDescription = null,
                tint = Color(0xFF111827),
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "PAK E DRIVE",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF111827)
              )
              Text(
                text = if (authMode == 0) "Client & Passenger Sign Up" else "Client Sign In",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF6B7280)
              )
            }
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(32.dp)
          ) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF6B7280))
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Mode Switcher: Sign Up vs Sign In
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF3F4F6))
            .padding(3.dp)
        ) {
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(8.dp))
              .background(if (authMode == 0) Color.White else Color.Transparent)
              .clickable {
                authMode = 0
                errorMessage = null
              }
              .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "Sign Up (New)",
              fontSize = 12.sp,
              fontWeight = if (authMode == 0) FontWeight.Bold else FontWeight.Medium,
              color = if (authMode == 0) Color(0xFF111827) else Color(0xFF6B7280)
            )
          }

          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(8.dp))
              .background(if (authMode == 1) Color.White else Color.Transparent)
              .clickable {
                authMode = 1
                errorMessage = null
              }
              .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "Sign In",
              fontSize = 12.sp,
              fontWeight = if (authMode == 1) FontWeight.Bold else FontWeight.Medium,
              color = if (authMode == 1) Color(0xFF111827) else Color(0xFF6B7280)
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = if (authMode == 0)
            "Register as a passenger to book chauffeur-driven rental cars across Pakistan."
          else
            "Enter your email or phone and password to access your bookings.",
          fontSize = 11.5.sp,
          color = Color(0xFF4B5563),
          textAlign = TextAlign.Start,
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 1. Full Name Field (Always for Sign Up)
        if (authMode == 0) {
          Text(
            text = "Full Name",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF111827),
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(4.dp))
          OutlinedTextField(
            value = userName,
            onValueChange = {
              userName = it
              errorMessage = null
            },
            placeholder = { Text("e.g. Muhammad Raza", fontSize = 12.sp, color = Color(0xFF9CA3AF)) },
            leadingIcon = {
              Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF111827), modifier = Modifier.size(18.dp))
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = Color(0xFF111827),
              unfocusedTextColor = Color(0xFF111827),
              cursorColor = Color(0xFF111827),
              focusedContainerColor = Color.White,
              unfocusedContainerColor = Color.White,
              focusedBorderColor = Color(0xFF111827),
              unfocusedBorderColor = Color(0xFFE5E7EB)
            )
          )

          Spacer(modifier = Modifier.height(10.dp))
        }

        // 2. Email Address Field
        Text(
          text = "Email Address",
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          color = Color(0xFF111827),
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = emailInput,
          onValueChange = {
            emailInput = it
            errorMessage = null
          },
          placeholder = { Text("client@gmail.com", fontSize = 12.sp, color = Color(0xFF9CA3AF)) },
          leadingIcon = {
            Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF111827), modifier = Modifier.size(18.dp))
          },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color(0xFF111827),
            unfocusedTextColor = Color(0xFF111827),
            cursorColor = Color(0xFF111827),
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = Color(0xFF111827),
            unfocusedBorderColor = Color(0xFFE5E7EB)
          )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Phone Number Field (+92)
        Text(
          text = "Phone Number (+92)",
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          color = Color(0xFF111827),
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = phoneNumber,
          onValueChange = {
            phoneNumber = it
            errorMessage = null
          },
          placeholder = { Text("315 2292493", fontSize = 12.sp, color = Color(0xFF9CA3AF)) },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
          leadingIcon = {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(start = 10.dp, end = 4.dp)
            ) {
              Text("🇵🇰 +92", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
            }
          },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color(0xFF111827),
            unfocusedTextColor = Color(0xFF111827),
            cursorColor = Color(0xFF111827),
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = Color(0xFF111827),
            unfocusedBorderColor = Color(0xFFE5E7EB)
          )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 4. NADRA CNIC Number (13 Digits)
        Text(
          text = "NADRA CNIC Number (13 Digits)",
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          color = Color(0xFF111827),
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = cnicInput,
          onValueChange = {
            cnicInput = CnicValidator.formatCnic(it)
            errorMessage = null
          },
          placeholder = { Text("42101-1234567-1", fontSize = 12.sp, color = Color(0xFF9CA3AF)) },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
          leadingIcon = {
            Icon(Icons.Default.Badge, contentDescription = null, tint = Color(0xFF111827), modifier = Modifier.size(18.dp))
          },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color(0xFF111827),
            unfocusedTextColor = Color(0xFF111827),
            cursorColor = Color(0xFF111827),
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = Color(0xFF111827),
            unfocusedBorderColor = Color(0xFFE5E7EB)
          )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 5. Password Field
        Text(
          text = "Password",
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          color = Color(0xFF111827),
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = password,
          onValueChange = {
            password = it
            errorMessage = null
          },
          placeholder = { Text("Min 8 characters (letters & numbers)", fontSize = 12.sp, color = Color(0xFF9CA3AF)) },
          singleLine = true,
          visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
          leadingIcon = {
            Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF111827), modifier = Modifier.size(18.dp))
          },
          trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
              Icon(
                if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                tint = Color(0xFF6B7280),
                modifier = Modifier.size(18.dp)
              )
            }
          },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color(0xFF111827),
            unfocusedTextColor = Color(0xFF111827),
            cursorColor = Color(0xFF111827),
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = Color(0xFF111827),
            unfocusedBorderColor = Color(0xFFE5E7EB)
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
            tint = if (hasLength && hasAlphaNum) Color(0xFF15803D) else Color(0xFF6B7280),
            modifier = Modifier.size(12.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "Requires min 8 alphanumeric characters",
            fontSize = 10.5.sp,
            color = if (hasLength && hasAlphaNum) Color(0xFF15803D) else Color(0xFF6B7280)
          )
        }

        errorMessage?.let { err ->
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = err,
            color = Color(0xFFB91C1C),
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth()
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Submit Button (Client Account)
        Button(
          onClick = {
            if (authMode == 0 && userName.isBlank()) {
              errorMessage = "Please enter your full name."
              return@Button
            }

            if (emailInput.isBlank() || !emailInput.contains("@") || !emailInput.contains(".")) {
              errorMessage = "Please enter a valid email address."
              return@Button
            }

            val phoneDigits = phoneNumber.filter { it.isDigit() }
            if (phoneDigits.length < 7) {
              errorMessage = "Please enter a valid Pakistani mobile number."
              return@Button
            }

            val cnicCheck = CnicValidator.validate(cnicInput)
            if (!cnicCheck.isValid) {
              errorMessage = cnicCheck.errorMessage ?: "Valid 13-digit NADRA CNIC is required."
              return@Button
            }

            val valRes = PasswordValidator.validate(password)
            if (!valRes.isValid) {
              errorMessage = valRes.errorMessage
              return@Button
            }

            errorMessage = null
            isLoading = true

            val formattedPhone = if (phoneDigits.startsWith("0")) "+92 " + phoneDigits.substring(1) else "+92 $phoneDigits"
            val formattedCnic = CnicValidator.formatCnic(cnicInput)

            coroutineScope.launch {
              delay(350)
              if (authMode == 0) {
                viewModel.registerClient(
                  name = userName.trim(),
                  phone = formattedPhone,
                  email = emailInput.trim(),
                  cnic = formattedCnic
                )
              } else {
                viewModel.loginWithPassword(
                  identifier = emailInput.trim(),
                  name = userName.ifBlank { emailInput.substringBefore("@").replaceFirstChar { it.uppercase() } },
                  isEmail = true,
                  cnic = formattedCnic
                )
              }
              isLoading = false
              onDismiss()
            }
          },
          enabled = !isLoading,
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF111827)),
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
        ) {
          if (isLoading) {
            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Processing...", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
          } else {
            Icon(if (authMode == 0) Icons.Default.PersonAdd else Icons.Default.Login, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (authMode == 0) "Create Client Account" else "Sign In as Client",
              color = Color.White,
              fontSize = 14.5.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Divert to Driver Partner Portal Card
        Card(
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
          modifier = Modifier
            .fillMaxWidth()
            .clickable {
              onDismiss()
              viewModel.setShowDriverPartnerDialog(true)
            }
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
              Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = Color(0xFF111827), modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text("Are you a Driver or Fleet Owner?", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                Text("Requires Driving License & NADRA check", fontSize = 10.sp, color = Color(0xFF6B7280))
              }
            }
            Text("Register as Driver →", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
          }
        }
      }
    }
  }
}
