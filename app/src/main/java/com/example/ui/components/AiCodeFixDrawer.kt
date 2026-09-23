package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.gemini.AnalysisResult
import com.example.data.gemini.BuildErrorSeverity
import com.example.data.gemini.GeminiBuildLogAnalyzer
import com.example.data.gemini.IdentifiedBuildError
import com.example.data.local.CrashLog
import com.example.data.repository.CrashLogRepository
import com.example.ui.theme.*
import com.example.ui.viewmodel.BuildLogViewModel
import com.example.util.NetworkMonitor
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiCodeFixDrawer(
  onDismiss: () -> Unit,
  onNavigateToScreen: (String) -> Unit = {}
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val crashLogRepo = remember { CrashLogRepository.getInstance(context) }
  val networkMonitor = remember { NetworkMonitor(context) }
  val isOnline by networkMonitor.isOnline.collectAsState(initial = true)
  val crashLogs by crashLogRepo.getAllCrashLogs().collectAsState(initial = emptyList())
  val unsyncedCount by crashLogRepo.getUnsyncedCount().collectAsState(initial = 0)

  var selectedTab by remember { mutableIntStateOf(0) }
  var logInputText by remember { mutableStateOf("") }
  var isAnalyzing by remember { mutableStateOf(false) }
  var analysisResult by remember { mutableStateOf<AnalysisResult?>(null) }
  var selectedCrashLog by remember { mutableStateOf<CrashLog?>(null) }
  var syncStatusMessage by remember { mutableStateOf<String?>(null) }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    containerColor = MaterialTheme.colorScheme.surface,
    modifier = Modifier.testTag("ai_code_fix_drawer_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(0.92f)
        .padding(horizontal = 16.dp)
    ) {
      // Header Banner
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(PakGreen.copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = "Gemini AI",
            tint = PakGreen,
            modifier = Modifier.size(24.dp)
          )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Gemini AI Code Fix & Crash Logs",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "Offline Room Persistence • Auto-Sync with Firestore",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        IconButton(
          onClick = onDismiss,
          modifier = Modifier.testTag("close_ai_drawer_btn")
        ) {
          Icon(Icons.Default.Close, contentDescription = "Close")
        }
      }

      // Offline / Online Firestore Room Sync Status Bar
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(
          containerColor = if (isOnline) PakGreen.copy(alpha = 0.08f) else Color(0xFFFFF3E0)
        ),
        shape = RoundedCornerShape(12.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = if (isOnline) Icons.Default.CloudDone else Icons.Default.CloudOff,
            contentDescription = null,
            tint = if (isOnline) PakGreen else Color(0xFFE65100),
            modifier = Modifier.size(22.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = if (isOnline) "Connected • Firestore Auto-Sync Active" else "Offline • Storing Logs in Local Room DB",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = if (isOnline) PakGreen else Color(0xFFE65100)
            )
            Text(
              text = if (unsyncedCount > 0) "$unsyncedCount unsynced log(s) waiting for Firestore connection" else "All ${crashLogs.size} logs synchronized",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          if (unsyncedCount > 0) {
            FilledTonalButton(
              onClick = {
                scope.launch {
                  val result = crashLogRepo.syncOfflineLogsToFirestore()
                  syncStatusMessage = if (result.isSuccess) {
                    "Synced ${result.getOrNull()} logs to Firestore!"
                  } else {
                    "Sync queued (will retry when online)"
                  }
                }
              },
              modifier = Modifier.testTag("sync_firestore_btn"),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text("Sync Now", fontSize = 11.sp)
            }
          }
        }
      }

      syncStatusMessage?.let { msg ->
        Text(
          text = msg,
          color = PakGreen,
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium,
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
      }

      // Tab Row
      TabRow(
        selectedTabIndex = selectedTab,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.padding(top = 4.dp)
      ) {
        Tab(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text("Room Crash Logs")
              if (unsyncedCount > 0) {
                Spacer(modifier = Modifier.width(4.dp))
                Badge(containerColor = MaterialTheme.colorScheme.error) {
                  Text("$unsyncedCount")
                }
              }
            }
          }
        )
        Tab(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          text = { Text("Gemini AI Fix") }
        )
        Tab(
          selected = selectedTab == 2,
          onClick = { selectedTab = 2 },
          text = { Text("Preset Errors") }
        )
        Tab(
          selected = selectedTab == 3,
          onClick = { selectedTab = 3 },
          text = { Text("Build Health") }
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      when (selectedTab) {
        0 -> {
          // Tab 0: Room Local Crash Logs
          RoomCrashLogsTab(
            crashLogs = crashLogs,
            unsyncedCount = unsyncedCount,
            onAnalyzeWithGemini = { logText ->
              logInputText = logText
              selectedTab = 1
              scope.launch {
                isAnalyzing = true
                analysisResult = GeminiBuildLogAnalyzer.analyzeBuildLog(logText)
                isAnalyzing = false
              }
            },
            onSimulateCrash = {
              scope.launch {
                crashLogRepo.logException(
                  throwable = RuntimeException("Offline test exception: NullPointerException at CarRentalRepository.kt:42"),
                  tag = "Simulation_OfflineTest",
                  severity = "CRITICAL",
                  customMessage = "Simulated crash saved locally in Room while offline."
                )
                Toast.makeText(context, "Crash saved to Room! Sync will trigger when online.", Toast.LENGTH_SHORT).show()
              }
            },
            onClearLogs = {
              scope.launch {
                crashLogRepo.clearAll()
              }
            }
          )
        }
        1 -> {
          // Tab 1: Live Gemini AI Diagnosis
          GeminiAiDiagnosisTab(
            logInput = logInputText,
            onLogInputChange = { logInputText = it },
            isAnalyzing = isAnalyzing,
            analysisResult = analysisResult,
            onTriggerAnalysis = {
              scope.launch {
                isAnalyzing = true
                analysisResult = GeminiBuildLogAnalyzer.analyzeBuildLog(logInputText)
                isAnalyzing = false
              }
            }
          )
        }
        2 -> {
          // Tab 2: Error Presets
          ErrorPresetsTab(
            onSelectPreset = { presetTitle, presetSnippet ->
              logInputText = presetSnippet
              selectedTab = 1
              scope.launch {
                isAnalyzing = true
                analysisResult = GeminiBuildLogAnalyzer.analyzeBuildLog(presetSnippet)
                isAnalyzing = false
              }
            }
          )
        }
        3 -> {
          // Tab 3: Build Health Dashboard & Real-Time Logs
          val buildLogVm: BuildLogViewModel = viewModel()
          RealtimeBuildLogViewer(
            viewModel = buildLogVm,
            modifier = Modifier.fillMaxSize()
          )
        }
      }
    }
  }
}

