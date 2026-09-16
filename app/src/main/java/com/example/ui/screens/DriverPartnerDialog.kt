package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.pomo.mypomo.R
import com.example.data.model.Car
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import com.example.util.CnicValidator
import com.example.util.DlimsVerificationResult
import com.example.util.NadraVerificationResult
import com.example.util.PasswordValidator
import java.util.UUID
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class VehiclePhotoPreset(
  val label: String,
  val resId: Int
)

@Composable
fun DriverPartnerDialog(
  viewModel: MainViewModel,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val profile by viewModel.userProfile.collectAsState()
  val cities = viewModel.cities.filter { it != "All Cities" }

  // Driver Personal Details
  var driverName by remember { mutableStateOf(if (profile.isDriverPartner) profile.name else "") }
  var driverPhone by remember { mutableStateOf(if (profile.isDriverPartner) profile.phone.removePrefix("+92 ") else "") }
  var driverEmail by remember { mutableStateOf(if (profile.isDriverPartner) profile.email else "") }
  var driverCnic by remember { mutableStateOf(if (profile.isDriverPartner) profile.cnic else "") }
  var licenseNumber by remember { mutableStateOf(if (profile.isDriverPartner) profile.driverLicenseNumber else "") }
  var driverPassword by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }

  // Car Details
  var carName by remember { mutableStateOf("") }
  var carVariant by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf("Sedan") }
  var selectedCity by remember { mutableStateOf(cities.firstOrNull() ?: "Karachi") }
  var showCityDropdown by remember { mutableStateOf(false) }
  var plateNumber by remember { mutableStateOf("") }
  var tenHourRateText by remember { mutableStateOf("7000") }
  var dailyRateText by remember { mutableStateOf("9000") }
  var selectedTransmission by remember { mutableStateOf("Automatic") }
  var selectedFuel by remember { mutableStateOf("Petrol") }
  var seatsCount by remember { mutableStateOf("5") }

  // Photo Presets
  val presets = listOf(
    VehiclePhotoPreset("Corolla Altis", R.drawable.car_corolla_altis),
    VehiclePhotoPreset("Civic RS Turbo", R.drawable.car_civic_turbo),
    VehiclePhotoPreset("Changan Oshan X7", R.drawable.car_changan_x7_black),
    VehiclePhotoPreset("Fortuner 4x4", R.drawable.car_fortuner),
    VehiclePhotoPreset("Toyota Prado", R.drawable.car_toyota_prado),
    VehiclePhotoPreset("HiAce Grand Cabin", R.drawable.car_hiace_cabin),
    VehiclePhotoPreset("Honda BR-V 7-Seater", R.drawable.car_honda_brv),
    VehiclePhotoPreset("Changan Karvaan", R.drawable.car_changan_karvaan),
    VehiclePhotoPreset("Suzuki Alto", R.drawable.car_suzuki_alto),
    VehiclePhotoPreset("Toyota Yaris", R.drawable.car_toyota_yaris),
    VehiclePhotoPreset("Toyota Revo B6", R.drawable.car_revo_b6),
    VehiclePhotoPreset("Audi Luxury", R.drawable.car_audi_wedding)
  )

  var selectedImageRes by remember { mutableStateOf(presets.first().resId) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var isSubmitting by remember { mutableStateOf(false) }

  val coroutineScope = rememberCoroutineScope()
  var isVerifyingNadra by remember { mutableStateOf(false) }
  var nadraResult by remember { mutableStateOf<NadraVerificationResult?>(null) }
  var isVerifyingLicense by remember { mutableStateOf(false) }
  var dlimsResult by remember { mutableStateOf<DlimsVerificationResult?>(null) }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Card(
      shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(0.96f)
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
      ) {
        // Header (Realistic Off-White, Compact Brand Identity)
        Surface(
          color = Color(0xFFF8F9FA),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(32.dp)
                  .clip(CircleShape)
                  .background(Color(0xFF0F172A)),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.DriveEta, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "Driver Partner & Car Listing",
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF111827)
                )
                Text(
                  text = "NADRA & DLIMS Verified Fleet Portal",
                  fontSize = 11.sp,
                  color = Color(0xFF4B5563)
                )
              }
            }

            IconButton(
              onClick = onDismiss,
              modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(Color(0xFFE5E7EB))
            ) {
              Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF111827), modifier = Modifier.size(16.dp))
            }
          }
        }

        Column(modifier = Modifier.padding(16.dp)) {
          // Banner
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = StatusGreen, modifier = Modifier.size(24.dp))
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text("Join PAK E DRIVE Fleet Network", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF14532D))
                Text("Earn guaranteed daily income with your car and commercial chauffeur services across all Pakistani highways.", fontSize = 11.sp, color = Color(0xFF166534))
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // SECTION 1: DRIVER CREDENTIALS
          Text("1. Driver Partner Credentials", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
          Spacer(modifier = Modifier.height(8.dp))

          // Driver Name
          OutlinedTextField(
            value = driverName,
            onValueChange = {
              driverName = it
              errorMessage = null
            },
            label = { Text("Driver Full Name", fontSize = 11.sp) },
            placeholder = { Text("e.g. Muhammad Aslam / Tariq Mehmood", fontSize = 12.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
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

          Spacer(modifier = Modifier.height(8.dp))

          // CNIC MANDATORY & NADRA VERIFICATION
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedTextField(
              value = driverCnic,
              onValueChange = {
                driverCnic = CnicValidator.formatCnic(it)
                nadraResult = null
                errorMessage = null
              },
              label = { Text("NADRA CNIC Number (13 Digits)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
              placeholder = { Text("42101-1234567-1", fontSize = 12.sp) },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
              leadingIcon = {
                Icon(Icons.Default.Badge, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(18.dp))
              },
              singleLine = true,
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(8.dp),
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

            Button(
              onClick = {
                if (driverCnic.filter { it.isDigit() }.length != 13) {
                  errorMessage = "Please enter complete 13-digit Pakistani CNIC first."
                  return@Button
                }
                isVerifyingNadra = true
                coroutineScope.launch {
                  delay(750)
                  nadraResult = CnicValidator.verifyWithNadraPakId(driverCnic, driverName)
                  isVerifyingNadra = false
                }
              },
              enabled = !isVerifyingNadra && driverCnic.filter { it.isDigit() }.length == 13,
              shape = RoundedCornerShape(8.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF111827),
                disabledContainerColor = Color(0xFFE5E7EB)
              ),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
              modifier = Modifier.height(52.dp)
            ) {
              if (isVerifyingNadra) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
              } else {
                Text(
                  text = if (nadraResult?.isVerified == true) "Verified ✓" else "Verify NADRA",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (nadraResult?.isVerified == true) Color(0xFF86EFAC) else Color.White
                )
              }
            }
          }

          // NADRA Verification Status Banner
          nadraResult?.let { result ->
            Spacer(modifier = Modifier.height(6.dp))
            Card(
              shape = RoundedCornerShape(8.dp),
              colors = CardDefaults.cardColors(
                containerColor = if (result.isVerified) Color(0xFFF0FDF4) else Color(0xFFFEF2F2)
              ),
              border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (result.isVerified) Color(0xFF86EFAC) else Color(0xFFFCA5A5)
              ),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = if (result.isVerified) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                  contentDescription = null,
                  tint = if (result.isVerified) Color(0xFF16A34A) else Color(0xFFDC2626),
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                  Text(
                    text = if (result.isVerified) "NADRA Pak-ID Verisys Authenticated" else "NADRA Verification Rejected",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (result.isVerified) Color(0xFF15803D) else Color(0xFFB91C1C)
                  )
                  Text(
                    text = if (result.isVerified)
                      "Citizen: ${result.citizenName} • Region: ${result.provinceName ?: "Pakistan"} (${result.issuingDistrict ?: "Verified"}) • Status: Active Record"
                    else
                      result.errorMessage ?: "NADRA Pak-ID verification failed",
                    fontSize = 10.sp,
                    color = if (result.isVerified) Color(0xFF166534) else Color(0xFF991B1B)
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Phone & Email in a row
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
              value = driverPhone,
              onValueChange = {
                driverPhone = it
                errorMessage = null
              },
              label = { Text("WhatsApp Phone", fontSize = 11.sp) },
              placeholder = { Text("315 2292493", fontSize = 12.sp) },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
              leadingIcon = { Text("🇵🇰 +92", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NavyPrimary, modifier = Modifier.padding(start = 6.dp)) },
              singleLine = true,
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(8.dp),
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

            OutlinedTextField(
              value = driverEmail,
              onValueChange = {
                driverEmail = it
                errorMessage = null
              },
              label = { Text("Email Address", fontSize = 11.sp) },
              placeholder = { Text("driver@gmail.com", fontSize = 12.sp) },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
              singleLine = true,
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(8.dp),
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

          Spacer(modifier = Modifier.height(8.dp))

          // Driving License & DLIMS Verification Button
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedTextField(
              value = licenseNumber,
              onValueChange = {
                licenseNumber = it
                dlimsResult = null
                errorMessage = null
              },
              label = { Text("Driving License #", fontSize = 11.sp) },
              placeholder = { Text("KHI-DL-7861", fontSize = 12.sp) },
              singleLine = true,
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(8.dp),
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

            Button(
              onClick = {
                if (licenseNumber.isBlank()) {
                  errorMessage = "Please enter driving license number."
                  return@Button
                }
                isVerifyingLicense = true
                coroutineScope.launch {
                  delay(750)
                  dlimsResult = CnicValidator.verifyDrivingLicenseWithDlims(licenseNumber, driverCnic)
                  isVerifyingLicense = false
                }
              },
              enabled = !isVerifyingLicense && licenseNumber.isNotBlank(),
              shape = RoundedCornerShape(8.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF111827),
                disabledContainerColor = Color(0xFFE5E7EB)
              ),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
              modifier = Modifier.height(52.dp)
            ) {
              if (isVerifyingLicense) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
              } else {
                Text(
                  text = if (dlimsResult?.isVerified == true) "Verified ✓" else "Verify DLIMS",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (dlimsResult?.isVerified == true) Color(0xFF86EFAC) else Color.White
                )
              }
            }
          }

          // DLIMS Verification Status Banner
          dlimsResult?.let { dlims ->
            Spacer(modifier = Modifier.height(6.dp))
            Card(
              shape = RoundedCornerShape(8.dp),
              colors = CardDefaults.cardColors(
                containerColor = if (dlims.isVerified) Color(0xFFF0FDF4) else Color(0xFFFEF2F2)
              ),
              border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (dlims.isVerified) Color(0xFF86EFAC) else Color(0xFFFCA5A5)
              ),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = if (dlims.isVerified) Icons.Default.Verified else Icons.Default.Cancel,
                  contentDescription = null,
                  tint = if (dlims.isVerified) Color(0xFF16A34A) else Color(0xFFDC2626),
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                  Text(
                    text = if (dlims.isVerified) "DLIMS Traffic Police Endorsed" else "DLIMS Verification Incomplete",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (dlims.isVerified) Color(0xFF15803D) else Color(0xFFB91C1C)
                  )
                  Text(
                    text = if (dlims.isVerified)
                      "Authority: ${dlims.issuingAuthority} • Category: ${dlims.category} • Valid Chauffeur"
                    else
                      dlims.errorMessage ?: "DLIMS driving license verification failed",
                    fontSize = 10.sp,
                    color = if (dlims.isVerified) Color(0xFF166534) else Color(0xFF991B1B)
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Driver Password
          OutlinedTextField(
            value = driverPassword,
            onValueChange = {
              driverPassword = it
              errorMessage = null
            },
            label = { Text("Account Password", fontSize = 11.sp) },
            placeholder = { Text("Min 8 alphanumeric characters", fontSize = 11.sp) },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
            trailingIcon = {
              IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                  if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp),
                  tint = TextSecondaryMuted
                )
              }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
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

          Spacer(modifier = Modifier.height(18.dp))

          // SECTION 2: CAR INFORMATION & PRICING
          Text("2. Vehicle Information & Custom Rental Rates", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
          Spacer(modifier = Modifier.height(8.dp))

          // Car Name & Variant
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
              value = carName,
              onValueChange = {
                carName = it
                errorMessage = null
              },
              label = { Text("Car Make & Model", fontSize = 11.sp) },
              placeholder = { Text("e.g. Toyota Corolla / Fortuner", fontSize = 11.5.sp) },
              singleLine = true,
              modifier = Modifier.weight(1.2f),
              shape = RoundedCornerShape(8.dp),
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

            OutlinedTextField(
              value = carVariant,
              onValueChange = {
                carVariant = it
                errorMessage = null
              },
              label = { Text("Variant / Model", fontSize = 11.sp) },
              placeholder = { Text("Altis Grande 1.8", fontSize = 11.5.sp) },
              singleLine = true,
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(8.dp),
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

          Spacer(modifier = Modifier.height(8.dp))

          // Category Chips
          Text("Vehicle Category", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = TextPrimaryDark)
          Spacer(modifier = Modifier.height(4.dp))
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            listOf("Sedan", "SUV / 4x4", "Van / Hiace", "Economy", "Luxury Wedding").forEach { cat ->
              FilterChip(
                selected = selectedCategory == cat,
                onClick = { selectedCategory = cat },
                label = { Text(cat, fontSize = 11.5.sp) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = NavyPrimary,
                  selectedLabelColor = Color.White,
                  containerColor = Color.White,
                  labelColor = TextPrimaryDark
                )
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Base City Selector & Plate Number
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.weight(1f)) {
              OutlinedButton(
                onClick = { showCityDropdown = true },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyPrimary),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 12.dp)
              ) {
                Icon(Icons.Default.LocationCity, contentDescription = null, tint = OrangeAccent, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("City: $selectedCity", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
              }
              DropdownMenu(expanded = showCityDropdown, onDismissRequest = { showCityDropdown = false }) {
                cities.forEach { c ->
                  DropdownMenuItem(
                    text = { Text(c) },
                    onClick = {
                      selectedCity = c
                      showCityDropdown = false
                    }
                  )
                }
              }
            }

            OutlinedTextField(
              value = plateNumber,
              onValueChange = {
                plateNumber = it
                errorMessage = null
              },
              label = { Text("Registration Plate #", fontSize = 11.sp) },
              placeholder = { Text("LED-7860", fontSize = 11.5.sp) },
              singleLine = true,
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(8.dp),
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

          Spacer(modifier = Modifier.height(10.dp))

          // Custom Pricing fields
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
              value = tenHourRateText,
              onValueChange = { tenHourRateText = it.filter { ch -> ch.isDigit() } },
              label = { Text("10-Hour Rate (PKR)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
              placeholder = { Text("7000", fontSize = 12.sp) },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
              singleLine = true,
              leadingIcon = { Text("Rs.", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OrangeAccent, modifier = Modifier.padding(start = 6.dp)) },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(8.dp),
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

            OutlinedTextField(
              value = dailyRateText,
              onValueChange = { dailyRateText = it.filter { ch -> ch.isDigit() } },
              label = { Text("24-Hour Day Rate (PKR)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
              placeholder = { Text("9000", fontSize = 12.sp) },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
              singleLine = true,
              leadingIcon = { Text("Rs.", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OrangeAccent, modifier = Modifier.padding(start = 6.dp)) },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(8.dp),
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

          Spacer(modifier = Modifier.height(10.dp))

          // Transmission, Fuel, Seats in row
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Transmission
            Box(modifier = Modifier.weight(1f)) {
              OutlinedButton(
                onClick = { selectedTransmission = if (selectedTransmission == "Automatic") "Manual" else "Automatic" },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyPrimary),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 10.dp)
              ) {
                Text(selectedTransmission, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
              }
            }

            // Fuel
            Box(modifier = Modifier.weight(1f)) {
              OutlinedButton(
                onClick = {
                  selectedFuel = when (selectedFuel) {
                    "Petrol" -> "Hybrid"
                    "Hybrid" -> "Diesel"
                    else -> "Petrol"
                  }
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyPrimary),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 10.dp)
              ) {
                Text(selectedFuel, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
              }
            }

            // Seats
            OutlinedTextField(
              value = seatsCount,
              onValueChange = { seatsCount = it.take(2).filter { c -> c.isDigit() } },
              label = { Text("Seats", fontSize = 10.5.sp) },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
              singleLine = true,
              modifier = Modifier.weight(0.7f),
              shape = RoundedCornerShape(8.dp),
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

          Spacer(modifier = Modifier.height(14.dp))

          // Photo Selection (Vehicle Presets)
          Text("3. Choose Vehicle Photo Preset", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
          Spacer(modifier = Modifier.height(6.dp))

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            presets.forEach { preset ->
              val isSelected = selectedImageRes == preset.resId
              Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = if (isSelected) NavyPrimary.copy(alpha = 0.08f) else Color.White),
                border = androidx.compose.foundation.BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) NavyPrimary else BorderStroke),
                modifier = Modifier
                  .width(115.dp)
                  .clickable { selectedImageRes = preset.resId }
              ) {
                Column(
                  modifier = Modifier.padding(6.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Image(
                    painter = painterResource(id = preset.resId),
                    contentDescription = preset.label,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(65.dp)
                      .clip(RoundedCornerShape(6.dp))
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = preset.label,
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) NavyPrimary else TextPrimaryDark,
                    maxLines = 1
                  )
                }
              }
            }
          }

          errorMessage?.let { err ->
            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = err,
              color = StatusRed,
              fontSize = 11.5.sp,
              fontWeight = FontWeight.SemiBold,
              modifier = Modifier.fillMaxWidth()
            )
          }

          Spacer(modifier = Modifier.height(18.dp))

          // Submit Button
          Button(
            onClick = {
              if (driverName.isBlank()) {
                errorMessage = "Please enter driver full name."
                return@Button
              }

              val cnicCheck = CnicValidator.validate(driverCnic)
              if (!cnicCheck.isValid) {
                errorMessage = cnicCheck.errorMessage ?: "Valid NADRA CNIC required (13 digits)."
                return@Button
              }

              val phoneDigits = driverPhone.filter { it.isDigit() }
              if (phoneDigits.length < 7) {
                errorMessage = "Please enter valid phone number."
                return@Button
              }

              if (carName.isBlank()) {
                errorMessage = "Please enter car make and model."
                return@Button
              }

              val tenHr = tenHourRateText.toIntOrNull() ?: 7000
              val daily = dailyRateText.toIntOrNull() ?: 9000

              if (nadraResult?.isVerified != true) {
                errorMessage = "NADRA Pak-ID Verisys verification is mandatory. Please tap 'Verify NADRA' to authenticate your citizen CNIC before registration."
                return@Button
              }

              if (dlimsResult?.isVerified != true) {
                errorMessage = "DLIMS Driving License verification is mandatory. Please tap 'Verify DLIMS' to authenticate your driver license before registration."
                return@Button
              }

              if (driverPassword.isNotBlank()) {
                val passCheck = PasswordValidator.validate(driverPassword)
                if (!passCheck.isValid) {
                  errorMessage = passCheck.errorMessage
                  return@Button
                }
              }

              errorMessage = null
              isSubmitting = true

              val fullPhone = if (phoneDigits.startsWith("0")) "+92 ${phoneDigits.substring(1)}" else "+92 $phoneDigits"

              // Register Driver Partner Profile with verified government credentials
              viewModel.registerDriverPartner(
                name = driverName.trim(),
                phone = fullPhone,
                email = driverEmail.trim().ifBlank { "partner@pakedrive.pk" },
                cnic = CnicValidator.formatCnic(driverCnic),
                licenseNumber = licenseNumber.trim().ifBlank { "VERIFIED-DL" },
                isNadraVerified = nadraResult?.isVerified ?: true,
                isLicenseVerified = dlimsResult?.isVerified ?: true,
                licenseAuthority = dlimsResult?.issuingAuthority ?: "DLIMS Traffic Police"
              )

              // Create & Add Car to Fleet
              val newCar = Car(
                id = "partner_${UUID.randomUUID().toString().take(8)}",
                name = carName.trim(),
                variant = carVariant.trim().ifBlank { "Chauffeur Edition" },
                make = carName.split(" ").firstOrNull() ?: "Toyota",
                category = selectedCategory,
                dailyRate = daily,
                tenHourRate = tenHr,
                priceDisplay = "Rs. ${String.format("%,d", tenHr)} / 10h",
                rateType = "/ 10h",
                routeSnippet = "$selectedCity • Partner Fleet",
                fromCity = selectedCity,
                toCity = "Intercity / Local",
                imageRes = selectedImageRes,
                engineSpec = "$selectedFuel • $selectedTransmission",
                seats = seatsCount.toIntOrNull() ?: 5,
                transmission = selectedTransmission,
                fuelType = selectedFuel,
                serviceType = "With Driver",
                isFeatured = true,
                rating = 5.0,
                reviewCount = 1,
                description = "Verified Partner Vehicle with Chauffeur. Plate: ${plateNumber.ifBlank { "PED-786" }}. Driver: ${driverName.trim()} (CNIC: ${CnicValidator.formatCnic(driverCnic)}).",
                isPartnerCar = true,
                partnerDriverName = driverName.trim(),
                partnerPhone = fullPhone,
                partnerCnic = CnicValidator.formatCnic(driverCnic)
              )

              viewModel.addPartnerCar(newCar)
              isSubmitting = false
              Toast.makeText(context, "Success! Your car has been listed on PAK E DRIVE fleet.", Toast.LENGTH_LONG).show()
              onDismiss()
            },
            enabled = !isSubmitting,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF111827)),
            modifier = Modifier
              .fillMaxWidth()
              .height(52.dp)
          ) {
            Icon(Icons.Default.AddBusiness, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Register & List Car on PAK E DRIVE",
              color = Color.White,
              fontSize = 14.5.sp,
              fontWeight = FontWeight.Bold
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Divert to Client Account
          Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier
              .fillMaxWidth()
              .clickable {
                onDismiss()
                viewModel.setShowAuthDialog(true)
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
                Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF111827), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                  Text("Looking to rent or book a car as a Client?", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                  Text("Only Name, Email, Phone, CNIC & Password needed", fontSize = 10.sp, color = Color(0xFF6B7280))
                }
              }
              Text("Client Sign Up →", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
            }
          }

          Spacer(modifier = Modifier.height(14.dp))
        }
      }
    }
  }
}
