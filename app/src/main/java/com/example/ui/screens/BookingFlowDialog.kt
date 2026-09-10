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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Booking
import com.example.data.model.Car
import com.example.ui.components.RouteVisualizerCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
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
          Text(text = "2. Itinerary & Location", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
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

          // 4. Passenger Details (CRITICAL: Black text Color(0xFF0A0F1D) on white)
          Text(text = "4. Passenger Contact Info", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
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
                Text("PKR ${String.format("%,d", calculatedTotal)}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = OrangeAccent)
              }
            }
          }

          Spacer(modifier = Modifier.height(18.dp))

          // Confirm and Submit Button
          Button(
            onClick = {
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
                customerName = customerName.ifBlank { "Valued Customer" },
                customerPhone = customerPhone.ifBlank { "+92 315 2292493" },
                status = "Driver Assigned",
                driverName = "Muhammad Aslam",
                driverPhone = "+92 315 2292493",
                vehiclePlateNumber = "BLF-256 (Sindh)"
              )
              viewModel.completeBooking(newBooking)

              // Track booking route in Firebase Analytics
              PakEDriveApplication.logBookingRoute(
                carName = car.name,
                fromCity = pickupCity,
                toCity = dropCity,
                rate = calculatedTotal.toLong()
              )

              // WhatsApp direct intent with pre-filled booking details
              val whatsappMsg = "Assalam-o-Alaikum PAK E DRIVE! I booked ${car.name} (Booking ID: #$bookingId) for $pickupCity to $dropCity on $selectedDate at $selectedTime. Duration: $durationDays Day(s). Total: PKR ${String.format("%,d", calculatedTotal)}. Name: $customerName, Phone: $customerPhone."
              val url = "https://wa.me/923152292493?text=${Uri.encode(whatsappMsg)}"
              val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
              try { context.startActivity(intent) } catch (_: Exception) {}
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
            modifier = Modifier
              .fillMaxWidth()
              .height(54.dp)
          ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Confirm Booking & Send to WhatsApp",
              color = Color.White,
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold
            )
          }

          Spacer(modifier = Modifier.height(16.dp))
        }
      }
    }
  }
}
