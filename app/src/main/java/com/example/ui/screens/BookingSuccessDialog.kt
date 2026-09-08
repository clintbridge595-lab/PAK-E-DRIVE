package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.data.model.Booking
import com.example.ui.theme.*

@Composable
fun BookingSuccessDialog(
  booking: Booking,
  onDismiss: () -> Unit,
  onGoToBookings: () -> Unit
) {
  val context = LocalContext.current

  Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Success checkmark badge
        Box(
          modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(StatusGreenLight),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Success",
            tint = StatusGreen,
            modifier = Modifier.size(36.dp)
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = "Ride Booked Successfully!",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = NavyPrimary
        )

        Text(
          text = "Booking Reference: #${booking.id}",
          fontSize = 13.sp,
          fontWeight = FontWeight.SemiBold,
          color = OrangeAccent
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Booking mini card
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF1F4F9))
            .padding(10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Image(
            painter = painterResource(id = booking.carImageRes),
            contentDescription = booking.carName,
            contentScale = ContentScale.Crop,
            modifier = Modifier
              .size(54.dp)
              .clip(RoundedCornerShape(8.dp))
          )
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(text = booking.carName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
            Text(text = "${booking.pickupCity} → ${booking.dropCity}", fontSize = 11.sp, color = TextSecondaryMuted)
            Text(text = "${booking.dateText} at ${booking.timeText}", fontSize = 11.sp, color = NavyPrimary, fontWeight = FontWeight.Medium)
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stepper
        Column(modifier = Modifier.fillMaxWidth()) {
          TrackingStepItem(title = "Booking Registered", subtitle = "Confirmed in Hat Cab system", isCompleted = true)
          TrackingStepItem(title = "Chauffeur Assigned", subtitle = "${booking.driverName} (${booking.driverPhone})", isCompleted = true)
          TrackingStepItem(title = "Doorstep Vehicle Arrival", subtitle = "Pickup at ${booking.pickupAddress}", isCompleted = false)
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Actions
        Button(
          onClick = {
            onDismiss()
            onGoToBookings()
          },
          colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text("View in My Bookings", color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
          onClick = {
            val url = "https://wa.me/923152292493"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            try { context.startActivity(intent) } catch (_: Exception) {}
            onDismiss()
          },
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Icon(Icons.Default.Chat, contentDescription = null, tint = StatusGreen, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("WhatsApp Fleet Support")
        }
      }
    }
  }
}

@Composable
fun TrackingStepItem(
  title: String,
  subtitle: String,
  isCompleted: Boolean
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    verticalAlignment = Alignment.Top
  ) {
    Box(
      modifier = Modifier
        .size(20.dp)
        .clip(CircleShape)
        .background(if (isCompleted) StatusGreen else Color(0xFFD2D6DC)),
      contentAlignment = Alignment.Center
    ) {
      if (isCompleted) {
        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
      }
    }
    Spacer(modifier = Modifier.width(10.dp))
    Column {
      Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isCompleted) NavyPrimary else TextSecondaryMuted)
      Text(text = subtitle, fontSize = 10.sp, color = TextSecondaryMuted)
    }
  }
}