@Composable
fun RoomCrashLogsTab(
  crashLogs: List<CrashLog>,
  unsyncedCount: Int,
  onAnalyzeWithGemini: (String) -> Unit,
  onSimulateCrash: () -> Unit,
  onClearLogs: () -> Unit
) {
  Column(modifier = Modifier.fillMaxSize()) {
    // Actions Row
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Stored Logs in Room (${crashLogs.size})",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold
      )
      Row {
        OutlinedButton(
          onClick = onSimulateCrash,
          modifier = Modifier.testTag("simulate_crash_btn"),
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
        ) {
          Icon(Icons.Default.BugReport, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Test Crash", fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.width(8.dp))
        if (crashLogs.isNotEmpty()) {
          TextButton(
            onClick = onClearLogs,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text("Clear", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
          }
        }
      }
    }

    if (crashLogs.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = PakGreen,
            modifier = Modifier.size(48.dp)
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "No crashes recorded yet!",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Use 'Test Crash' to simulate saving a crash log offline in Room.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(crashLogs, key = { it.id }) { log ->
          CrashLogItemCard(
            crashLog = log,
            onAnalyze = {
              val logSnippet = """
                [CRASH_LOG #${log.id}] Tag: ${log.tag}
                Severity: ${log.severity} | Time: ${Date(log.timestamp)}
                Message: ${log.message}
                Metadata: ${log.metadata}
                StackTrace:
                ${log.stackTrace}
              """.trimIndent()
              onAnalyzeWithGemini(logSnippet)
            }
          )
        }
      }
    }
  }
}

