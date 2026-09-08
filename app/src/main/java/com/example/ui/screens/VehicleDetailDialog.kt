package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Car
import com.example.ui.theme.*

@Composable
fun VehicleDetailDialog(
  car: Car,
  onDismiss: () -> Unit,
  onBookNow: () -> Unit
) {
  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Card(
      shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(0.92f)
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
      ) {
        // Image Header with Close Button
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .background(Color(0xFFEAEFF5))
        ) {
          Image(
            painter = painterResource(id = car.imageRes),
            contentDescription = car.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
          )

          // Close button
          IconButton(
            onClick = onDismiss,
            modifier = Modifier
              .align(Alignment.TopEnd)
              .padding(14.dp)
              .size(36.dp)
              .clip(CircleShape)
              .background(Color.Black.copy(alpha = 0.5f))
          ) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(20.dp))
          }

          // Category Badge
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = NavyPrimary,
            modifier = Modifier
              .align(Alignment.BottomStart)
              .padding(14.dp)
          ) {
            Text(
              text = car.category,
              color = Color.White,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            )
          }
        }

        // Details content
        Column(modifier = Modifier.padding(20.dp)) {
          Text(
            text = car.name,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = NavyPrimary
          )

          Spacer(modifier = Modifier.height(4.dp))

          Text(
            text = "${car.engineSpec} • ${car.transmission} • ${car.fuelType}",
            fontSize = 13.sp,
            color = TextSecondaryMuted
          )

          Spacer(modifier = Modifier.height(14.dp))

          // Rate highlight banner
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFF1F4F9),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(text = "10 Hours City Package", fontSize = 11.sp, color = TextSecondaryMuted)
                Text(
                  text = "PKR ${String.format("%,d", car.tenHourRate)}",
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Bold,
                  color = NavyPrimary
                )
              }

              VerticalDivider(color = BorderStroke, modifier = Modifier.height(36.dp))

              Column {
                Text(text = "24 Hours Full Day", fontSize = 11.sp, color = TextSecondaryMuted)
                Text(
                  text = "PKR ${String.format("%,d", car.dailyRate)}",
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Bold,
                  color = OrangeAccent
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(18.dp))

          Text(
            text = "Vehicle Overview",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = NavyPrimary
          )

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = car.description,
            fontSize = 13.sp,
            color = TextPrimaryDark,
            lineHeight = 19.sp
          )

          Spacer(modifier = Modifier.height(16.dp))

          Text(
            text = "Rental Inclusions & Specs",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = NavyPrimary
          )

          Spacer(modifier = Modifier.height(10.dp))

          car.features.forEach { feat ->
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(vertical = 4.dp)
            ) {
              Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = StatusGreen,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(text = feat, fontSize = 13.sp, color = TextPrimaryDark)
            }
          }

          Spacer(modifier = Modifier.height(20.dp))

          // Bottom CTA button
          Button(
            onClick = onBookNow,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
            modifier = Modifier
              .fillMaxWidth()
              .height(52.dp)
          ) {
            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Book Ride with Chauffeur",
              color = Color.White,
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }
  }
}
