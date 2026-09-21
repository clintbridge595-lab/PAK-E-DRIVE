package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.gemini.BuildErrorSeverity
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.BuildLogViewModel
import kotlinx.coroutines.launch

/**
 * Real-time Build Log Viewer component that displays streaming Gradle logs,
 * syntax highlighting, search filter, auto-scrolling, scenario selection,
 * and integrated status dashboard with error filtering.
 */
@Composable
fun RealtimeBuildLogViewer(
  viewModel: BuildLogViewModel,
  onDismiss: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val logs by viewModel.filteredLogs.collectAsState()
  val rawLogs by viewModel.logs.collectAsState()
  val health by viewModel.buildHealth.collectAsState()
  val allIssues by viewModel.allIssues.collectAsState()
  val filteredIssues by viewModel.filteredIssues.collectAsState()
  val filterState by viewModel.filterState.collectAsState()
  val isBuildActive by viewModel.isBuildActive.collectAsState()
  val searchQuery by viewModel.searchQuery.collectAsState()
  val selectedScenario by viewModel.selectedScenario.collectAsState()
  val isAiAnalyzing by viewModel.isAnalyzingWithGemini.collectAsState()

  var selectedTab by remember { mutableIntStateOf(0) }
  var autoScroll by remember { mutableStateOf(true) }
  var showScenarioMenu by remember { mutableStateOf(false) }

  val listState = rememberLazyListState()
  val coroutineScope = rememberCoroutineScope()
  val clipboardManager = LocalClipboardManager.current

  // Auto-scroll when new logs arrive
  LaunchedEffect(logs.size, autoScroll) {
    if (autoScroll && logs.isNotEmpty()) {
      listState.animateScrollToItem(logs.size - 1)
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(BackgroundLight)
      .testTag("realtime_build_log_viewer")
  ) {
    // 1. Header Toolbar
    Surface(
      color = Color.White,
      shadowElevation = 2.dp,
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(NavyPrimary.copy(alpha = 0.1f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Terminal,
                contentDescription = null,
                tint = NavyPrimary,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Gradle Real-Time Diagnostics",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = NavyPrimary
              )
              Text(
                text = "AI-Powered Android Build Monitor",
                fontSize = 11.sp,
                color = TextSecondaryMuted
              )
            }
          }

          if (onDismiss != null) {
            IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
              Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondaryMuted)
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Scenario Picker & Build Controls
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Scenario Dropdown
          Box {
            OutlinedButton(
              onClick = { showScenarioMenu = true },
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
              modifier = Modifier.height(34.dp)
            ) {
              Text(
                text = selectedScenario.displayName,
                fontSize = 11.sp,
                maxLines = 1,
                color = NavyPrimary,
                fontWeight = FontWeight.SemiBold
              )
              Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = NavyPrimary)
            }

            DropdownMenu(
              expanded = showScenarioMenu,
              onDismissRequest = { showScenarioMenu = false }
            ) {
              BuildScenario.values().forEach { scenario ->
                DropdownMenuItem(
                  text = {
                    Column {
                      Text(scenario.displayName, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                      Text(scenario.description, fontSize = 10.sp, color = TextSecondaryMuted)
                    }
                  },
                  onClick = {
                    viewModel.setSelectedScenario(scenario)
                    showScenarioMenu = false
                    viewModel.startBuildSimulation(scenario)
                  }
                )
              }
            }
          }

          // Play / Stop / Clear Actions
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            if (isBuildActive) {
              Button(
                onClick = { viewModel.stopBuild() },
                colors = ButtonDefaults.buttonColors(containerColor = StatusRed),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(34.dp)
              ) {
                Icon(Icons.Default.Stop, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Stop", fontSize = 11.sp)
              }
            } else {
              Button(
                onClick = { viewModel.startBuildSimulation() },
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(34.dp)
              ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Run Build", fontSize = 11.sp)
              }
            }

            OutlinedButton(
              onClick = { viewModel.clearLogs() },
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
              modifier = Modifier.height(34.dp)
            ) {
              Icon(Icons.Default.DeleteSweep, contentDescription = "Clear logs", tint = TextSecondaryMuted, modifier = Modifier.size(16.dp))
            }
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Navigation Tabs: Dashboard & Errors vs Live Console
        TabRow(
          selectedTabIndex = selectedTab,
          containerColor = Color.White,
          contentColor = NavyPrimary,
          modifier = Modifier.fillMaxWidth()
        ) {
          Tab(
            selected = selectedTab == 0,
            onClick = { selectedTab = 0 },
            text = {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Health & Errors", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                if (allIssues.isNotEmpty()) {
                  Spacer(modifier = Modifier.width(6.dp))
                  Badge(containerColor = if (health.criticalErrors > 0) StatusRed else StatusAmber) {
                    Text("${allIssues.size}", fontSize = 10.sp)
                  }
                }
              }
            }
          )
          Tab(
            selected = selectedTab == 1,
            onClick = { selectedTab = 1 },
            text = {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Live Terminal (${rawLogs.size})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                if (isBuildActive) {
                  Spacer(modifier = Modifier.width(6.dp))
                  Box(
                    modifier = Modifier
                      .size(8.dp)
                      .clip(CircleShape)
                      .background(OrangeAccent)
                  )
                }
              }
            }
          )
        }
      }
    }

    // 2. Tab Content
    when (selectedTab) {
      0 -> {
        // TAB 0: STATUS DASHBOARD & ERROR FILTER SYSTEM
        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          // Status Dashboard UI
          item {
            BuildStatusDashboard(
              health = health,
              isBuildActive = isBuildActive,
              onSeverityClick = { severity ->
                viewModel.setSingleSeverityFilter(severity)
              }
            )
          }

          // Error List with Filtering System
          item {
            ErrorListFilterSystem(
              allErrors = allIssues,
              filteredErrors = filteredIssues,
              filter = filterState,
              onToggleSeverity = { severity -> viewModel.toggleSeverityFilter(severity) },
              onSelectSingleSeverity = { severity -> viewModel.setSingleSeverityFilter(severity) },
              onSelectAll = { viewModel.selectAllFilters() },
              onRequestAiFix = { issue -> viewModel.requestGeminiAiFix(issue) },
              onApplyFix = { issue -> viewModel.applyFix(issue) },
              isFixApplied = { issue -> viewModel.isFixApplied(issue) },
              isAiAnalyzing = isAiAnalyzing
            )
          }
        }
      }

      1 -> {
        // TAB 1: LIVE CONSOLE LOGS CAPTURED FROM GRADLE
        Column(
          modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
        ) {
          // Search & Console Controls
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(Color(0xFF1E293B))
              .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            // Search Input
            OutlinedTextField(
              value = searchQuery,
              onValueChange = { viewModel.setSearchQuery(it) },
              placeholder = { Text("Filter logs...", color = Color(0xFF64748B), fontSize = 11.sp) },
              leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(16.dp)) },
              trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                  IconButton(onClick = { viewModel.setSearchQuery("") }, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
                  }
                }
              },
              singleLine = true,
              textStyle = LocalTextStyle.current.copy(
                color = Color.White,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
              ),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF38BDF8),
                unfocusedBorderColor = Color(0xFF334155),
                focusedContainerColor = Color(0xFF0F172A),
                unfocusedContainerColor = Color(0xFF0F172A)
              ),
              modifier = Modifier
                .weight(1f)
                .height(40.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Auto-scroll toggle
            FilterChip(
              selected = autoScroll,
              onClick = { autoScroll = !autoScroll },
              label = { Text("Auto-Scroll", fontSize = 10.sp) },
              leadingIcon = {
                Icon(
                  if (autoScroll) Icons.Default.VerticalAlignBottom else Icons.Default.Pause,
                  contentDescription = null,
                  modifier = Modifier.size(12.dp)
                )
              },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = NavyLight,
                selectedLabelColor = Color.White,
                selectedLeadingIconColor = Color.White,
                containerColor = Color(0xFF0F172A),
                labelColor = Color(0xFF94A3B8)
              ),
              modifier = Modifier.height(32.dp)
            )

            Spacer(modifier = Modifier.width(4.dp))

            // Copy all logs
            IconButton(
              onClick = {
                val fullLog = logs.joinToString("\n") { it.message }
                clipboardManager.setText(AnnotatedString(fullLog))
              },
              modifier = Modifier.size(32.dp)
            ) {
              Icon(Icons.Default.ContentCopy, contentDescription = "Copy all logs", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
            }
          }

          // Terminal Output Stream
          LazyColumn(
            state = listState,
            modifier = Modifier
              .fillMaxSize()
              .padding(horizontal = 12.dp, vertical = 8.dp)
          ) {
            items(logs, key = { it.id }) { entry ->
              LogLineRow(
                entry = entry,
                onClick = {
                  if (entry.level == BuildLogLevel.ERROR || entry.level == BuildLogLevel.WARN) {
                    selectedTab = 0 // jump to diagnostics
                  }
                }
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun LogLineRow(
  entry: BuildLogEntry,
  onClick: () -> Unit
) {
  val (lineColor, bgAlpha, icon) = when (entry.level) {
    BuildLogLevel.ERROR -> Triple(Color(0xFFEF4444), 0.15f, Icons.Default.Cancel)
    BuildLogLevel.WARN -> Triple(Color(0xFFF59E0B), 0.12f, Icons.Default.Warning)
    BuildLogLevel.TASK -> Triple(Color(0xFF38BDF8), 0.08f, Icons.Default.PlayArrow)
    BuildLogLevel.SUCCESS -> Triple(Color(0xFF10B981), 0.12f, Icons.Default.Check)
    BuildLogLevel.INFO -> Triple(Color(0xFF94A3B8), 0f, null)
  }

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(4.dp))
      .then(
        if (bgAlpha > 0f) Modifier.background(lineColor.copy(alpha = bgAlpha)) else Modifier
      )
      .clickable(enabled = entry.level == BuildLogLevel.ERROR || entry.level == BuildLogLevel.WARN, onClick = onClick)
      .padding(horizontal = 6.dp, vertical = 2.dp),
    verticalAlignment = Alignment.Top
  ) {
    if (icon != null) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = lineColor,
        modifier = Modifier
          .padding(top = 2.dp, end = 6.dp)
          .size(12.dp)
      )
    } else {
      Spacer(modifier = Modifier.width(18.dp))
    }

    Text(
      text = entry.message,
      color = lineColor,
      fontSize = 11.sp,
      fontFamily = FontFamily.Monospace,
      lineHeight = 15.sp
    )
  }
}
