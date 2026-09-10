package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Booking
import com.example.ui.theme.*
import com.example.util.AppLanguage
import com.example.util.LocaleStrings

@Composable
fun ReviewDialog(
  booking: Booking,
  currentLanguage: AppLanguage,
  onDismiss: () -> Unit,
  onSubmitReview: (carRating: Float, driverRating: Float, comment: String) -> Unit
) {
  var carRating by remember { mutableFloatStateOf(5.0f) }
  var driverRating by remember { mutableFloatStateOf(5.0f) }
  var comment by remember { mutableStateOf("") }
  var isSubmitting by remember { mutableStateOf(false) }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Card(
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .wrapContentHeight()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(20.dp)
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = LocaleStrings.get("rate_review", currentLanguage),
              fontSize = 18.sp,
              fontWeight = FontWeight.Black,
              color = NavyPrimary
            )
            Text(
              text = "${booking.carName} • ${booking.pickupCity} → ${booking.dropCity}",
              fontSize = 12.sp,
              color = TextSecondaryMuted
            )
          }
          IconButton(onClick = onDismiss, modifier = Modifier.size(30.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondaryMuted)
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Chauffeur preview card
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color(0xFFF8FAFC),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(NavyPrimary.copy(alpha = 0.1f)),
              contentAlignment = Alignment.Center
            ) {
              Text("🚗", fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Chauffeur: ${booking.driverName}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark
              )
              Text(
                text = "Booking #${booking.id} • ${booking.dateText}",
                fontSize = 11.sp,
                color = TextSecondaryMuted
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 1. Vehicle Condition Rating
        Text(
          text = LocaleStrings.get("car_rating_label", currentLanguage),
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = TextPrimaryDark
        )
        Spacer(modifier = Modifier.height(6.dp))
        StarRatingBar(
          rating = carRating,
          onRatingChanged = { carRating = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Chauffeur Behavior Rating
        Text(
          text = LocaleStrings.get("driver_rating_label", currentLanguage),
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = TextPrimaryDark
        )
        Spacer(modifier = Modifier.height(6.dp))
        StarRatingBar(
          rating = driverRating,
          onRatingChanged = { driverRating = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Comments
        Text(
          text = if (currentLanguage == AppLanguage.URDU) "آپ کا تفصیلی تبصرہ (اختیاری)" else "Written Feedback (Optional)",
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          color = TextPrimaryDark
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = comment,
          onValueChange = { comment = it },
          placeholder = {
            Text(
              text = LocaleStrings.get("review_placeholder", currentLanguage),
              fontSize = 12.sp,
              color = TextSecondaryMuted
            )
          },
          minLines = 3,
          maxLines = 5,
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

        Spacer(modifier = Modifier.height(20.dp))

        // Submit Button
        Button(
          onClick = {
            isSubmitting = true
            onSubmitReview(carRating, driverRating, comment)
            onDismiss()
          },
          enabled = !isSubmitting,
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
        ) {
          Text(
            text = LocaleStrings.get("submit_review", currentLanguage),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
          )
        }
      }
    }
  }
}

@Composable
fun StarRatingBar(
  rating: Float,
  onRatingChanged: (Float) -> Unit,
  maxStars: Int = 5,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(6.dp)
  ) {
    for (i in 1..maxStars) {
      val isFilled = i <= rating
      IconButton(
        onClick = { onRatingChanged(i.toFloat()) },
        modifier = Modifier.size(36.dp)
      ) {
        Icon(
          imageVector = if (isFilled) Icons.Default.Star else Icons.Default.StarOutline,
          contentDescription = "Star $i",
          tint = if (isFilled) Color(0xFFF59E0B) else Color(0xFFD1D5DB),
          modifier = Modifier.size(32.dp)
        )
      }
    }

    Spacer(modifier = Modifier.width(8.dp))

    Text(
      text = String.format("%.1f / 5.0", rating),
      fontWeight = FontWeight.Bold,
      fontSize = 13.sp,
      color = Color(0xFFB45309)
    )
  }
}
