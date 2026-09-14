package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import com.example.PakEDriveApplication
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
import com.example.data.model.Booking
import com.example.data.model.Car
import com.example.data.model.PakistanRoutesData
import com.example.ui.components.RouteVisualizerCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import com.example.util.CnicValidator
import com.example.util.PasswordValidator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingFlowDialog(
  car: Car,
  viewModel: MainViewModel,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val profile by viewModel.userProfile.collectAsState()
  val cities = viewModel.cities.filter { it != "All Cities" }

  var selectedTripType by remember { mutableStateOf("Local City (10 Hours)") }
  var pickupCity by remember { mutableStateOf(if (cities.contains(profile.city)) profile.city else "Karachi") }
  var pickupAddress by remember { mutableStateOf(profile.address.ifBlank { "Clifton Block 4, Karachi" }) }
  var dropCity by remember { mutableStateOf(if (selectedTripType.contains("Intercity")) "Hyderabad" else pickupCity) }
  var dropAddress by remember { mutableStateOf(if (selectedTripType.contains("Intercity")) "Auto Bahn Road, Hyderabad" else "Jinnah International Airport, Karachi") }

  var selectedDate by remember { mutableStateOf("Tomorrow") }
  var selectedTime by remember { mutableStateOf("10:00 AM") }
  var durationDays by remember { mutableStateOf(1) }

  var customerName by remember(profile) { mutableStateOf(profile.name) }
  var customerPhone by remember(profile) { mutableStateOf(profile.phone) }
  var customerEmail by remember(profile) { mutableStateOf(profile.email) }
  var customerCnic by remember(profile) { mutableStateOf(profile.cnic) }
  var selectedRouteInfo by remember { mutableStateOf<com.example.data.model.PakistanRoute?>(null) }
  var contactMode by remember { mutableStateOf(0) } // 0: Mobile Number (+92), 1: Email Address
  var password by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }
  var authErrorMessage by remember { mutableStateOf<String?>(null) }
  var isProcessingAuth by remember { mutableStateOf(false) }
  val coroutineScope = rememberCoroutineScope()

  var showPickupCityMenu by remember { mutableStateOf(false) }
  var showDropCityMenu by remember { mutableStateOf(false) }

  // Calculation of estimated price
  val basePrice = if (selectedTripType.contains("10 Hours")) car.tenHourRate else car.dailyRate
  val calculatedTotal = basePrice * durationDays

  val tripTypes = listOf("Local City (10 Hours)", "Intercity Highway Tour", "Airport Transfer", "Wedding Decorated")
  val dateOptions = listOf("Today", "Tomorrow", "In 2 Days", "This Weekend")
  val timeOptions = listOf("08:00 AM", "10:00 AM", "02:00 PM", "06:00 PM", "09:00 PM")

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Card(
      shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(0.95f)
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
      ) {
        // Top Header
        Surface(
          color = NavyPrimary,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Book Ride: ${car.name}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Text(
                text = "Doorstep pickup with verified commercial chauffeur",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.8f)
              )
            }

            IconButton(
              onClick = onDismiss,
              modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f))
            ) {
              Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(18.dp))
            }
          }
        }

        Column(modifier = Modifier.padding(16.dp)) {
          // Selected vehicle summary preview
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(Color(0xFFF1F4F9))
              .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Image(
              painter = painterResource(id = car.imageRes),
              contentDescription = car.name,
              contentScale = ContentScale.Crop,
              modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(text = car.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
              Text(text = "${car.category} • ${car.seats} Seater • ${car.transmission}", fontSize = 11.sp, color = TextSecondaryMuted)
              Text(text = "Chauffeur Included • 100% Reliable", fontSize = 11.sp, color = StatusGreen, fontWeight = FontWeight.SemiBold)
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // 1. Service Type
          Text(text = "1. Select Trip Type", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
          Spacer(modifier = Modifier.height(8.dp))
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            tripTypes.forEach { type ->
              val isSelected = selectedTripType == type
              FilterChip(
                selected = isSelected,
                onClick = { selectedTripType = type },
                label = { Text(type, fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = NavyPrimary,
                  selectedLabelColor = Color.White,
                  containerColor = Color.White,
                  labelColor = TextPrimaryDark
                ),
                border = FilterChipDefaults.filterChipBorder(enabled = true, selected = isSelected, borderColor = if (isSelected) NavyPrimary else BorderStroke)
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // 2. Pickup & Drop Locations
          Text(text = "2. Itinerary & Route Selection", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
          Spacer(modifier = Modifier.height(6.dp))

          // Popular Pakistan Routes Quick Chips
          Text(
            text = "Popular Highway Routes:",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondaryMuted
          )
          Spacer(modifier = Modifier.height(4.dp))
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            PakistanRoutesData.routes.forEach { r ->
              val isRouteActive = pickupCity == r.fromCity && dropCity == r.toCity
              SuggestionChip(
                onClick = {
                  pickupCity = r.fromCity
                  dropCity = r.toCity
                  selectedTripType = "Intercity Highway Tour"
                  selectedRouteInfo = r
                  if (r.distanceKm > 400 && durationDays == 1) {
                    durationDays = if (r.distanceKm > 900) 3 else 2
                  }
                },
                label = {
                  Text(
                    text = "${r.fromCity} ➔ ${r.toCity}",
                    fontSize = 11.5.sp,
                    fontWeight = if (isRouteActive) FontWeight.Bold else FontWeight.Medium
                  )
                },
                colors = SuggestionChipDefaults.suggestionChipColors(
                  containerColor = if (isRouteActive) NavyPrimary else Color(0xFFF1F5F9),
                  labelColor = if (isRouteActive) Color.White else TextPrimaryDark
                ),
                border = SuggestionChipDefaults.suggestionChipBorder(
                  enabled = true,
                  borderColor = if (isRouteActive) NavyPrimary else BorderStroke
                )
              )
            }
          }

          selectedRouteInfo?.let { r ->
            if (pickupCity == r.fromCity && dropCity == r.toCity) {
              Spacer(modifier = Modifier.height(6.dp))
              Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.Route, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(18.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "${r.highwayName} • ${r.distanceKm} km • Est. ${r.estimatedDuration}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NavyPrimary
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Pickup City Selector & Address
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            // City Box
            Box(modifier = Modifier.width(130.dp)) {
              OutlinedButton(
                onClick = { showPickupCityMenu = true },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyPrimary),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 14.dp)
              ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = OrangeAccent, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(pickupCity, fontSize = 12.sp, fontWeight = FontWeight.Bold)
              }
              DropdownMenu(
                expanded = showPickupCityMenu,
                onDismissRequest = { showPickupCityMenu = false }
              ) {
                cities.forEach { city ->
                  DropdownMenuItem(
                    text = { Text(city) },
                    onClick = {
                      pickupCity = city
                      showPickupCityMenu = false
                    }
                  )
                }
              }
            }

            // Pickup Address (CRITICAL: Black text Color(0xFF0A0F1D) on white)
            OutlinedTextField(
              value = pickupAddress,
              onValueChange = { pickupAddress = it },
              label = { Text("Pickup Address", fontSize = 11.sp) },
              singleLine = true,
              shape = RoundedCornerShape(10.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color(0xFF0A0F1D),
                unfocusedTextColor = Color(0xFF0A0F1D),
                cursorColor = NavyPrimary,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = NavyPrimary,
                unfocusedBorderColor = BorderStroke
              ),
              modifier = Modifier.weight(1f)
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Drop City Selector & Address
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            // City Box
            Box(modifier = Modifier.width(130.dp)) {
              OutlinedButton(
                onClick = { showDropCityMenu = true },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyPrimary),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 14.dp)
              ) {
                Icon(Icons.Default.Flag, contentDescription = null, tint = StatusGreen, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(dropCity, fontSize = 12.sp, fontWeight = FontWeight.Bold)
              }
              DropdownMenu(
                expanded = showDropCityMenu,
                onDismissRequest = { showDropCityMenu = false }
              ) {
                cities.forEach { city ->
                  DropdownMenuItem(
                    text = { Text(city) },
                    onClick = {
                      dropCity = city
                      showDropCityMenu = false
                    }
                  )
                }
              }
            }

            // Drop Address (CRITICAL: Black text Color(0xFF0A0F1D) on white)
            OutlinedTextField(
              value = dropAddress,
              onValueChange = { dropAddress = it },
              label = { Text("Destination / Drop-off", fontSize = 11.sp) },
              singleLine = true,
              shape = RoundedCornerShape(10.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color(0xFF0A0F1D),
                unfocusedTextColor = Color(0xFF0A0F1D),
                cursorColor = NavyPrimary,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = NavyPrimary,
                unfocusedBorderColor = BorderStroke
              ),
              modifier = Modifier.weight(1f)
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Visual Route Map preview
          RouteVisualizerCard(
            pickupCity = pickupCity,
            pickupAddress = pickupAddress,
            dropCity = dropCity,
            dropAddress = dropAddress
          )

          Spacer(modifier = Modifier.height(16.dp))

          // 3. Date & Time
          Text(text = "3. Date & Starting Time", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            dateOptions.forEach { date ->
              val isSelected = selectedDate == date
              FilterChip(
                selected = isSelected,
                onClick = { selectedDate = date },
                label = { Text(date, fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = NavyPrimary,
                  selectedLabelColor = Color.White,
                  containerColor = Color.White,
                  labelColor = TextPrimaryDark
                ),
                border = FilterChipDefaults.filterChipBorder(enabled = true, selected = isSelected, borderColor = if (isSelected) NavyPrimary else BorderStroke)
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            timeOptions.forEach { time ->
              val isSelected = selectedTime == time
              FilterChip(
                selected = isSelected,
                onClick = { selectedTime = time },
                label = { Text(time, fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = OrangeAccent,
                  selectedLabelColor = Color.White,
                  containerColor = Color.White,
                  labelColor = TextPrimaryDark
                ),
                border = FilterChipDefaults.filterChipBorder(enabled = true, selected = isSelected, borderColor = if (isSelected) OrangeAccent else BorderStroke)
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Duration stepper
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text("Rental Duration", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
              Text(
                text = if (durationDays == 1) "1 Day (${if (selectedTripType.contains("10 Hours")) "10 Hours" else "24 Hours"})" else "$durationDays Days Tour",
                fontSize = 12.sp,
                color = TextSecondaryMuted
              )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
              IconButton(
                onClick = { if (durationDays > 1) durationDays-- },
                modifier = Modifier
                  .size(34.dp)
                  .clip(CircleShape)
                  .background(Color(0xFFE2E8F0))
              ) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = TextPrimaryDark, modifier = Modifier.size(16.dp))
              }

              Text(
                text = "$durationDays",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = NavyPrimary,
                modifier = Modifier.padding(horizontal = 14.dp)
              )

              IconButton(
                onClick = { if (durationDays < 30) durationDays++ },
                modifier = Modifier
                  .size(34.dp)
                  .clip(CircleShape)
                  .background(NavyPrimary)
              ) {
                Icon(Icons.Default.Add, contentDescription = "Increase", tint = Color.White, modifier = Modifier.size(16.dp))
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // 4. Passenger Details & Account Sign-In
          val isClientLoggedIn = profile.isLoggedIn
          Text(
            text = if (isClientLoggedIn) "4. Passenger Contact Info" else "4. Account Sign-In / Sign-Up (Required to Book)",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = NavyPrimary
          )
          Spacer(modifier = Modifier.height(8.dp))

          if (isClientLoggedIn) {
            Card(
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
              border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF22C55E)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text("Verified Member Account", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF14532D))
                  Text("${profile.name} • ${profile.phone.ifBlank { profile.email }}", fontSize = 11.5.sp, color = Color(0xFF166534))
                }
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
              value = customerName,
              onValueChange = { customerName = it },
              label = { Text("Passenger Name", fontSize = 11.sp) },
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

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
              value = customerPhone,
              onValueChange = { customerPhone = it },
              label = { Text("Mobile Number (WhatsApp)", fontSize = 11.sp) },
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

            Spacer(modifier = Modifier.height(8.dp))

            // CNIC for logged in user
            OutlinedTextField(
              value = customerCnic,
              onValueChange = {
                customerCnic = CnicValidator.formatCnic(it)
                authErrorMessage = null
              },
              label = { Text("Customer CNIC (13 Digits Required)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
              placeholder = { Text("42101-1234567-1", fontSize = 12.sp) },
              singleLine = true,
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
              leadingIcon = {
                Icon(Icons.Default.Badge, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(18.dp))
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

            val memberCnicCheck = CnicValidator.validate(customerCnic)
            Row(modifier = Modifier.padding(top = 3.dp), verticalAlignment = Alignment.CenterVertically) {
              Icon(
                if (memberCnicCheck.isValid) Icons.Default.CheckCircle else Icons.Default.Info,
                contentDescription = null,
                tint = if (memberCnicCheck.isValid) StatusGreen else OrangeAccent,
                modifier = Modifier.size(12.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (memberCnicCheck.isValid) "CNIC Verified for Ride" else "Mandatory 13-digit Pakistani CNIC to complete booking",
                fontSize = 10.5.sp,
                color = if (memberCnicCheck.isValid) StatusGreen else OrangeAccent
              )
            }
          } else {
            Card(
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
              border = androidx.compose.foundation.BorderStroke(1.dp, BorderStroke),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.Lock, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "Sign In & Continue with Password",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                  )
                }
                Text(
                  text = "Please enter your name, phone or email, and password to confirm your booking and create your member account.",
                  fontSize = 11.sp,
                  color = TextSecondaryMuted,
                  modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Full Name
                Text("Full Name", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = TextPrimaryDark)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                  value = customerName,
                  onValueChange = {
                    customerName = it
                    authErrorMessage = null
                  },
                  placeholder = { Text("e.g. Mehdi Raza", fontSize = 12.sp, color = TextSecondaryMuted) },
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

                // Mandatory CNIC field
                Text("NADRA CNIC (13 Digits)", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = TextPrimaryDark)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                  value = customerCnic,
                  onValueChange = {
                    customerCnic = CnicValidator.formatCnic(it)
                    authErrorMessage = null
                  },
                  placeholder = { Text("42101-1234567-1", fontSize = 12.sp, color = TextSecondaryMuted) },
                  singleLine = true,
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                  leadingIcon = {
                    Icon(Icons.Default.Badge, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(16.dp))
                  },
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

                val cnicV = CnicValidator.validate(customerCnic)
                Row(modifier = Modifier.padding(top = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    if (cnicV.isValid) Icons.Default.CheckCircle else Icons.Default.Info,
                    contentDescription = null,
                    tint = if (cnicV.isValid) StatusGreen else TextSecondaryMuted,
                    modifier = Modifier.size(11.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = if (cnicV.isValid) "CNIC Format Verified" else "Mandatory 13-digit Pakistani CNIC to book car",
                    fontSize = 10.sp,
                    color = if (cnicV.isValid) StatusGreen else TextSecondaryMuted
                  )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Mode Switcher: Mobile Phone vs Email Address
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE2E8F0))
                    .padding(2.dp)
                ) {
                  Box(
                    modifier = Modifier
                      .weight(1f)
                      .clip(RoundedCornerShape(6.dp))
                      .background(if (contactMode == 0) Color.White else Color.Transparent)
                      .clickable {
                        contactMode = 0
                        authErrorMessage = null
                      }
                      .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = "Mobile Phone (+92)",
                      fontSize = 11.sp,
                      fontWeight = if (contactMode == 0) FontWeight.Bold else FontWeight.Medium,
                      color = if (contactMode == 0) NavyPrimary else TextSecondaryMuted
                    )
                  }

                  Box(
                    modifier = Modifier
                      .weight(1f)
                      .clip(RoundedCornerShape(6.dp))
                      .background(if (contactMode == 1) Color.White else Color.Transparent)
                      .clickable {
                        contactMode = 1
                        authErrorMessage = null
                      }
                      .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = "Email Address",
                      fontSize = 11.sp,
                      fontWeight = if (contactMode == 1) FontWeight.Bold else FontWeight.Medium,
                      color = if (contactMode == 1) NavyPrimary else TextSecondaryMuted
                    )
                  }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (contactMode == 0) {
                  OutlinedTextField(
                    value = customerPhone,
                    onValueChange = {
                      customerPhone = it
                      authErrorMessage = null
                    },
                    placeholder = { Text("315 2292493", fontSize = 12.sp, color = TextSecondaryMuted) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                    leadingIcon = {
                      Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 8.dp, end = 4.dp)) {
                        Text("🇵🇰 +92", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                      }
                    },
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
                } else {
                  OutlinedTextField(
                    value = customerEmail,
                    onValueChange = {
                      customerEmail = it
                      authErrorMessage = null
                    },
                    placeholder = { Text("user@gmail.com", fontSize = 12.sp, color = TextSecondaryMuted) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    leadingIcon = {
                      Icon(Icons.Default.Email, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(16.dp))
                    },
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
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Password
                Text("Password", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = TextPrimaryDark)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                  value = password,
                  onValueChange = {
                    password = it
                    authErrorMessage = null
                  },
                  placeholder = { Text("Min 8 chars, letters & numbers", fontSize = 12.sp, color = TextSecondaryMuted) },
                  singleLine = true,
                  visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                  leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(16.dp))
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

                val hasLength = password.length >= 8
                val hasAlphaNum = password.any { it.isLetter() } && password.any { it.isDigit() }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    if (hasLength && hasAlphaNum) Icons.Default.CheckCircle else Icons.Default.Info,
                    contentDescription = null,
                    tint = if (hasLength && hasAlphaNum) StatusGreen else TextSecondaryMuted,
                    modifier = Modifier.size(12.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "Requires min 8 alphanumeric characters (letters + numbers)",
                    fontSize = 10.sp,
                    color = if (hasLength && hasAlphaNum) StatusGreen else TextSecondaryMuted
                  )
                }

                authErrorMessage?.let { err ->
                  Spacer(modifier = Modifier.height(6.dp))
                  Text(text = err, color = StatusRed, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Pricing Breakdown Card
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F9FC)),
            modifier = Modifier
              .fillMaxWidth()
              .border(1.dp, BorderStroke, RoundedCornerShape(12.dp))
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text("Base Rate ($durationDays Day/Shift)", fontSize = 12.sp, color = TextSecondaryMuted)
                Text("PKR ${String.format("%,d", calculatedTotal)}", fontSize = 12.sp, color = TextPrimaryDark, fontWeight = FontWeight.SemiBold)
              }
              Spacer(modifier = Modifier.height(4.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text("Chauffeur Services", fontSize = 12.sp, color = TextSecondaryMuted)
                Text("INCLUDED", fontSize = 12.sp, color = StatusGreen, fontWeight = FontWeight.Bold)
              }
              Spacer(modifier = Modifier.height(4.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text("Fuel & Toll Taxes", fontSize = 12.sp, color = TextSecondaryMuted)
                Text("As per consumption", fontSize = 12.sp, color = TextSecondaryMuted)
              }
              HorizontalDivider(color = BorderStroke, modifier = Modifier.padding(vertical = 8.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text("Total Estimated", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                Text("PKR ${String.format("%,d", calculatedTotal)}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF111827))
              }
            }
          }

          Spacer(modifier = Modifier.height(18.dp))

          // Confirm and Submit Button (Enforces sign in & continue before booking!)
          Button(
            onClick = {
              // STRICT CNIC VALIDATION - BOOKING BLOCKED WITHOUT CNIC
              val cnicCheck = CnicValidator.validate(customerCnic)
              if (!cnicCheck.isValid) {
                authErrorMessage = cnicCheck.errorMessage ?: "Valid 13-digit Pakistani CNIC is required to book a car."
                return@Button
              }

              val formattedCnic = CnicValidator.formatCnic(customerCnic)

              if (!isClientLoggedIn) {
                if (customerName.isBlank()) {
                  authErrorMessage = "Please enter your full name."
                  return@Button
                }

                val identifier = if (contactMode == 0) {
                  val digits = customerPhone.filter { it.isDigit() }
                  if (digits.length < 7) {
                    authErrorMessage = "Please enter a valid mobile number."
                    return@Button
                  }
                  if (digits.startsWith("0")) "+92 " + digits.substring(1) else "+92 $digits"
                } else {
                  if (customerEmail.isBlank() || !customerEmail.contains("@")) {
                    authErrorMessage = "Please enter a valid email address."
                    return@Button
                  }
                  customerEmail.trim()
                }

                val valRes = PasswordValidator.validate(password)
                if (!valRes.isValid) {
                  authErrorMessage = valRes.errorMessage
                  return@Button
                }

                authErrorMessage = null
                isProcessingAuth = true

                coroutineScope.launch {
                  delay(400)
                  viewModel.loginWithPassword(
                    identifier = identifier,
                    name = customerName.trim(),
                    isEmail = contactMode == 1,
                    cnic = formattedCnic
                  )

                  val bookingId = "PED-${10000 + Random().nextInt(90000)}"
                  val newBooking = Booking(
                    id = bookingId,
                    carId = car.id,
                    carName = car.name,
                    carCategory = car.category,
                    carImageRes = car.imageRes,
                    tripType = selectedTripType,
                    pickupCity = pickupCity,
                    pickupAddress = pickupAddress,
                    dropCity = dropCity,
                    dropAddress = dropAddress,
                    dateText = selectedDate,
                    timeText = selectedTime,
                    rentalDurationText = if (durationDays == 1) "1 Day" else "$durationDays Days",
                    withDriver = true,
                    totalEstimatedPrice = calculatedTotal,
                    customerName = customerName.trim(),
                    customerPhone = if (contactMode == 0) identifier else "+92 315 2292493",
                    customerCnic = formattedCnic,
                    status = "Driver Assigned",
                    driverName = car.partnerDriverName ?: "Muhammad Aslam",
                    driverPhone = car.partnerPhone ?: "+92 315 2292493",
                    vehiclePlateNumber = "BLF-256 (Sindh)"
                  )
                  viewModel.completeBooking(newBooking)

                  PakEDriveApplication.logBookingRoute(
                    carName = car.name,
                    fromCity = pickupCity,
                    toCity = dropCity,
                    rate = calculatedTotal.toLong()
                  )

                  val whatsappMsg = "Assalam-o-Alaikum PAK E DRIVE! I signed in and booked ${car.name} (Booking ID: #$bookingId) for $pickupCity to $dropCity on $selectedDate at $selectedTime. Duration: $durationDays Day(s). Total: PKR ${String.format("%,d", calculatedTotal)}. Name: $customerName, Contact: $identifier, CNIC: $formattedCnic."
                  val url = "https://wa.me/923152292493?text=${Uri.encode(whatsappMsg)}"
                  val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                  try { context.startActivity(intent) } catch (_: Exception) {}
                  isProcessingAuth = false
                }
              } else {
                val bookingId = "PED-${10000 + Random().nextInt(90000)}"
                // Update profile with CNIC if not already saved
                viewModel.updateProfile(
                  name = customerName.ifBlank { profile.name },
                  phone = customerPhone.ifBlank { profile.phone },
                  email = profile.email,
                  city = profile.city,
                  address = profile.address,
                  cnic = formattedCnic
                )

                val newBooking = Booking(
                  id = bookingId,
                  carId = car.id,
                  carName = car.name,
                  carCategory = car.category,
                  carImageRes = car.imageRes,
                  tripType = selectedTripType,
                  pickupCity = pickupCity,
                  pickupAddress = pickupAddress,
                  dropCity = dropCity,
                  dropAddress = dropAddress,
                  dateText = selectedDate,
                  timeText = selectedTime,
                  rentalDurationText = if (durationDays == 1) "1 Day" else "$durationDays Days",
                  withDriver = true,
                  totalEstimatedPrice = calculatedTotal,
                  customerName = customerName.ifBlank { profile.name.ifBlank { "Valued Member" } },
                  customerPhone = customerPhone.ifBlank { profile.phone.ifBlank { "+92 315 2292493" } },
                  customerCnic = formattedCnic,
                  status = "Driver Assigned",
                  driverName = car.partnerDriverName ?: "Muhammad Aslam",
                  driverPhone = car.partnerPhone ?: "+92 315 2292493",
                  vehiclePlateNumber = "BLF-256 (Sindh)"
                )
                viewModel.completeBooking(newBooking)

                PakEDriveApplication.logBookingRoute(
                  carName = car.name,
                  fromCity = pickupCity,
                  toCity = dropCity,
                  rate = calculatedTotal.toLong()
                )

                val whatsappMsg = "Assalam-o-Alaikum PAK E DRIVE! I booked ${car.name} (Booking ID: #$bookingId) for $pickupCity to $dropCity on $selectedDate at $selectedTime. Duration: $durationDays Day(s). Total: PKR ${String.format("%,d", calculatedTotal)}. Name: ${newBooking.customerName}, Phone: ${newBooking.customerPhone}, CNIC: $formattedCnic."
                val url = "https://wa.me/923152292493?text=${Uri.encode(whatsappMsg)}"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                try { context.startActivity(intent) } catch (_: Exception) {}
              }
            },
            enabled = !isProcessingAuth,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF111827)),
            modifier = Modifier
              .fillMaxWidth()
              .height(54.dp)
          ) {
            if (isProcessingAuth) {
              CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
              Spacer(modifier = Modifier.width(8.dp))
              Text("Signing In & Confirming Booking...", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            } else {
              Icon(
                if (!isClientLoggedIn) Icons.Default.Login else Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color.White
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = if (!isClientLoggedIn) "Sign In & Confirm Booking" else "Confirm Booking & Send to WhatsApp",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))
        }
      }
    }
  }
}