@Composable
fun CrashLogItemCard(
  crashLog: CrashLog,
  onAnalyze: () -> Unit
) {
  var isExpanded by remember { mutableStateOf(false) }
  val dateFormat = remember { SimpleDateFormat("dd MMM, HH:mm:ss", Locale.getDefault()) }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { isExpanded = !isExpanded }
      .testTag("crash_log_card_${crashLog.id}"),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ),
    shape = RoundedCornerShape(12.dp)
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Surface(
            color = if (crashLog.severity == "CRITICAL") MaterialTheme.colorScheme.errorContainer else Color(0xFFFFF3E0),
            shape = RoundedCornerShape(6.dp)
          ) {
            Text(
              text = crashLog.severity,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = if (crashLog.severity == "CRITICAL") MaterialTheme.colorScheme.onErrorContainer else Color(0xFFE65100),
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = crashLog.tag,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
          )
        }

        // Sync Status Badge
        Surface(
          color = if (crashLog.isSynced) PakGreen.copy(alpha = 0.15f) else Color(0xFFEDE7F6),
          shape = RoundedCornerShape(6.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = if (crashLog.isSynced) Icons.Default.CloudDone else Icons.Default.CloudOff,
              contentDescription = null,
              tint = if (crashLog.isSynced) PakGreen else Color(0xFF5E35B1),
              modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (crashLog.isSynced) "Synced" else "Room Local",
              fontSize = 10.sp,
              fontWeight = FontWeight.Medium,
              color = if (crashLog.isSynced) PakGreen else Color(0xFF5E35B1)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = crashLog.message,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = dateFormat.format(Date(crashLog.timestamp)),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 11.sp
      )

      AnimatedVisibility(visible = isExpanded) {
        Column(modifier = Modifier.padding(top = 8.dp)) {
          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = crashLog.stackTrace,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
              .fillMaxWidth()
              .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(6.dp))
              .padding(8.dp)
              .horizontalScroll(rememberScrollState())
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
      ) {
        Button(
          onClick = onAnalyze,
          colors = ButtonDefaults.buttonColors(containerColor = PakGreen),
          shape = RoundedCornerShape(8.dp),
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
          modifier = Modifier.testTag("analyze_crash_btn_${crashLog.id}")
        ) {
          Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Diagnose with Gemini", fontSize = 12.sp)
        }
      }
    }
  }
}

