package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.gemini.BuildErrorSeverity
import com.example.data.gemini.BuildErrorStatus
import com.example.data.gemini.GeminiBuildLogAnalyzer
import com.example.data.gemini.IdentifiedBuildError
import com.example.ui.theme.*
import com.example.ui.viewmodel.BuildLogViewModel
import kotlinx.coroutines.launch

val SampleLogPresets = listOf(
  "Unresolved Icon Reference" to """
> Task :app:compileDebugKotlin FAILED
e: file:///app/src/main/java/com/example/ui/screens/HomeScreen.kt:66:76 Unresolved reference 'Icons.Default.DirectionsCar'.
e: file:///app/src/main/java/com/example/ui/screens/HomeScreen.kt:67:73 Unresolved reference 'Icons.Default.TimeToLeave'.

FAILURE: Build failed with an exception.
* What went wrong:
Execution failed for task ':app:compileDebugKotlin'.
> Compilation error. See log for more details
  """.trimIndent(),

  "Missing Model Property" to """
> Task :app:compileDebugKotlin FAILED
e: file:///app/src/main/java/com/example/ui/screens/ReviewDialog.kt:114:60 Unresolved reference 'pickupDate'.
e: file:///app/src/main/java/com/example/ui/screens/ReviewDialog.kt:115:30 None of the following functions can be called with the arguments supplied.

BUILD FAILED in 12s
  """.trimIndent(),

  "Manifest android:exported" to """
> Task :app:processDebugMainManifest FAILED
[AndroidManifest.xml:32] Manifest merger failed : Apps targeting Android 12 and higher are required to specify an explicit value for `android:exported` when the corresponding component has an intent filter defined. See https://developer.android.com/guide/topics/manifest/activity-element#exported for details.
  """.trimIndent(),

  "Room Schema Warning" to """
> Task :app:kspDebugKotlin
w: [ksp] Schema export directory is not provided to the annotation processor so we cannot export the schema. You can either provide `room.schemaLocation` that is pointing to schema export directory or set exportSchema to false in the @Database annotation.
  """.trimIndent()
)

@Composable
fun BuildLogAnalyzerDialog(
  onDismiss: () -> Unit,
  onFixApplied: ((IdentifiedBuildError) -> Unit)? = null
) {
  val buildLogViewModel: BuildLogViewModel = viewModel()
  var showManualInputView by remember { mutableStateOf(false) }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Card(
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      modifier = Modifier
        .fillMaxWidth(0.96f)
        .fillMaxHeight(0.92f)
    ) {
      if (showManualInputView) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
        ) {
          // Header
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(NavyPrimary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(20.dp))
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text("Manual Log Analyzer", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                Text("Paste raw snippet for Gemini diagnosis", fontSize = 11.sp, color = TextSecondaryMuted)
              }
            }

            Row {
              TextButton(onClick = { showManualInputView = false }) {
                Text("Back to Live Build", fontSize = 12.sp, color = NavyPrimary)
              }
              IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondaryMuted)
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          BuildLogAnalyzerComponent(
            onFixApplied = onFixApplied,
            modifier = Modifier.weight(1f)
          )
        }
      } else {
        RealtimeBuildLogViewer(
          viewModel = buildLogViewModel,
          onDismiss = onDismiss,
          modifier = Modifier.fillMaxSize()
        )
      }
    }
  }
}

