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
import coil.compose.AsyncImage
import com.pomo.mypomo.R
import com.example.data.model.Car
import com.example.ui.theme.*

@Composable
fun PakEDriveTopBar(
  selectedTab: Int = 0,
  currentCity: String = "Karachi",
  userName: String = "Mehdi",
  unreadCount: Int,
  onNotificationsClick: () -> Unit,
  onCityClick: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  Surface(
    color = Color(0xFFF8F9FA),
    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
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
      if (selectedTab == 0) {
        // Home Screen Top Bar (Left: Bell icon + City Selector, Right: Hello, Mehdi)
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.clickable(onClick = onCityClick)
        ) {
          Box(modifier = Modifier.size(36.dp)) {
            IconButton(
              onClick = onNotificationsClick,
              modifier = Modifier.fillMaxSize()
            ) {
              Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notifications",
                tint = Color(0xFF111827),
                modifier = Modifier.size(24.dp)
              )
            }
            if (unreadCount > 0) {
              Box(
                modifier = Modifier
                  .align(Alignment.TopEnd)
                  .padding(top = 2.dp, end = 2.dp)
                  .size(14.dp)
                  .clip(CircleShape)
                  .background(Color(0xFFEF4444)),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = unreadCount.toString(),
                  color = Color.White,
                  fontSize = 8.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }

          Spacer(modifier = Modifier.width(4.dp))

          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 2.dp)
          ) {
            Text(
              text = currentCity,
              color = Color(0xFF111827),
              fontSize = 15.sp,
              fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.width(2.dp))
            Icon(
              imageVector = Icons.Default.KeyboardArrowDown,
              contentDescription = "Select City",
              tint = Color(0xFF4B5563),
              modifier = Modifier.size(18.dp)
            )
          }
        }

        Text(
          text = "Hello, $userName",
          color = Color(0xFF111827),
          fontSize = 15.sp,
          fontWeight = FontWeight.SemiBold
        )
      } else {
        // Other tabs top bar with centered title
        val title = when (selectedTab) {
          1 -> "My Bookings"
          2 -> "Search"
          3 -> "Messages"
          4 -> "Profile"
          else -> "PAK E DRIVE"
        }

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(36.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = title,
            color = Color(0xFF111827),
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }
}

/**
 * Clean, normal 2-column vehicle card directly matching the reference video:
 * - Car image with rounded top corners loaded via Coil
 * - Top-left "Featured" navy badge
 * - Line 1: Name on left | Price on right
 * - Line 2: Variant on left
 * - Line 3: Route in green/teal
 * - Entire card clickable to book / view details
 */
@Composable
fun CarCard(
  car: Car,
  onViewDetails: () -> Unit,
  onBookNow: () -> Unit = onViewDetails,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
    modifier = modifier
      .fillMaxWidth()
      .clickable(onClick = onViewDetails)
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      // 1. Car Image with "Featured" pill badge
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(115.dp)
          .background(Color(0xFFF9FAFB)),
        contentAlignment = Alignment.Center
      ) {
        AsyncImage(
          model = car.imageRes,
          contentDescription = car.name,
          contentScale = ContentScale.Fit,
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 6.dp, vertical = 6.dp)
        )

        // "Featured" or "Partner Driver" pill badge top-left
        if (car.isPartnerCar) {
          Surface(
            shape = RoundedCornerShape(4.dp),
            color = Color(0xFF166534),
            modifier = Modifier
              .padding(6.dp)
              .align(Alignment.TopStart)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Icon(Icons.Default.Verified, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
              Spacer(modifier = Modifier.width(3.dp))
              Text(
                text = "Partner Driver",
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        } else if (car.isFeatured) {
          Surface(
            shape = RoundedCornerShape(4.dp),
            color = Color(0xFF132238),
            modifier = Modifier
              .padding(6.dp)
              .align(Alignment.TopStart)
          ) {
            Text(
              text = "Featured",
              color = Color.White,
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }
      }

      // 2. Info details section with clear, normalized typography hierarchy
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 10.dp, vertical = 8.dp)
      ) {
        // Line 1: Vehicle Name at the top
        Text(
          text = car.name,
          fontSize = 13.5.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF111827),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Line 2: 10-Hour Rate as secondary, legible caption
        val rateCaption = if (car.tenHourRate > 0) {
          "10-Hour Rate: Rs. %,d".format(car.tenHourRate)
        } else if (car.priceDisplay.isNotBlank()) {
          car.priceDisplay
        } else {
          "Rs. %,d/day".format(car.dailyRate)
        }

        Text(
          text = rateCaption,
          fontSize = 11.5.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF111827),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Line 3: Model Variant / Specs
        Text(
          text = if (car.variant.isNotBlank()) car.variant else car.make,
          fontSize = 10.5.sp,
          color = Color(0xFF6B7280),
          fontWeight = FontWeight.Normal,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(3.dp))

        // Line 4: Route info in readable dark tone
        Text(
          text = car.routeSnippet,
          fontSize = 10.5.sp,
          color = Color(0xFF374151),
          fontWeight = FontWeight.Medium,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
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
