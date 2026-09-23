package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
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
import com.example.data.model.Booking
import com.example.ui.components.RouteVisualizerCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun BookingsScreen(
  viewModel: MainViewModel,
  onBrowseCarsClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val bookings by viewModel.bookings.collectAsState()
  var selectedTab by remember { mutableStateOf(0) } // 0: Active, 1: History

  val activeBookings = remember(bookings) {
    bookings.filter { it.status != "Completed" && it.status != "Cancelled" }
  }

  val historyBookings = remember(bookings) {
    bookings.filter { it.status == "Completed" || it.status == "Cancelled" }
  }

  val displayList = if (selectedTab == 0) activeBookings else historyBookings

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(BackgroundLight)
  ) {
    // Header
    Surface(
      color = Color.White,
      modifier = Modifier.fillMaxWidth(),
      shadowElevation = 1.dp
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp)
      ) {
        Text(
          text = "My Bookings & Trips",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = NavyPrimary
        )
        Text(
          text = "Track your chauffeur, ride itinerary, and past invoices",
          fontSize = 12.sp,
          color = TextSecondaryMuted
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Tabs
        TabRow(
          selectedTabIndex = selectedTab,
          containerColor = Color(0xFFF1F4F9),
          contentColor = NavyPrimary,
          indicator = {},
          divider = {},
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
        ) {
          Tab(
            selected = selectedTab == 0,
            onClick = { selectedTab = 0 },
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(if (selectedTab == 0) NavyPrimary else Color.Transparent)
              .padding(vertical = 10.dp)
          ) {
            Text(
              text = "Active (${activeBookings.size})",
              color = if (selectedTab == 0) Color.White else TextPrimaryDark,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold
            )
          }

          Tab(
            selected = selectedTab == 1,
            onClick = { selectedTab = 1 },
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(if (selectedTab == 1) NavyPrimary else Color.Transparent)
              .padding(vertical = 10.dp)
          ) {
            Text(
              text = "Past & Cancelled (${historyBookings.size})",
              color = if (selectedTab == 1) Color.White else TextPrimaryDark,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }

    // Bookings List
    if (displayList.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(24.dp),
        contentAlignment = Alignment.Center
      ) {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Box(
              modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color(0xFFEAEFF8)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.DirectionsCar,
                contentDescription = null,
                tint = NavyPrimary,
                modifier = Modifier.size(32.dp)
              )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
              text = "No bookings yet.",
              fontSize = 17.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF111827)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Tap a car on the home screen to book.",
              fontSize = 13.sp,
              color = Color(0xFF6B7280),
              textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
              onClick = onBrowseCarsClick,
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF132238)),
              shape = RoundedCornerShape(20.dp)
            ) {
              Text("Browse Fleet", color = Color.White, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        items(displayList, key = { it.id }) { booking ->
          BookingItemCard(
            booking = booking,
            onCancelBooking = { viewModel.cancelBooking(booking.id) },
            onDeleteBooking = { viewModel.deleteBooking(booking.id) },
            onReviewClick = { viewModel.openReviewDialog(booking) }
          )
        }
      }
    }
  }
}

@Composable
fun BookingItemCard(
  booking: Booking,
  onCancelBooking: () -> Unit,
  onDeleteBooking: () -> Unit,
  onReviewClick: () -> Unit = {}
) {
  val context = LocalContext.current

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = Modifier
      .fillMaxWidth()
      .border(1.dp, BorderStroke, RoundedCornerShape(16.dp))
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      // Top row: ID & Status
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.ConfirmationNumber,
            contentDescription = null,
            tint = NavyPrimary,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "#${booking.id}",
            color = NavyPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
          )
        }
        StatusBadge(status = booking.status)
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Car details row
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Image(
          painter = painterResource(id = booking.carImageRes),
          contentDescription = booking.carName,
          contentScale = ContentScale.Crop,
          modifier = Modifier
            .size(72.dp)
            .clip(RoundedCornerShape(10.dp))
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = booking.carName,
            color = TextPrimaryDark,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "${booking.tripType} • ${booking.rentalDurationText}",
            color = TextSecondaryMuted,
            fontSize = 11.sp
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Total: PKR ${String.format("%,d", booking.totalEstimatedPrice)}",
            color = OrangeAccent,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Route snippet
      RouteVisualizerCard(
        pickupCity = booking.pickupCity,
        pickupAddress = booking.pickupAddress,
        dropCity = booking.dropCity,
        dropAddress = booking.dropAddress
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Chauffeur & Vehicle plate card
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFFF7F9FC),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderStroke),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(NavyPrimary),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = "Chauffeur: ${booking.driverName}",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimaryDark
              )
              Text(
                text = "Vehicle No: ${booking.vehiclePlateNumber}",
                fontSize = 10.sp,
                color = TextSecondaryMuted
              )
            }
          }

          // Chauffeur Call Button
          IconButton(
            onClick = {
              val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${booking.driverPhone}"))
              try { context.startActivity(intent) } catch (_: Exception) {}
            },
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(StatusGreenLight)
          ) {
            Icon(
              imageVector = Icons.Default.Phone,
              contentDescription = "Call Chauffeur",
              tint = StatusGreen,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Action buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (booking.status != "Cancelled" && booking.status != "Completed") {
          TextButton(
            onClick = onCancelBooking,
            colors = ButtonDefaults.textButtonColors(contentColor = StatusRed)
          ) {
            Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Cancel Ride", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
          }
        } else {
          TextButton(
            onClick = onDeleteBooking,
            colors = ButtonDefaults.textButtonColors(contentColor = TextSecondaryMuted)
          ) {
            Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Remove", fontSize = 12.sp)
          }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Button(
          onClick = onReviewClick,
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
          shape = RoundedCornerShape(8.dp),
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
        ) {
          Icon(Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Rate Trip", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.width(8.dp))

        Button(
          onClick = {
            // Open WhatsApp with booking details
            val msg = "Hello PAK E DRIVE! Regarding Booking #${booking.id} (${booking.carName}) for ${booking.pickupCity}."
            val url = "https://wa.me/923152292493?text=${Uri.encode(msg)}"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            try { context.startActivity(intent) } catch (_: Exception) {}
          },
          colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
          shape = RoundedCornerShape(8.dp),
          contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
        ) {
          Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Trip Support", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}