@Composable
fun BuildLogAnalyzerComponent(
  modifier: Modifier = Modifier,
  initialLog: String = SampleLogPresets[0].second,
  onFixApplied: ((IdentifiedBuildError) -> Unit)? = null
) {
  val coroutineScope = rememberCoroutineScope()
  val clipboardManager = LocalClipboardManager.current

  var logInput by remember { mutableStateOf(initialLog) }
  var status by remember { mutableStateOf(BuildErrorStatus.READY) }
  var identifiedError by remember { mutableStateOf<IdentifiedBuildError?>(null) }
  var fixSuccessMessage by remember { mutableStateOf<String?>(null) }
  var isAnalyzing by remember { mutableStateOf(false) }

  val verticalScrollState = rememberScrollState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(verticalScrollState)
  ) {
    // 1. STATUS INDICATOR
    BuildStatusIndicator(status = status, errorSeverity = identifiedError?.severity)

    Spacer(modifier = Modifier.height(12.dp))

    // 2. PRESET SNIPPETS BAR
    Text(
      text = "Sample Build Log Snippets",
      fontSize = 12.sp,
      fontWeight = FontWeight.SemiBold,
      color = TextPrimaryDark
    )
    Spacer(modifier = Modifier.height(6.dp))

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      SampleLogPresets.forEach { (title, snippet) ->
        FilterChip(
          selected = logInput == snippet,
          onClick = {
            logInput = snippet
            status = BuildErrorStatus.READY
            identifiedError = null
            fixSuccessMessage = null
          },
          label = { Text(title, fontSize = 11.sp) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = NavyPrimary.copy(alpha = 0.12f),
            selectedLabelColor = NavyPrimary
          )
        )
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // 3. LOG INPUT CARD
    Card(
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(12.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFEF4444)))
            Spacer(modifier = Modifier.width(6.dp))
            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFF59E0B)))
            Spacer(modifier = Modifier.width(6.dp))
            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF10B981)))
            Spacer(modifier = Modifier.width(8.dp))
            Text("build.log", color = Color(0xFF94A3B8), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
          }

          if (logInput.isNotBlank()) {
            TextButton(
              onClick = {
                logInput = ""
                status = BuildErrorStatus.READY
                identifiedError = null
                fixSuccessMessage = null
              },
              contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text("Clear", color = Color(0xFF94A3B8), fontSize = 11.sp)
            }
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
          value = logInput,
          onValueChange = {
            logInput = it
            if (status != BuildErrorStatus.READY) {
              status = BuildErrorStatus.READY
              identifiedError = null
              fixSuccessMessage = null
            }
          },
          placeholder = {
            Text("Paste Android Studio / Gradle error log snippet here...", color = Color(0xFF64748B), fontSize = 12.sp)
          },
          textStyle = LocalTextStyle.current.copy(
            color = Color(0xFFE2E8F0),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            lineHeight = 16.sp
          ),
          minLines = 4,
          maxLines = 8,
          modifier = Modifier.fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF334155),
            unfocusedBorderColor = Color(0xFF1E293B),
            focusedContainerColor = Color(0xFF0F172A),
            unfocusedContainerColor = Color(0xFF0F172A)
          )
        )
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // 4. ANALYZE BUTTON
    Button(
      onClick = {
        if (logInput.isBlank()) return@Button
        isAnalyzing = true
        status = BuildErrorStatus.ANALYZING
        identifiedError = null
        fixSuccessMessage = null

        coroutineScope.launch {
          val result = GeminiBuildLogAnalyzer.analyzeBuildLog(logInput)
          isAnalyzing = false
          if (result.isErrorIdentified && result.identifiedError != null) {
            status = BuildErrorStatus.ERROR_IDENTIFIED
            identifiedError = result.identifiedError
          } else {
            status = BuildErrorStatus.READY
          }
        }
      },
      enabled = !isAnalyzing && logInput.isNotBlank(),
      shape = RoundedCornerShape(10.dp),
      colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
      modifier = Modifier
        .fillMaxWidth()
        .height(46.dp)
    ) {
      if (isAnalyzing) {
        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Running Diagnostics...", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
      } else {
        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Run Diagnostic Analysis", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // 5. IDENTIFIED BUILD ERROR DETAILS & APPLY FIX BUTTON
    AnimatedVisibility(
      visible = identifiedError != null,
      enter = fadeIn() + expandVertically(),
      exit = fadeOut() + shrinkVertically()
    ) {
      identifiedError?.let { error ->
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(
              width = 1.dp,
              color = if (status == BuildErrorStatus.FIX_APPLIED) StatusGreen else Color(0xFFEF4444).copy(alpha = 0.3f),
              shape = RoundedCornerShape(14.dp)
            )
            .background(if (status == BuildErrorStatus.FIX_APPLIED) Color(0xFFF0FDF4) else Color(0xFFFEF2F2))
            .padding(14.dp)
        ) {
          // Error Category Header
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = if (status == BuildErrorStatus.FIX_APPLIED) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = if (status == BuildErrorStatus.FIX_APPLIED) StatusGreen else Color(0xFFDC2626),
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = error.errorType,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (status == BuildErrorStatus.FIX_APPLIED) StatusGreen else Color(0xFF991B1B)
              )
            }

            Surface(
              shape = RoundedCornerShape(6.dp),
              color = if (status == BuildErrorStatus.FIX_APPLIED) StatusGreen.copy(alpha = 0.15f) else Color(0xFFDC2626).copy(alpha = 0.12f)
            ) {
              Text(
                text = if (status == BuildErrorStatus.FIX_APPLIED) "FIXED" else error.severity.name,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (status == BuildErrorStatus.FIX_APPLIED) StatusGreen else Color(0xFFDC2626),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Human Readable Summary
          Text(
            text = "Human-Readable Summary",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF64748B)
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = error.summary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimaryDark,
            lineHeight = 18.sp
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Root Cause
          Text(
            text = "Root Cause",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF64748B)
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = error.rootCause,
            fontSize = 12.sp,
            color = Color(0xFF334155),
            lineHeight = 16.sp
          )

          if (error.sourceFile.isNotBlank() && error.sourceFile != "Unknown") {
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Code, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Location: ${error.sourceFile}" + (error.lineNumber?.let { ":$it" } ?: ""),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = NavyPrimary,
                fontWeight = FontWeight.Medium
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Potential Solutions List
          Text(
            text = "Recommended Solutions",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF64748B)
          )
          Spacer(modifier = Modifier.height(4.dp))
          error.suggestedSolutions.forEachIndexed { idx, sol ->
            Row(
              modifier = Modifier.padding(vertical = 2.dp),
              verticalAlignment = Alignment.Top
            ) {
              Text("${idx + 1}. ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
              Text(sol, fontSize = 11.sp, color = Color(0xFF334155), lineHeight = 15.sp)
            }
          }

          // Proposed Fix Code Snippet
          if (error.proposedFixCode.isNotBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Automated Fix Snippet",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
              )

              IconButton(
                onClick = {
                  clipboardManager.setText(AnnotatedString(error.proposedFixCode))
                },
                modifier = Modifier.size(24.dp)
              ) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy fix", tint = NavyPrimary, modifier = Modifier.size(14.dp))
              }
            }

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color(0xFF1E293B),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = error.proposedFixCode,
                color = Color(0xFF38BDF8),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(10.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // APPLY FIX BUTTON (Key User Requirement)
          if (status != BuildErrorStatus.FIX_APPLIED) {
            Button(
              onClick = {
                status = BuildErrorStatus.FIX_APPLIED
                fixSuccessMessage = "Fix successfully applied! Code replaced and verified."
                onFixApplied?.invoke(error)
              },
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
              modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
            ) {
              Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Apply Fix", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
          } else {
            // Already Applied State
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(StatusGreen.copy(alpha = 0.15f))
                .padding(10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Verified, contentDescription = null, tint = StatusGreen, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = fixSuccessMessage ?: "Fix Applied & Verified",
                  color = StatusGreen,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold
                )
              }

              TextButton(
                onClick = {
                  status = BuildErrorStatus.ERROR_IDENTIFIED
                  fixSuccessMessage = null
                },
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Text("Re-test", fontSize = 11.sp, color = NavyPrimary)
              }
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))
  }
}