@Composable
fun GeminiAiDiagnosisTab(
  logInput: String,
  onLogInputChange: (String) -> Unit,
  isAnalyzing: Boolean,
  analysisResult: AnalysisResult?,
  onTriggerAnalysis: () -> Unit
) {
  val context = LocalContext.current
  val scrollState = rememberScrollState()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .padding(bottom = 24.dp)
  ) {
    Text(
      text = "Current Log / Stack Trace",
      style = MaterialTheme.typography.labelMedium,
      fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(4.dp))
    OutlinedTextField(
      value = logInput,
      onValueChange = onLogInputChange,
      placeholder = { Text("Paste build error or select a crash log from the Room tab...") },
      modifier = Modifier
        .fillMaxWidth()
        .height(130.dp)
        .testTag("gemini_log_input_field"),
      textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
      shape = RoundedCornerShape(8.dp)
    )

    Spacer(modifier = Modifier.height(8.dp))
    Button(
      onClick = onTriggerAnalysis,
      enabled = logInput.isNotBlank() && !isAnalyzing,
      modifier = Modifier
        .fillMaxWidth()
        .testTag("gemini_trigger_analysis_btn"),
      colors = ButtonDefaults.buttonColors(containerColor = PakGreen)
    ) {
      if (isAnalyzing) {
        CircularProgressIndicator(
          modifier = Modifier.size(18.dp),
          color = MaterialTheme.colorScheme.onPrimary,
          strokeWidth = 2.dp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Gemini is analyzing log & creating solution...")
      } else {
        Icon(Icons.Default.AutoAwesome, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Propose Code Fix with Gemini")
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Analysis Result Display
    analysisResult?.let { result ->
      val err = result.identifiedError
      if (err != null) {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("gemini_analysis_result_card"),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          shape = RoundedCornerShape(12.dp)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = err.errorType,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Surface(
                color = if (err.severity == BuildErrorSeverity.CRITICAL) MaterialTheme.colorScheme.errorContainer else Color(0xFFFFF3E0),
                shape = RoundedCornerShape(6.dp)
              ) {
                Text(
                  text = err.severity.name,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (err.severity == BuildErrorSeverity.CRITICAL) MaterialTheme.colorScheme.onErrorContainer else Color(0xFFE65100),
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "File: ${err.sourceFile}${err.lineNumber?.let { ":$it" } ?: ""}",
              fontFamily = FontFamily.Monospace,
              fontSize = 12.sp,
              color = PakGreen,
              fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "Summary:",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = err.summary,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "Root Cause:",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = err.rootCause,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (err.suggestedSolutions.isNotEmpty()) {
              Spacer(modifier = Modifier.height(10.dp))
              Text(
                text = "Proposed Solution Steps:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
              )
              err.suggestedSolutions.forEachIndexed { idx, sol ->
                Text(
                  text = "${idx + 1}. $sol",
                  style = MaterialTheme.typography.bodySmall,
                  modifier = Modifier.padding(vertical = 2.dp)
                )
              }
            }

            if (err.proposedFixCode.isNotBlank()) {
              Spacer(modifier = Modifier.height(10.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "Proposed Code Fix:",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = PakGreen
                )
                TextButton(
                  onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Gemini Code Fix", err.proposedFixCode))
                    Toast.makeText(context, "Code fix copied to clipboard!", Toast.LENGTH_SHORT).show()
                  },
                  modifier = Modifier.testTag("copy_proposed_fix_btn")
                ) {
                  Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Copy Fix", fontSize = 12.sp)
                }
              }

              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
                  .padding(10.dp)
                  .horizontalScroll(rememberScrollState())
              ) {
                Text(
                  text = err.proposedFixCode,
                  fontFamily = FontFamily.Monospace,
                  fontSize = 11.sp,
                  color = Color(0xFF81C784)
                )
              }

              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = err.fixExplanation,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
              )
            }
          }
        }
      } else {
        Text(
          text = result.rawSummary,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurface
        )
      }
    }
  }
}

@Composable
fun ErrorPresetsTab(
  onSelectPreset: (String, String) -> Unit
) {
  val presets = remember {
    listOf(
      "NullPointerException on AWT Event Queue (KSP)" to """
        Exception in thread "AWT-EventQueue-0" java.lang.NullPointerException: Cannot invoke "ksp.com.intellij.openapi.application.Application.getService(java.lang.Class)" because the return value of "ksp.com.intellij.openapi.application.ApplicationManager.getApplication()" is null
        at com.squareup.moshi.kotlin.codegen.ksp.MoshiSymbolProcessor.process(MoshiSymbolProcessor.kt:45)
      """.trimIndent(),

      "Firestore Offline Sync Timeout (504/408)" to """
        com.google.firebase.firestore.FirebaseFirestoreException: Failed to get document because the client is offline or network timed out (504 GATEWAY_TIMEOUT).
        at com.example.offline.OfflineSyncManager.syncPendingBookings(OfflineSyncManager.kt:85)
      """.trimIndent(),

      "Room Database Destructive Migration Missing" to """
        java.lang.IllegalStateException: Room cannot verify the data integrity. Looks like you've changed schema but forgot to update the version number. You can simply also call fallbackToDestructiveMigration().
        at androidx.room.RoomOpenHelper.onUpgrade(RoomOpenHelper.java:120)
      """.trimIndent(),

      "Unresolved Compose Icon Reference" to """
        > Task :app:compileDebugKotlin FAILED
        e: file:///app/src/main/java/com/example/ui/screens/HomeScreen.kt:66:76 Unresolved reference 'Icons.Default.DirectionsCar'.
        BUILD FAILED in 12s
      """.trimIndent()
    )
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    items(presets) { (title, snippet) ->
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onSelectPreset(title, snippet) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(10.dp)
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Code, contentDescription = null, tint = PakGreen, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = snippet,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            maxLines = 3,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
  }
}
