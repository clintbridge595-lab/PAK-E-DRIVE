package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Splash & Loading Screen matching the user's reference design:
 * - Silvery pale blue-grey gradient at top blending smoothly into rich deep royal/navy blue at the bottom.
 * - Centered car side silhouette with white window cutouts, vibrant orange swoosh underline, and registered mark.
 * - Bold, crisp "PAK E DRIVE" title in heavy dark lettering.
 * - Clean white "Travel & Tourism" subtitle.
 * - Smooth entrance animation and elegant progress loading bar.
 */
@Composable
fun SplashScreen(onTimeout: () -> Unit) {
  var startAnimation by remember { mutableStateOf(false) }

  val alphaAnim by animateFloatAsState(
    targetValue = if (startAnimation) 1f else 0f,
    animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
    label = "splashAlpha"
  )

  val scaleAnim by animateFloatAsState(
    targetValue = if (startAnimation) 1f else 0.92f,
    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
    label = "splashScale"
  )

  var progress by remember { mutableStateOf(0.1f) }
  val animatedProgress by animateFloatAsState(
    targetValue = progress,
    animationSpec = tween(durationMillis = 1800, easing = LinearOutSlowInEasing),
    label = "progress"
  )

  LaunchedEffect(Unit) {
    startAnimation = true
    progress = 1.0f
    delay(2200)
    onTimeout()
  }

  // Smooth vertical gradient exactly matching the user's reference screenshot:
  // Top: Silvery pale steel blue-grey -> Upper: Muted slate blue -> Mid: Royal twilight blue -> Bottom: Deep midnight navy
  val splashGradient = Brush.verticalGradient(
    colorStops = arrayOf(
      0.0f to Color(0xFFD6DEE9),
      0.15f to Color(0xFFBAC7D8),
      0.35f to Color(0xFF6B80A6),
      0.55f to Color(0xFF283B6A),
      0.75f to Color(0xFF19274F),
      1.0f to Color(0xFF0D1633)
    )
  )

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(splashGradient)
      .windowInsetsPadding(WindowInsets.statusBars),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier
        .fillMaxWidth()
        .scale(scaleAnim)
        .alpha(alphaAnim)
        .padding(horizontal = 24.dp)
    ) {
      Spacer(modifier = Modifier.weight(1f))

      // 1. Sleek Car Silhouette with Orange Swoosh & Registered Symbol
      CarSilhouetteLogo(
        modifier = Modifier
          .width(260.dp)
          .height(95.dp),
        carColor = Color(0xFF121E42),
        swooshColor = Color(0xFFFF5722),
        windowColor = Color.White
      )

      Spacer(modifier = Modifier.height(10.dp))

      // 2. Main Brand Name in bold, modern uppercase text
      Text(
        text = "PAK E DRIVE",
        color = Color(0xFF0F172A),
        fontSize = 24.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 1.8.sp
      )

      Spacer(modifier = Modifier.height(28.dp))

      // 3. Subtitle in clean white typography (exact match to reference image)
      Text(
        text = "Travel & Tourism",
        color = Color.White,
        fontSize = 20.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.5.sp
      )

      Spacer(modifier = Modifier.weight(1f))

      // Subtle Loading Progress Indicator
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 36.dp)
      ) {
        LinearProgressIndicator(
          progress = { animatedProgress },
          modifier = Modifier
            .width(140.dp)
            .height(3.dp),
          color = Color(0xFFFF5722),
          trackColor = Color.White.copy(alpha = 0.25f),
          strokeCap = StrokeCap.Round
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = "Loading Chauffeur Fleet...",
          color = Color.White.copy(alpha = 0.7f),
          fontSize = 11.5.sp,
          fontWeight = FontWeight.Normal
        )
      }
    }
  }
}

/**
 * Vector drawing of the modern car silhouette matching the reference logo:
 * - Aerodynamic hatchback / sedan profile
 * - Crisp white window cutouts
 * - Vibrant orange dynamic swoosh underneath
 * - ® Registered trademark symbol
 */
