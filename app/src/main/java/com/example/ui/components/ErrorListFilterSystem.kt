package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.gemini.BuildErrorSeverity
import com.example.data.gemini.IdentifiedBuildError
import com.example.data.model.BuildIssueFilter
import com.example.ui.theme.*

/**
 * Filter System & Error List UI allowing users to toggle between
 * 'Critical', 'Warning', and 'Info' build issues with live counts,
 * detailed issue cards, and integrated Gemini AI fix recommendations.
 */
@Composable
fun ErrorListFilterSystem(
  allErrors: List<IdentifiedBuildError>,
  filteredErrors: List<IdentifiedBuildError>,
  filter: BuildIssueFilter,
  onToggleSeverity: (BuildErrorSeverity) -> Unit,
  onSelectSingleSeverity: (BuildErrorSeverity?) -> Unit,
  onSelectAll: () -> Unit,
  onRequestAiFix: (IdentifiedBuildError) -> Unit,
  onApplyFix: (IdentifiedBuildError) -> Unit,
  isFixApplied: (IdentifiedBuildError) -> Boolean,
  isAiAnalyzing: Boolean,
  modifier: Modifier = Modifier
) {
  val criticalCount = remember(allErrors) { allErrors.count { it.severity == BuildErrorSeverity.CRITICAL } }
  val warningCount = remember(allErrors) { allErrors.count { it.severity == BuildErrorSeverity.WARNING } }
  val infoCount = remember(allErrors) { allErrors.count { it.severity == BuildErrorSeverity.INFO } }

  Column(modifier = modifier.fillMaxWidth()) {
    // 1. FILTER CONTROLS HEADER
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.FilterList,
          contentDescription = null,
          tint = NavyPrimary,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "Build Issues (${filteredErrors.size} of ${allErrors.size})",
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = NavyPrimary
        )
      }

      if (!filter.isAllSelected) {
        TextButton(
          onClick = onSelectAll,
          contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Text("Show All", fontSize = 11.sp, color = NavyPrimary, fontWeight = FontWeight.SemiBold)
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // 2. TOGGLE FILTER CHIPS: ALL, CRITICAL, WARNING, INFO
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // ALL Filter Chip
      SeverityFilterChip(
        label = "All Issues",
        count = allErrors.size,
        isSelected = filter.isAllSelected,
        icon = Icons.Default.ListAlt,
        activeColor = NavyPrimary,
        activeBgColor = NavyPrimary.copy(alpha = 0.12f),
        testTag = "filter_all_issues",
        onClick = onSelectAll
      )

      // CRITICAL Filter Chip
      SeverityFilterChip(
        label = "Critical",
        count = criticalCount,
        isSelected = filter.showCritical,
        icon = Icons.Default.Error,
        activeColor = StatusRed,
        activeBgColor = StatusRedLight,
        testTag = "filter_critical_issues",
        onClick = { onToggleSeverity(BuildErrorSeverity.CRITICAL) },
        onLongClick = { onSelectSingleSeverity(BuildErrorSeverity.CRITICAL) }
      )

      // WARNING Filter Chip
      SeverityFilterChip(
        label = "Warning",
        count = warningCount,
        isSelected = filter.showWarning,
        icon = Icons.Default.Warning,
        activeColor = StatusAmber,
        activeBgColor = StatusAmberLight,
        testTag = "filter_warning_issues",
        onClick = { onToggleSeverity(BuildErrorSeverity.WARNING) },
        onLongClick = { onSelectSingleSeverity(BuildErrorSeverity.WARNING) }
      )

      // INFO Filter Chip
      SeverityFilterChip(
        label = "Info",
        count = infoCount,
        isSelected = filter.showInfo,
        icon = Icons.Default.Info,
        activeColor = Color(0xFF0284C7),
        activeBgColor = Color(0xFFE0F2FE),
        testTag = "filter_info_issues",
        onClick = { onToggleSeverity(BuildErrorSeverity.INFO) },
        onLongClick = { onSelectSingleSeverity(BuildErrorSeverity.INFO) }
      )
    }

    Spacer(modifier = Modifier.height(10.dp))

    // 3. FILTERED ISSUES LIST
    if (filteredErrors.isEmpty()) {
      EmptyIssuesState(
        totalCount = allErrors.size,
        filter = filter,
        onReset = onSelectAll
      )
    } else {
      Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        filteredErrors.forEach { error ->
          BuildIssueCard(
            error = error,
            isApplied = isFixApplied(error),
            onRequestAiFix = { onRequestAiFix(error) },
            onApplyFix = { onApplyFix(error) },
            isAiAnalyzing = isAiAnalyzing
          )
        }
      }
    }
  }
}

