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
import com.example.data.model.Review
import com.example.ui.theme.*

@Composable
fun VehicleDetailDialog(
  car: Car,
  reviews: List<Review> = emptyList(),
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
                  color = Color(0xFF111827)
                )
              }

              VerticalDivider(color = BorderStroke, modifier = Modifier.height(36.dp))

              Column {
                Text(text = "24 Hours Full Day", fontSize = 11.sp, color = TextSecondaryMuted)
                Text(
                  text = "PKR ${String.format("%,d", car.dailyRate)}",
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF111827)
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

          Spacer(modifier = Modifier.height(18.dp))

          // Customer Reviews Section
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Verified Reviews & Ratings",
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = NavyPrimary
            )

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color(0xFFFEF3C7)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("4.9 (100% Verified)", color = Color(0xFFB45309), fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          val carReviews = reviews.filter { it.carId == car.id || it.carName.contains(car.name, ignoreCase = true) }
          if (carReviews.isNotEmpty()) {
            carReviews.forEach { review ->
              Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 4.dp)
              ) {
                Column(modifier = Modifier.padding(10.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(review.userName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                    Row {
                      repeat(5) { starIndex ->
                        val isFilled = (starIndex + 1) <= review.overallRating.toInt()
                        Icon(
                          imageVector = Icons.Default.Star,
                          contentDescription = null,
                          tint = if (isFilled) Color(0xFFF59E0B) else Color(0xFFCBD5E1),
                          modifier = Modifier.size(12.dp)
                        )
                      }
                    }
                  }
                  if (review.comment.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(review.comment, fontSize = 11.sp, color = TextSecondaryMuted, lineHeight = 15.sp)
                  }
                  Spacer(modifier = Modifier.height(2.dp))
                  Text("Chauffeur: ${String.format("%.1f", review.driverRating)}★ • ${review.userCity} • ${review.date}", fontSize = 10.sp, color = Color(0xFF94A3B8))
                }
              }
            }
          } else {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color(0xFFF1F5F9),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = "⭐ 100% verified rides recorded. Clean AC vehicle with sanitized interior and courteous licensed chauffeur.",
                fontSize = 12.sp,
                color = TextSecondaryMuted,
                modifier = Modifier.padding(10.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(20.dp))

          // Bottom CTA button
          Button(
            onClick = onBookNow,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF111827)),
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
