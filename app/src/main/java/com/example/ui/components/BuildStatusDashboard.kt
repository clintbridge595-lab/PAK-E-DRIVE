package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.gemini.BuildErrorSeverity
import com.example.data.model.BuildHealthStatus
import com.example.data.model.BuildResultStatus
import com.example.ui.theme.*

/**
 * Status Dashboard UI that visualizes the current health of the build,
 * including total errors found (with Critical, Warning, Info breakdown),
 * modules built progress, and time elapsed.
 */
@Composable
fun BuildStatusDashboard(
  health: BuildHealthStatus,
  isBuildActive: Boolean,
  onSeverityClick: ((BuildErrorSeverity) -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = modifier
      .fillMaxWidth()
      .testTag("build_status_dashboard")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      // 1. Dashboard Header with Status Banner & Health Score
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          StatusPulseIndicator(status = health.status, isBuilding = isBuildActive)
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "Build Health Monitor",
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = NavyPrimary
            )
            Text(
              text = when (health.status) {
                BuildResultStatus.IDLE -> "System Idle • Ready to build"
                BuildResultStatus.BUILDING -> "Building Gradle modules..."
                BuildResultStatus.PASSED -> "Build Successful • All healthy"
                BuildResultStatus.WARNINGS -> "Build Finished with ${health.warningCount} warning(s)"
                BuildResultStatus.FAILED -> "Build Failed • ${health.criticalErrors} critical error(s)"
              },
              fontSize = 11.sp,
              color = TextSecondaryMuted
            )
          }
        }

        // Health Score Badge
        HealthScorePill(score = health.healthScore, status = health.status)
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 2. Primary 3-Metric Visual Row: Total Errors, Modules Built, Time Elapsed
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Metric 1: TOTAL ERRORS FOUND
        MetricCard(
          title = "Total Errors",
          value = "${health.totalErrors}",
          subtitle = "${health.criticalErrors} Crit • ${health.warningCount} Warn",
          icon = if (health.criticalErrors > 0) Icons.Default.ErrorOutline else Icons.Default.CheckCircle,
          accentColor = when {
            health.criticalErrors > 0 -> StatusRed
            health.warningCount > 0 -> StatusAmber
            else -> StatusGreen
          },
          bgColor = when {
            health.criticalErrors > 0 -> StatusRedLight
            health.warningCount > 0 -> StatusAmberLight
            else -> StatusGreenLight
          },
          testTag = "metric_total_errors",
          modifier = Modifier.weight(1f)
        )

        // Metric 2: MODULES BUILT
        MetricCard(
          title = "Modules Built",
          value = "${health.modulesBuilt}/${health.totalModules}",
          subtitle = "${(health.progressFraction * 100).toInt()}% compiled",
          icon = Icons.Default.Widgets,
          accentColor = NavyPrimary,
          bgColor = NavyPrimary.copy(alpha = 0.08f),
          testTag = "metric_modules_built",
          modifier = Modifier.weight(1f)
        )

        // Metric 3: TIME ELAPSED
        MetricCard(
          title = "Time Elapsed",
          value = health.formattedElapsedTime,
          subtitle = if (isBuildActive) "Live runtime" else "Final duration",
          icon = Icons.Default.Timer,
          accentColor = OrangeAccent,
          bgColor = OrangeLight,
          testTag = "metric_time_elapsed",
          modifier = Modifier.weight(1f)
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      // 3. Module Build Progress Bar
      Column(modifier = Modifier.fillMaxWidth()) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Module Build Pipeline",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondaryMuted
          )
          Text(
            text = "${health.modulesBuilt} of ${health.totalModules} modules complete",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = NavyPrimary
          )
        }
        Spacer(modifier = Modifier.height(6.dp))

        val animatedProgress by animateFloatAsState(
          targetValue = health.progressFraction,
          animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
          label = "module_progress"
        )

        LinearProgressIndicator(
          progress = { animatedProgress },
          modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .testTag("modules_progress_bar"),
          color = when {
            health.status == BuildResultStatus.FAILED -> StatusRed
            health.progressFraction >= 1f -> StatusGreen
            else -> NavyPrimary
          },
          trackColor = Color(0xFFE2E8F0)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Module chips
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          val moduleList = listOf(":core:model", ":core:database", ":core:network", ":feature:rental", ":app")
          moduleList.forEachIndexed { index, modName ->
            val isBuilt = index < health.modulesBuilt
            val isCurrent = index == health.modulesBuilt && isBuildActive
            Text(
              text = modName.substringAfterLast(":"),
              fontSize = 9.sp,
              fontFamily = FontFamily.Monospace,
              fontWeight = if (isBuilt || isCurrent) FontWeight.Bold else FontWeight.Normal,
              color = when {
                isCurrent -> OrangeAccent
                isBuilt -> StatusGreen
                else -> Color(0xFF94A3B8)
              }
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // 4. Current Task & Breakdown Pills
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF0F172A),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
          ) {
            Icon(
              imageVector = Icons.Default.Terminal,
              contentDescription = null,
              tint = Color(0xFF38BDF8),
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = health.currentTask,
              color = Color(0xFFE2E8F0),
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace,
              maxLines = 1
            )
          }

          // Error count breakdown quick pills
          Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SeverityMiniChip(
              count = health.criticalErrors,
              label = "Crit",
              color = StatusRed,
              onClick = { onSeverityClick?.invoke(BuildErrorSeverity.CRITICAL) }
            )
            SeverityMiniChip(
              count = health.warningCount,
              label = "Warn",
              color = StatusAmber,
              onClick = { onSeverityClick?.invoke(BuildErrorSeverity.WARNING) }
            )
            SeverityMiniChip(
              count = health.infoCount,
              label = "Info",
              color = Color(0xFF0284C7),
              onClick = { onSeverityClick?.invoke(BuildErrorSeverity.INFO) }
            )
          }
        }
      }
    }
  }
}

