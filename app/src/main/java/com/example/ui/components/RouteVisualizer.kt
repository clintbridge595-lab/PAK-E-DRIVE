package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun RouteVisualizerCard(
  pickupCity: String,
  pickupAddress: String,
  dropCity: String,
  dropAddress: String,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = modifier
      .fillMaxWidth()
      .border(1.dp, BorderStroke, RoundedCornerShape(16.dp))
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.AltRoute,
            contentDescription = null,
            tint = OrangeAccent,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Trip Route & Distance Preview",
            color = NavyPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
          )
        }
        Surface(
          shape = RoundedCornerShape(6.dp),
          color = StatusGreenLight
        ) {
          Text(
            text = "Motorway / Express",
            color = StatusGreen,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Visual Canvas Route Map Representation
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(84.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(Color(0xFFF2F5F9))
          .padding(8.dp)
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val canvasWidth = size.width
          val canvasHeight = size.height

          // Dotted highway path
          val start = Offset(x = 36f, y = canvasHeight / 2)
          val end = Offset(x = canvasWidth - 36f, y = canvasHeight / 2)

          drawLine(
            color = Color(0xFFC7D2FE),
            start = start,
            end = end,
            strokeWidth = 6f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
          )

          // Center moving car indicator
          val center = Offset(x = canvasWidth * 0.45f, y = canvasHeight / 2)
          drawCircle(
            color = Color(0xFF0A1554),
            radius = 12f,
            center = center
          )
          drawCircle(
            color = Color(0xFFF5730B),
            radius = 6f,
            center = center
          )
        }

        Row(
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Pickup node
          Column(horizontalAlignment = Alignment.Start) {
            Box(
              modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(StatusGreen),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.TripOrigin,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(12.dp)
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = pickupCity,
              color = NavyPrimary,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold
            )
          }

          // In-between stats badge
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = NavyPrimary,
            shadowElevation = 2.dp
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
              Icon(
                imageVector = Icons.Default.DirectionsCar,
                contentDescription = null,
                tint = OrangeAccent,
                modifier = Modifier.size(12.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Live GPS Chauffeur",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
              )
            }
          }

          // Destination node
          Column(horizontalAlignment = Alignment.End) {
            Box(
              modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(OrangeAccent),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = dropCity,
              color = NavyPrimary,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Location Addresses
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text("Pickup Location", color = TextSecondaryMuted, fontSize = 10.sp)
          Text(
            text = pickupAddress.ifBlank { "Selected location in $pickupCity" },
            color = TextPrimaryDark,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
          )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text("Drop-off / Destination", color = TextSecondaryMuted, fontSize = 10.sp)
          Text(
            text = dropAddress.ifBlank { "Destination in $dropCity" },
            color = TextPrimaryDark,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
          )
        }
      }
    }
  }
}