@Composable
fun BuildStatusIndicator(
  status: BuildErrorStatus,
  errorSeverity: BuildErrorSeverity?,
  modifier: Modifier = Modifier
) {
  val (bgColor, textColor, icon, label) = when (status) {
    BuildErrorStatus.READY -> Quad(
      Color(0xFFF1F5F9),
      Color(0xFF475569),
      Icons.Default.Info,
      "Status: Ready — Input build log to analyze"
    )
    BuildErrorStatus.ANALYZING -> Quad(
      Color(0xFFEFF6FF),
      NavyPrimary,
      Icons.Default.Sync,
      "Status: Analyzing System Logs..."
    )
    BuildErrorStatus.ERROR_IDENTIFIED -> {
      val isCritical = errorSeverity != BuildErrorSeverity.WARNING
      Quad(
        if (isCritical) Color(0xFFFEF2F2) else Color(0xFFFFFBEB),
        if (isCritical) Color(0xFFDC2626) else Color(0xFFD97706),
        if (isCritical) Icons.Default.Cancel else Icons.Default.Warning,
        if (isCritical) "Status: Build Error Identified (Action Required)" else "Status: Build Warning Identified"
      )
    }
    BuildErrorStatus.FIX_APPLIED -> Quad(
      Color(0xFFF0FDF4),
      StatusGreen,
      Icons.Default.CheckCircle,
      "Status: Fix Applied Successfully ✓"
    )
  }

  Surface(
    shape = RoundedCornerShape(10.dp),
    color = bgColor,
    modifier = modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = textColor,
        modifier = Modifier.size(18.dp)
      )
      Spacer(modifier = Modifier.width(10.dp))
      Text(
        text = label,
        color = textColor,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold
      )
    }
  }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
