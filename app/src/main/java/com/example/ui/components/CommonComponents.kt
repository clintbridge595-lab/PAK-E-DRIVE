package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Car
import com.example.ui.theme.*

@Composable
fun HatCabTopBar(
  onMenuClick: () -> Unit,
  unreadCount: Int,
  onNotificationsClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    color = NavyPrimary,
    modifier = modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .statusBarsPadding()
        .padding(horizontal = 16.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      // Left: Hamburger Menu Icon
      IconButton(
        onClick = onMenuClick,
        modifier = Modifier.size(40.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Menu,
          contentDescription = "Open Menu",
          tint = Color.White,
          modifier = Modifier.size(24.dp)
        )
      }

      // Center: Logo & Brand Name
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 8.dp)
      ) {
        Box(
          modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .padding(4.dp),
          contentAlignment = Alignment.Center
        ) {
          Image(
            painter = painterResource(id = R.drawable.img_hatcab_logo),
            contentDescription = "Hat Cab Logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
          )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "HAT CAB",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
          )
          Text(
            text = "®",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
          )
        }
      }

      // Right: Notification Bell with Badge
      Box(modifier = Modifier.size(40.dp)) {
        IconButton(
          onClick = onNotificationsClick,
          modifier = Modifier.fillMaxSize()
        ) {
          Icon(
            imageVector = Icons.Outlined.Notifications,
            contentDescription = "Notifications",
            tint = Color.White,
            modifier = Modifier.size(24.dp)
          )
        }
        if (unreadCount > 0) {
          Box(
            modifier = Modifier
              .align(Alignment.TopEnd)
              .padding(top = 4.dp, end = 4.dp)
              .size(16.dp)
              .clip(CircleShape)
              .background(Color(0xFFE53935)),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = unreadCount.toString(),
              color = Color.White,
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }
  }
}

// Backwards compatibility alias
@Composable
fun PakEDriveTopBar(
  selectedCity: String = "All Cities",
  onCityClick: () -> Unit = {},
  unreadCount: Int = 0,
  onNotificationsClick: () -> Unit = {},
  onCallHelplineClick: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  HatCabTopBar(
    onMenuClick = onCityClick,
    unreadCount = unreadCount,
    onNotificationsClick = onNotificationsClick,
    modifier = modifier
  )
}

/**
 * Clean, high-end car card directly matching the reference video:
 * - Image with "Featured" dark navy badge on top-left
 * - Line 1: Name on left | Price on right
 * - Line 2: Variant on left
 * - Line 3: Route in green/teal
 * - Bottom: "Book Now" & "Details" buttons
 */
@Composable
fun CarCard(
  car: Car,
  onViewDetails: () -> Unit,
  onBookNow: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = modifier
      .fillMaxWidth()
      .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
      .clickable(onClick = onViewDetails)
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      // 1. Car Image with "Featured" pill badge
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(150.dp)
          .background(Color(0xFFF8F9FB)),
        contentAlignment = Alignment.Center
      ) {
        Image(
          painter = painterResource(id = car.imageRes),
          contentDescription = car.name,
          contentScale = ContentScale.Fit,
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 6.dp)
        )

        // "Featured" pill badge top-left
        if (car.isFeatured) {
          Surface(
            shape = RoundedCornerShape(4.dp),
            color = NavyPrimary,
            modifier = Modifier
              .padding(10.dp)
              .align(Alignment.TopStart)
          ) {
            Text(
              text = "Featured",
              color = Color.White,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
          }
        }
      }

      // 2. Info details section
      Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
        // Line 1: Car Name & Price
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = car.name,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111827),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
          )

          Text(
            text = if (car.priceDisplay.isNotBlank()) car.priceDisplay else "Rs. ${car.dailyRate}/day",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111827)
          )
        }

        Spacer(modifier = Modifier.height(3.dp))

        // Line 2: Variant
        Text(
          text = if (car.variant.isNotBlank()) car.variant else car.make,
          fontSize = 13.sp,
          color = Color(0xFF6B7280),
          fontWeight = FontWeight.Normal,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Line 3: Route in emerald/teal green
        Text(
          text = car.routeSnippet,
          fontSize = 13.sp,
          color = Color(0xFF00897B), // Rich emerald green like the video
          fontWeight = FontWeight.Medium,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Line 4: Action Buttons
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedButton(
            onClick = onViewDetails,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyPrimary),
            border = ButtonDefaults.outlinedButtonBorder.copy(
              brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFD1D5DB))
            ),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
            modifier = Modifier.height(34.dp)
          ) {
            Text("Details", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
          }

          Spacer(modifier = Modifier.width(8.dp))

          Button(
            onClick = onBookNow,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            modifier = Modifier.height(34.dp)
          ) {
            Text("Book Now", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

@Composable
fun StatusBadge(status: String) {
  val (bgColor, textColor, icon) = when (status) {
    "Confirmed", "Completed" -> Triple(StatusGreenLight, StatusGreen, Icons.Default.CheckCircle)
    "Driver Assigned", "On the Way" -> Triple(Color(0xFFE8F0FE), NavyPrimary, Icons.Default.DirectionsCar)
    "Requested" -> Triple(StatusAmberLight, StatusAmber, Icons.Default.AccessTime)
    "Cancelled" -> Triple(StatusRedLight, StatusRed, Icons.Default.Cancel)
    else -> Triple(Color(0xFFF1F3F4), Color(0xFF5F6368), Icons.Default.Info)
  }

  Surface(
    shape = RoundedCornerShape(8.dp),
    color = bgColor
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = textColor,
        modifier = Modifier.size(13.dp)
      )
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = status,
        color = textColor,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold
      )
    }
  }
}
