package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
  var startAnimation by remember { mutableStateOf(false) }

  val alphaAnim = animateFloatAsState(
    targetValue = if (startAnimation) 1f else 0f,
    animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
    label = "alpha"
  )

  val scaleAnim = animateFloatAsState(
    targetValue = if (startAnimation) 1f else 0.85f,
    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
    label = "scale"
  )

  LaunchedEffect(Unit) {
    startAnimation = true
    delay(1800)
    onTimeout()
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        androidx.compose.ui.graphics.Brush.verticalGradient(
          colors = listOf(
            Color(0xFF8DA3CE),
            Color(0xFF1E3A8A),
            Color(0xFF0C1947)
          )
        )
      ),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .scale(scaleAnim.value)
        .alpha(alphaAnim.value)
        .padding(24.dp)
    ) {
      Box(
        modifier = Modifier
          .size(130.dp)
          .clip(RoundedCornerShape(24.dp))
          .background(Color.White)
          .padding(12.dp),
        contentAlignment = Alignment.Center
      ) {
        Image(
          painter = painterResource(id = R.drawable.img_hatcab_logo),
          contentDescription = "HAT CAB Logo",
          contentScale = ContentScale.Fit,
          modifier = Modifier.fillMaxSize()
        )
      }

      Spacer(modifier = Modifier.height(28.dp))

      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = "HAT CAB",
          color = Color.White,
          fontSize = 30.sp,
          fontWeight = FontWeight.Black,
          letterSpacing = 2.sp
        )
        Text(
          text = " ®",
          color = Color.White.copy(alpha = 0.8f),
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = "Travel & Tourism",
        color = Color.White,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.5.sp
      )
    }
  }
}
