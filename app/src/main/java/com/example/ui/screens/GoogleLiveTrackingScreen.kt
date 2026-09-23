package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.services.LocationTrackingService
import kotlinx.coroutines.delay

data class IntercityRouteFare(
  val origin: String,
  val destination: String,
  val distanceKm: Int,
  val estimatedHours: Double,
  val motorwayTollsPkr: Int,
  val baseFarePkr: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleLiveTrackingScreen(
  onBack: () -> Unit
) {
  val context = LocalContext.current
  var isServiceRunning by remember { mutableStateOf(false) }
  var carPositionRatio by remember { mutableStateOf(0.35f) }
  var driverSpeed by remember { mutableStateOf(48) }
  var etaMinutes by remember { mutableStateOf(11) }

  // Intercity Route Fares Engine
  var selectedOrigin by remember { mutableStateOf("Karachi") }
  var selectedDestination by remember { mutableStateOf("Lahore") }
  var showFareCalculator by remember { mutableStateOf(false) }

  val popularRoutes = remember {
    listOf(
      IntercityRouteFare("Karachi", "Hyderabad", 160, 2.2, 450, 9500),
      IntercityRouteFare("Karachi", "Lahore", 1210, 16.5, 4200, 58000),
      IntercityRouteFare("Lahore", "Islamabad", 375, 4.0, 1100, 18500),
      IntercityRouteFare("Islamabad", "Peshawar", 185, 2.1, 650, 11000),
      IntercityRouteFare("Lahore", "Multan", 345, 3.8, 950, 16500)
    )
  }

  // Smooth animation of driver position along route
  LaunchedEffect(isServiceRunning) {
    if (isServiceRunning) {
      while (true) {
        delay(1200)
        carPositionRatio = (carPositionRatio + 0.04f).let { if (it > 0.95f) 0.1f else it }
        driverSpeed = (45..68).random()
        etaMinutes = maxOf(2, (15 * (1f - carPositionRatio)).toInt())
      }
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text("Live GPS Ride Tracking", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("Google Maps & Fused Location Service", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          IconButton(onClick = { showFareCalculator = !showFareCalculator }) {
            Icon(Icons.Default.Calculate, contentDescription = "Intercity Fares Engine")
          }
        }
      )
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .verticalScroll(rememberScrollState())
    ) {
      // 1. Interactive Visual Map Canvas with Route Polyline and Real-time Moving Vehicle
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(280.dp)
          .background(Color(0xFFE5E9EE))
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val w = size.width
          val h = size.height

          // Draw simulated map road grids
          val roadPaint = Color(0xFFFFFFFF)
          drawLine(roadPaint, Offset(0f, h * 0.3f), Offset(w, h * 0.3f), strokeWidth = 14f)
          drawLine(roadPaint, Offset(0f, h * 0.7f), Offset(w, h * 0.7f), strokeWidth = 12f)
          drawLine(roadPaint, Offset(w * 0.25f, 0f), Offset(w * 0.25f, h), strokeWidth = 12f)
          drawLine(roadPaint, Offset(w * 0.75f, 0f), Offset(w * 0.75f, h), strokeWidth = 12f)

          // Draw Primary High-Speed Highway Route (Polyline)
          val routePath = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.12f, h * 0.75f)
            cubicTo(w * 0.3f, h * 0.6f, w * 0.5f, h * 0.8f, w * 0.88f, h * 0.25f)
          }

          // Route border shadow
          drawPath(
            routePath,
            color = Color(0x331976D2),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 18f, cap = StrokeCap.Round)
          )
          // Route main line
          drawPath(
            routePath,
            color = Color(0xFF1E88E5),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 10f, cap = StrokeCap.Round)
          )

          // Pickup Pin Marker (Start)
          drawCircle(Color(0xFF2E7D32), radius = 12f, center = Offset(w * 0.12f, h * 0.75f))
          drawCircle(Color.White, radius = 5f, center = Offset(w * 0.12f, h * 0.75f))

          // Destination Pin Marker (End)
          drawCircle(Color(0xFFD32F2F), radius = 14f, center = Offset(w * 0.88f, h * 0.25f))
          drawCircle(Color.White, radius = 6f, center = Offset(w * 0.88f, h * 0.25f))

          // Live Moving Driver Vehicle Marker along cubic curve
          val t = carPositionRatio
          val p0 = Offset(w * 0.12f, h * 0.75f)
          val p1 = Offset(w * 0.3f, h * 0.6f)
          val p2 = Offset(w * 0.5f, h * 0.8f)
          val p3 = Offset(w * 0.88f, h * 0.25f)

          val cx = Math.pow(1.0 - t, 3.0) * p0.x + 3 * Math.pow(1.0 - t, 2.0) * t * p1.x + 3 * (1.0 - t) * Math.pow(t.toDouble(), 2.0) * p2.x + Math.pow(t.toDouble(), 3.0) * p3.x
          val cy = Math.pow(1.0 - t, 3.0) * p0.y + 3 * Math.pow(1.0 - t, 2.0) * t * p1.y + 3 * (1.0 - t) * Math.pow(t.toDouble(), 2.0) * p2.y + Math.pow(t.toDouble(), 3.0) * p3.y

          val carPos = Offset(cx.toFloat(), cy.toFloat())

          // Pulse radar circle
          drawCircle(Color(0x33FF5722), radius = 26f, center = carPos)
          // Car marker badge
          drawCircle(Color(0xFFFF5722), radius = 14f, center = carPos)
          drawCircle(Color.White, radius = 6f, center = carPos)
        }

        // Floating Map Status Badges
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.Black.copy(alpha = 0.75f),
            contentColor = Color.White
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(if (isServiceRunning) Color(0xFF00E676) else Color(0xFFFFB300))
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                if (isServiceRunning) "LIVE GPS (1s Sync)" else "GPS Paused",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }

          Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("$driverSpeed km/h", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
          }
        }

        // Live ETA Bottom Overlay inside map
        Card(
          modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(12.dp)
            .fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
        ) {
          Row(
            modifier = Modifier
              .padding(horizontal = 14.dp, vertical = 8.dp)
              .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Navigation, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text("Toyota Corolla Altis Grande (LED-2023)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("Heading north via Shahrah-e-Faisal", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }
            Column(horizontalAlignment = Alignment.End) {
              Text("$etaMinutes min", fontWeight = FontWeight.Black, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
              Text("ETA Arrival", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
        }
      }

      // 2. Location Tracking Service Controller
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        colors = CardDefaults.cardColors(
          containerColor = if (isServiceRunning) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                "Driver Foreground Service Tracking",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = if (isServiceRunning) Color(0xFF1B5E20) else MaterialTheme.colorScheme.onSurface
              )
              Text(
                "Broadcasts high-accuracy GPS coordinates to Firebase Realtime DB",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Switch(
              checked = isServiceRunning,
              onCheckedChange = { start ->
                isServiceRunning = start
                val intent = Intent(context, LocationTrackingService::class.java)
                if (start) {
                  try {
                    context.startService(intent)
                  } catch (_: Exception) {}
                } else {
                  context.stopService(intent)
                }
              }
            )
          }
        }
      }

      // 3. Driver & Vehicle Information Card
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text("Assigned Chauffeur & Vehicle", fontWeight = FontWeight.Bold, fontSize = 15.sp)
          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Muhammad Tariq", fontWeight = FontWeight.Bold, fontSize = 14.5.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                  shape = RoundedCornerShape(4.dp),
                  color = Color(0xFFE8F5E9)
                ) {
                  Text("DLIMS Verified", color = Color(0xFF2E7D32), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                }
              }
              Text("Rating: 4.9 ★ (218 trips) | Phone: +92 300 1234567", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }

          Spacer(modifier = Modifier.height(14.dp))
          HorizontalDivider()
          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text("Current Location", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text("24.8607° N, 67.0011° E", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
              Text("Speed & Heading", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text("$driverSpeed km/h | 42° NNE", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 4. Intercity Route Fares Engine & Distance Matrix
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.AutoMirrored.Filled.AltRoute, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Google Distance Matrix & Motorway Tolls", fontWeight = FontWeight.Bold, fontSize = 15.sp)
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            "Automated highway fare, distance, and M-Tag motorway toll tax calculation engine across Pakistan.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(14.dp))

          popularRoutes.forEach { route ->
            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
              shape = RoundedCornerShape(10.dp),
              color = MaterialTheme.colorScheme.surface
            ) {
              Row(
                modifier = Modifier
                  .padding(12.dp)
                  .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text("${route.origin} ➔ ${route.destination}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                  Text("${route.distanceKm} km | ~${route.estimatedHours} hrs | Tolls: PKR ${route.motorwayTollsPkr}", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("PKR ${route.baseFarePkr}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}
