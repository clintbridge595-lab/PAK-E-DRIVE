package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Animated Shimmer Skeleton Loader for Fleet Car Cards and Lists
 */
@Composable
fun ShimmerSkeletonCarCard(modifier: Modifier = Modifier) {
  val transition = rememberInfiniteTransition(label = "shimmer")
  val translateAnim by transition.animateFloat(
    initialValue = 0f,
    targetValue = 1000f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1200, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "shimmerTranslate"
  )

  val shimmerColors = listOf(
    Color.LightGray.copy(alpha = 0.4f),
    Color.LightGray.copy(alpha = 0.15f),
    Color.LightGray.copy(alpha = 0.4f)
  )

  val brush = Brush.linearGradient(
    colors = shimmerColors,
    start = Offset.Zero,
    end = Offset(x = translateAnim, y = translateAnim)
  )

  Card(
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = 8.dp),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(140.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(brush)
      )
      Spacer(modifier = Modifier.height(12.dp))
      Box(
        modifier = Modifier
          .fillMaxWidth(0.6f)
          .height(18.dp)
          .clip(RoundedCornerShape(6.dp))
          .background(brush)
      )
      Spacer(modifier = Modifier.height(8.dp))
      Box(
        modifier = Modifier
          .fillMaxWidth(0.4f)
          .height(14.dp)
          .clip(RoundedCornerShape(6.dp))
          .background(brush)
      )
    }
  }
}

/**
 * Robust Network Error & Timeout Dialog with Auto-Retry
 */
@Composable
fun NetworkTimeoutErrorDialog(
  errorCode: String = "504 Gateway Timeout",
  errorMessage: String = "The high-speed dispatch server took too long to respond due to heavy traffic on the M-2 / M-9 route corridor.",
  onRetry: () -> Unit,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    icon = {
      Icon(Icons.Default.WifiOff, contentDescription = null, tint = MaterialTheme.colorScheme.error)
    },
    title = {
      Text(errorCode, fontWeight = FontWeight.Bold)
    },
    text = {
      Column {
        Text(errorMessage, fontSize = 13.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(10.dp))
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
        ) {
          Text(
            text = "Offline fallback engine active. Your booking request will be saved and automatically enqueued via Android WorkManager.",
            fontSize = 11.5.sp,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(8.dp)
          )
        }
      }
    },
    confirmButton = {
      Button(onClick = onRetry) {
        Icon(Icons.Default.Refresh, contentDescription = null)
        Spacer(modifier = Modifier.width(4.dp))
        Text("Retry Connection")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Dismiss")
      }
    }
  )
}