@Composable
private fun MetricCard(
  title: String,
  value: String,
  subtitle: String,
  icon: ImageVector,
  accentColor: Color,
  bgColor: Color,
  testTag: String,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = bgColor,
    modifier = modifier.testTag(testTag)
  ) {
    Column(
      modifier = Modifier.padding(10.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = title,
          fontSize = 10.sp,
          fontWeight = FontWeight.SemiBold,
          color = TextSecondaryMuted
        )
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = accentColor,
          modifier = Modifier.size(14.dp)
        )
      }

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = value,
        fontSize = 17.sp,
        fontWeight = FontWeight.ExtraBold,
        color = accentColor,
        fontFamily = FontFamily.Monospace
      )

      Text(
        text = subtitle,
        fontSize = 9.sp,
        color = TextSecondaryMuted,
        maxLines = 1
      )
    }
  }
}

@Composable
private fun StatusPulseIndicator(
  status: BuildResultStatus,
  isBuilding: Boolean
) {
  val infiniteTransition = rememberInfiniteTransition(label = "pulse_trans")
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.3f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse_alpha"
  )

  val color = when (status) {
    BuildResultStatus.IDLE -> Color(0xFF64748B)
    BuildResultStatus.BUILDING -> OrangeAccent
    BuildResultStatus.PASSED -> StatusGreen
    BuildResultStatus.WARNINGS -> StatusAmber
    BuildResultStatus.FAILED -> StatusRed
  }

  Box(
    modifier = Modifier
      .size(36.dp)
      .clip(CircleShape)
      .background(color.copy(alpha = if (isBuilding) pulseAlpha * 0.25f else 0.15f)),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      imageVector = when (status) {
        BuildResultStatus.IDLE -> Icons.Default.Pause
        BuildResultStatus.BUILDING -> Icons.Default.Sync
        BuildResultStatus.PASSED -> Icons.Default.Check
        BuildResultStatus.WARNINGS -> Icons.Default.Warning
        BuildResultStatus.FAILED -> Icons.Default.Close
      },
      contentDescription = null,
      tint = color,
      modifier = Modifier.size(20.dp)
    )
  }
}

@Composable
private fun HealthScorePill(
  score: Int,
  status: BuildResultStatus
) {
  val (bgColor, textColor) = when {
    score >= 90 -> StatusGreenLight to StatusGreen
    score >= 60 -> StatusAmberLight to StatusAmber
    else -> StatusRedLight to StatusRed
  }

  Surface(
    shape = RoundedCornerShape(20.dp),
    color = bgColor,
    border = androidx.compose.foundation.BorderStroke(1.dp, textColor.copy(alpha = 0.3f))
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = Icons.Default.Shield,
        contentDescription = null,
        tint = textColor,
        modifier = Modifier.size(12.dp)
      )
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = "Health $score%",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = textColor
      )
    }
  }
}

@Composable
private fun SeverityMiniChip(
  count: Int,
  label: String,
  color: Color,
  onClick: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(4.dp),
    color = color.copy(alpha = 0.2f),
    onClick = onClick,
    modifier = Modifier.clip(RoundedCornerShape(4.dp))
  ) {
    Text(
      text = "$label $count",
      fontSize = 9.sp,
      fontWeight = FontWeight.Bold,
      color = color,
      fontFamily = FontFamily.Monospace,
      modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
    )
  }
}