@Composable
fun CarSilhouetteLogo(
  modifier: Modifier = Modifier,
  carColor: Color = Color(0xFF121E42),
  swooshColor: Color = Color(0xFFFF5722),
  windowColor: Color = Color.White
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height

    // Reference bounding box: 260w x 95h
    // Scale factors
    val sx = w / 260f
    val sy = h / 95f

    // 1. Car Body Silhouette
    val carPath = Path().apply {
      // Start at rear bumper bottom
      moveTo(35f * sx, 56f * sy)
      // Rear bumper upward curve
      quadraticTo(28f * sx, 50f * sy, 32f * sx, 43f * sy)
      // Taillight notch
      lineTo(42f * sx, 42f * sy)
      // Rear hatch rising up to roof spoiler
      quadraticTo(52f * sx, 32f * sy, 62f * sx, 25f * sy)
      // Roofline
      lineTo(142f * sx, 25f * sy)
      // Windshield sloping down to hood
      quadraticTo(168f * sx, 33f * sy, 185f * sx, 45f * sy)
      // Hood line to front nose
      lineTo(220f * sx, 50f * sy)
      // Front bumper curve down
      quadraticTo(226f * sx, 53f * sy, 222f * sx, 57f * sy)
      // Front lower spoiler
      lineTo(205f * sx, 58f * sy)
      // Underbody bottom line back to rear
      lineTo(45f * sx, 58f * sy)
      close()
    }
    drawPath(path = carPath, color = carColor, style = Fill)

    // 2. Rear Window (White cutout)
    val rearWindowPath = Path().apply {
      moveTo(68f * sx, 28f * sy)
      lineTo(98f * sx, 28f * sy)
      lineTo(98f * sx, 41f * sy)
      lineTo(60f * sx, 41f * sy)
      quadraticTo(62f * sx, 34f * sy, 68f * sx, 28f * sy)
      close()
    }
    drawPath(path = rearWindowPath, color = windowColor, style = Fill)

    // 3. Front Window (White cutout)
    val frontWindowPath = Path().apply {
      moveTo(105f * sx, 28f * sy)
      lineTo(138f * sx, 28f * sy)
      quadraticTo(158f * sx, 34f * sy, 168f * sx, 41f * sy)
      lineTo(105f * sx, 41f * sy)
      close()
    }
    drawPath(path = frontWindowPath, color = windowColor, style = Fill)

    // 4. Front Headlight Accent (Small orange/amber sliver)
    val headlightPath = Path().apply {
      moveTo(216f * sx, 50f * sy)
      lineTo(223f * sx, 51f * sy)
      lineTo(220f * sx, 54f * sy)
      lineTo(214f * sx, 53f * sy)
      close()
    }
    drawPath(path = headlightPath, color = Color(0xFFFF9800), style = Fill)

    // 5. Vibrant Orange Swoosh Underline
    val swooshPath = Path().apply {
      moveTo(30f * sx, 64f * sy)
      // Top curve of the swoosh flowing right
      quadraticTo(125f * sx, 69f * sy, 225f * sx, 65f * sy)
      // Return curve thinning out to the left
      quadraticTo(130f * sx, 76f * sy, 30f * sx, 64f * sy)
      close()
    }
    drawPath(path = swooshPath, color = swooshColor, style = Fill)

    // 6. Registered Trademark Symbol ®
    val regCenter = Offset(228f * sx, 26f * sy)
    val regRadius = 5.5f * sx
    drawCircle(
      color = carColor,
      radius = regRadius,
      center = regCenter,
      style = Stroke(width = 1.2f * sx)
    )
    // Draw small 'R' inside
    val rPath = Path().apply {
      moveTo(regCenter.x - 2f * sx, regCenter.y + 3f * sy)
      lineTo(regCenter.x - 2f * sx, regCenter.y - 3f * sy)
      lineTo(regCenter.x + 1f * sx, regCenter.y - 3f * sy)
      quadraticTo(regCenter.x + 2.5f * sx, regCenter.y - 1.5f * sy, regCenter.x + 1f * sx, regCenter.y)
      lineTo(regCenter.x - 2f * sx, regCenter.y)
      moveTo(regCenter.x, regCenter.y)
      lineTo(regCenter.x + 2.2f * sx, regCenter.y + 3f * sy)
    }
    drawPath(
      path = rPath,
      color = carColor,
      style = Stroke(width = 1.1f * sx, cap = StrokeCap.Round)
    )
  }
}