@Composable
private fun SeverityFilterChip(
  label: String,
  count: Int,
  isSelected: Boolean,
  icon: ImageVector,
  activeColor: Color,
  activeBgColor: Color,
  testTag: String,
  onClick: () -> Unit,
  onLongClick: (() -> Unit)? = null
) {
  Surface(
    shape = RoundedCornerShape(10.dp),
    color = if (isSelected) activeBgColor else Color(0xFFF1F5F9),
    border = androidx.compose.foundation.BorderStroke(
      width = if (isSelected) 1.5.dp else 1.dp,
      color = if (isSelected) activeColor else Color(0xFFE2E8F0)
    ),
    modifier = Modifier
      .testTag(testTag)
      .clip(RoundedCornerShape(10.dp))
      .clickable(onClick = onClick)
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (isSelected) activeColor else Color(0xFF64748B),
        modifier = Modifier.size(15.dp)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = label,
        fontSize = 12.sp,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
        color = if (isSelected) activeColor else Color(0xFF475569)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Surface(
        shape = CircleShape,
        color = if (isSelected) activeColor else Color(0xFFCBD5E1)
      ) {
        Text(
          text = "$count",
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White,
          modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
        )
      }
    }
  }
}

@Composable
private fun BuildIssueCard(
  error: IdentifiedBuildError,
  isApplied: Boolean,
  onRequestAiFix: () -> Unit,
  onApplyFix: () -> Unit,
  isAiAnalyzing: Boolean
) {
  var isExpanded by remember { mutableStateOf(false) }
  val clipboardManager = LocalClipboardManager.current

  val (severityColor, severityBg, severityIcon) = when (error.severity) {
    BuildErrorSeverity.CRITICAL -> Triple(StatusRed, StatusRedLight, Icons.Default.Cancel)
    BuildErrorSeverity.WARNING -> Triple(StatusAmber, StatusAmberLight, Icons.Default.Warning)
    BuildErrorSeverity.INFO -> Triple(Color(0xFF0284C7), Color(0xFFE0F2FE), Icons.Default.Info)
  }

  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isApplied) Color(0xFFF0FDF4) else Color.White
    ),
    border = androidx.compose.foundation.BorderStroke(
      width = 1.dp,
      color = if (isApplied) StatusGreen.copy(alpha = 0.5f) else Color(0xFFE2E8F0)
    ),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("issue_card_${error.errorType}")
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      // Top Row: Severity Badge + Location + Expand toggle
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = if (isApplied) StatusGreenLight else severityBg
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = if (isApplied) Icons.Default.CheckCircle else severityIcon,
                contentDescription = null,
                tint = if (isApplied) StatusGreen else severityColor,
                modifier = Modifier.size(12.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (isApplied) "FIXED" else error.severity.name,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isApplied) StatusGreen else severityColor
              )
            }
          }

          if (error.sourceFile.isNotBlank() && error.sourceFile != "Unknown") {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = error.sourceFile + (error.lineNumber?.let { ":$it" } ?: ""),
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace,
              color = NavyPrimary,
              fontWeight = FontWeight.Medium
            )
          }
        }

        IconButton(
          onClick = { isExpanded = !isExpanded },
          modifier = Modifier.size(28.dp)
        ) {
          Icon(
            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = "Toggle details",
            tint = Color(0xFF64748B)
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Title & Summary
      Text(
        text = error.errorType,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = TextPrimaryDark
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = error.summary,
        fontSize = 12.sp,
        color = Color(0xFF334155),
        lineHeight = 16.sp
      )

      // Code Snippet Preview (if available)
      if (error.originalSnippet.isNotBlank()) {
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
          shape = RoundedCornerShape(6.dp),
          color = Color(0xFF0F172A),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = error.originalSnippet,
            color = Color(0xFFE2E8F0),
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            lineHeight = 14.sp,
            maxLines = if (isExpanded) 10 else 2,
            modifier = Modifier.padding(8.dp)
          )
        }
      }

      // Action Buttons: AI Fix & Apply
      Spacer(modifier = Modifier.height(10.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // AI Suggestion Trigger Button
        OutlinedButton(
          onClick = {
            isExpanded = true
            onRequestAiFix()
          },
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
          modifier = Modifier.height(32.dp)
        ) {
          Icon(
            imageVector = Icons.Default.AutoFixHigh,
            contentDescription = null,
            tint = NavyPrimary,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text("Gemini AI Fix", fontSize = 11.sp, color = NavyPrimary, fontWeight = FontWeight.SemiBold)
        }

        if (error.proposedFixCode.isNotBlank()) {
          if (!isApplied) {
            Button(
              onClick = onApplyFix,
              colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
              modifier = Modifier.height(32.dp)
            ) {
              Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Apply Fix", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
          } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Verified, contentDescription = null, tint = StatusGreen, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Patch Applied", fontSize = 11.sp, color = StatusGreen, fontWeight = FontWeight.Bold)
            }
          }
        }
      }

      // EXPANDED ACCORDION: Root Cause & Fix Details
      AnimatedVisibility(
        visible = isExpanded,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
            .background(Color(0xFFF8FAFC))
            .padding(10.dp)
        ) {
          // Root Cause
          Text("Root Cause Analysis", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
          Spacer(modifier = Modifier.height(2.dp))
          Text(error.rootCause, fontSize = 11.sp, color = Color(0xFF1E293B), lineHeight = 15.sp)

          // Solutions
          if (error.suggestedSolutions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Recommended Resolution Steps", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
            Spacer(modifier = Modifier.height(2.dp))
            error.suggestedSolutions.forEachIndexed { i, sol ->
              Text("${i + 1}. $sol", fontSize = 11.sp, color = Color(0xFF334155), lineHeight = 15.sp)
            }
          }

          // Proposed Fix Code
          if (error.proposedFixCode.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("Proposed Code Fix", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
              IconButton(
                onClick = { clipboardManager.setText(AnnotatedString(error.proposedFixCode)) },
                modifier = Modifier.size(24.dp)
              ) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy code", tint = NavyPrimary, modifier = Modifier.size(14.dp))
              }
            }
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = Color(0xFF1E293B),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = error.proposedFixCode,
                color = Color(0xFF38BDF8),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(8.dp)
              )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Why this works: ${error.fixExplanation}",
              fontSize = 10.sp,
              color = Color(0xFF64748B),
              lineHeight = 14.sp
            )
          }
        }
      }
    }
  }
}

@Composable
private fun EmptyIssuesState(
  totalCount: Int,
  filter: BuildIssueFilter,
  onReset: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = Color(0xFFF8FAFC),
    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Icon(
        imageVector = Icons.Default.CheckCircle,
        contentDescription = null,
        tint = StatusGreen,
        modifier = Modifier.size(36.dp)
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = if (totalCount == 0) "Zero Issues Detected" else "No Issues in Selected Filter",
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = TextPrimaryDark
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = if (totalCount == 0) {
          "Gradle build succeeded with no errors or warnings."
        } else {
          "Toggle other severity categories above to view warnings or info messages."
        },
        fontSize = 12.sp,
        color = TextSecondaryMuted,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
      )
      if (totalCount > 0 && !filter.isAllSelected) {
        Spacer(modifier = Modifier.height(10.dp))
        FilledTonalButton(onClick = onReset) {
          Text("Reset Filter (Show All $totalCount)", fontSize = 12.sp)
        }
      }
    }
  }
}
