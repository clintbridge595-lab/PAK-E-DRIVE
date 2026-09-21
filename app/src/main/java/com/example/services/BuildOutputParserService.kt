package com.example.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.data.gemini.BuildErrorSeverity
import com.example.data.gemini.GeminiBuildLogAnalyzer
import com.example.data.gemini.IdentifiedBuildError
import com.example.data.model.*
import com.example.data.repository.BuildLogRepository
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Background Service that parses Android and Gradle build output in real time,
 * classifies errors (Critical, Warning, Info), tracks build health metrics (modules built,
 * elapsed time, error counts), and automatically invokes Gemini API to suggest AI-generated fixes.
 */
class BuildOutputParserService : Service() {

  private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
  private val repository = BuildLogRepository.getInstance()
  private val isRunning = AtomicBoolean(false)
  private var buildJob: Job? = null

  override fun onBind(intent: Intent?): IBinder? = null

  override fun onCreate() {
    super.onCreate()
    createNotificationChannel()
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    when (intent?.action) {
      ACTION_START_GRADLE_BUILD -> {
        val scenarioName = intent.getStringExtra(EXTRA_SCENARIO) ?: BuildScenario.MULTI_ISSUE_BUILD.name
        val scenario = try {
          BuildScenario.valueOf(scenarioName)
        } catch (_: Exception) {
          BuildScenario.MULTI_ISSUE_BUILD
        }
        startGradleBuildSimulation(scenario)
      }

      ACTION_PARSE_RAW_LOG -> {
        val rawLog = intent.getStringExtra(EXTRA_RAW_LOG) ?: ""
        if (rawLog.isNotBlank()) {
          parseAndAnalyzeRawLog(rawLog)
        }
      }

      ACTION_STOP_BUILD -> {
        stopCurrentBuild()
      }
    }
    return START_NOT_STICKY
  }

  private fun startGradleBuildSimulation(scenario: BuildScenario) {
    buildJob?.cancel()
    buildJob = serviceScope.launch {
      isRunning.set(true)
      repository.setBuildActive(true)
      repository.clearLogs()
      repository.clearErrors()

      val startTime = System.currentTimeMillis()
      val totalModules = 5
      val modules = listOf(":core:model", ":core:database", ":core:network", ":feature:rental", ":app")

      repository.setBuildHealth(
        BuildHealthStatus(
          status = BuildResultStatus.BUILDING,
          totalErrors = 0,
          criticalErrors = 0,
          warningCount = 0,
          infoCount = 0,
          modulesBuilt = 0,
          totalModules = totalModules,
          timeElapsedMillis = 0L,
          currentTask = "> Initializing Gradle daemon...",
          healthScore = 100
        )
      )

      // Background timer updater
      val timerJob = launch {
        while (isActive && isRunning.get()) {
          val elapsed = System.currentTimeMillis() - startTime
          repository.updateHealth { it.copy(timeElapsedMillis = elapsed) }
          delay(150)
        }
      }

      try {
        val script = getScenarioLogScript(scenario)
        var currentModuleIdx = 0

        for (step in script) {
          if (!isActive || !isRunning.get()) break

          // Emit log entry
          val entry = BuildLogEntry(
            level = step.level,
            message = step.message,
            taskName = step.taskName,
            moduleName = step.moduleName
          )
          repository.appendLog(entry)

          // Update current task
          if (step.taskName != null) {
            repository.updateHealth { it.copy(currentTask = step.taskName) }
          }

          // If a module completed its primary task, increment modulesBuilt
          if (step.isModuleComplete && currentModuleIdx < totalModules) {
            currentModuleIdx++
            val builtCount = currentModuleIdx
            repository.updateHealth { it.copy(modulesBuilt = builtCount) }
          }

          // If step contains an error or warning, trigger automated Gemini AI fix parsing
          if (step.detectedIssueSnippet != null) {
            parseAndAnalyzeSnippetWithGemini(step.detectedIssueSnippet, step.forcedSeverity)
          }

          delay(step.delayMillis)
        }

        // Finalize build health
        val finalElapsed = System.currentTimeMillis() - startTime
        repository.updateHealth { current ->
          val resultStatus = when {
            current.criticalErrors > 0 -> BuildResultStatus.FAILED
            current.warningCount > 0 -> BuildResultStatus.WARNINGS
            else -> BuildResultStatus.PASSED
          }
          current.copy(
            status = resultStatus,
            currentTask = if (resultStatus == BuildResultStatus.PASSED) "BUILD SUCCESSFUL" else "BUILD FAILED",
            timeElapsedMillis = finalElapsed
          )
        }

      } catch (e: CancellationException) {
        Log.d(TAG, "Build simulation cancelled")
      } catch (e: Exception) {
        Log.e(TAG, "Error in build parsing service", e)
      } finally {
        timerJob.cancel()
        isRunning.set(false)
        repository.setBuildActive(false)
      }
    }
  }

  private fun parseAndAnalyzeRawLog(rawLog: String) {
    serviceScope.launch {
      repository.setBuildActive(true)
      repository.clearLogs()
      repository.clearErrors()

      val startTime = System.currentTimeMillis()
      val lines = rawLog.lines()

      repository.setBuildHealth(
        BuildHealthStatus(
          status = BuildResultStatus.BUILDING,
          totalModules = 5,
          modulesBuilt = 3,
          currentTask = "Parsing provided build log...",
          timeElapsedMillis = 0L
        )
      )

      val logEntries = mutableListOf<BuildLogEntry>()
      val issueSnippets = mutableListOf<String>()
      var currentSnippet = StringBuilder()
      var collectingIssue = false

      for (line in lines) {
        val level = when {
          line.contains("FAILED") || line.contains("e: ") || line.contains("error:") -> BuildLogLevel.ERROR
          line.contains("w: ") || line.contains("warning:") || line.contains("Warning") -> BuildLogLevel.WARN
          line.startsWith("> Task") -> BuildLogLevel.TASK
          line.contains("SUCCESS") -> BuildLogLevel.SUCCESS
          else -> BuildLogLevel.INFO
        }

        logEntries.add(BuildLogEntry(level = level, message = line))

        if (level == BuildLogLevel.ERROR || level == BuildLogLevel.WARN) {
          collectingIssue = true
          currentSnippet.appendLine(line)
        } else if (collectingIssue) {
          if (line.isBlank() || line.startsWith("> Task")) {
            collectingIssue = false
            if (currentSnippet.isNotBlank()) {
              issueSnippets.add(currentSnippet.toString().trim())
              currentSnippet = StringBuilder()
            }
          } else {
            currentSnippet.appendLine(line)
          }
        }
      }

      if (currentSnippet.isNotBlank()) {
        issueSnippets.add(currentSnippet.toString().trim())
      }

      repository.appendLogs(logEntries)

      // If no separated snippets found but rawLog contains errors, treat whole log as snippet
      if (issueSnippets.isEmpty() && (rawLog.contains("FAILED") || rawLog.contains("e: ") || rawLog.contains("error"))) {
        issueSnippets.add(rawLog)
      }

      // Analyze each identified issue via Gemini API
      for (snippet in issueSnippets.take(4)) {
        parseAndAnalyzeSnippetWithGemini(snippet, null)
      }

      val elapsed = System.currentTimeMillis() - startTime
      repository.updateHealth { current ->
        val finalStatus = when {
          current.criticalErrors > 0 -> BuildResultStatus.FAILED
          current.warningCount > 0 -> BuildResultStatus.WARNINGS
          else -> BuildResultStatus.PASSED
        }
        current.copy(
          status = finalStatus,
          modulesBuilt = 5,
          timeElapsedMillis = elapsed,
          currentTask = if (finalStatus == BuildResultStatus.FAILED) "BUILD FAILED" else "ANALYSIS COMPLETE"
        )
      }

      repository.setBuildActive(false)
    }
  }

  private suspend fun parseAndAnalyzeSnippetWithGemini(snippet: String, forcedSeverity: BuildErrorSeverity?) {
    try {
      val analysisResult = GeminiBuildLogAnalyzer.analyzeBuildLog(snippet)
      if (analysisResult.isErrorIdentified && analysisResult.identifiedError != null) {
        val errorToStore = if (forcedSeverity != null && analysisResult.identifiedError.severity != forcedSeverity) {
          analysisResult.identifiedError.copy(severity = forcedSeverity)
        } else {
          analysisResult.identifiedError
        }
        repository.addIdentifiedError(errorToStore)
      }
    } catch (e: Exception) {
      Log.e(TAG, "Gemini API analysis failed in background service", e)
    }
  }

  private fun stopCurrentBuild() {
    isRunning.set(false)
    buildJob?.cancel()
    repository.setBuildActive(false)
    repository.updateHealth { it.copy(status = BuildResultStatus.IDLE, currentTask = "BUILD STOPPED") }
  }

  private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        CHANNEL_ID,
        "Build Log Parser & AI Diagnosis",
        NotificationManager.IMPORTANCE_LOW
      ).apply {
        description = "Monitors real-time Gradle compilation and automatically suggests Gemini AI fixes"
      }
      val manager = getSystemService(NotificationManager::class.java)
      manager?.createNotificationChannel(channel)
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    serviceScope.cancel()
  }

  companion object {
    private const val TAG = "BuildOutputParserService"
    const val CHANNEL_ID = "build_parser_channel"

    const val ACTION_START_GRADLE_BUILD = "com.example.services.action.START_GRADLE_BUILD"
    const val ACTION_PARSE_RAW_LOG = "com.example.services.action.PARSE_RAW_LOG"
    const val ACTION_STOP_BUILD = "com.example.services.action.STOP_BUILD"

    const val EXTRA_SCENARIO = "extra_scenario"
    const val EXTRA_RAW_LOG = "extra_raw_log"

    fun startBuildSimulation(context: Context, scenario: BuildScenario) {
      val intent = Intent(context, BuildOutputParserService::class.java).apply {
        action = ACTION_START_GRADLE_BUILD
        putExtra(EXTRA_SCENARIO, scenario.name)
      }
      context.startService(intent)
    }

    fun parseRawLog(context: Context, rawLog: String) {
      val intent = Intent(context, BuildOutputParserService::class.java).apply {
        action = ACTION_PARSE_RAW_LOG
        putExtra(EXTRA_RAW_LOG, rawLog)
      }
      context.startService(intent)
    }

    fun stopBuild(context: Context) {
      val intent = Intent(context, BuildOutputParserService::class.java).apply {
        action = ACTION_STOP_BUILD
      }
      context.startService(intent)
    }
  }
}

private data class BuildSimulationStep(
  val level: BuildLogLevel,
  val message: String,
  val taskName: String? = null,
  val moduleName: String? = null,
  val isModuleComplete: Boolean = false,
  val detectedIssueSnippet: String? = null,
  val forcedSeverity: BuildErrorSeverity? = null,
  val delayMillis: Long = 180L
)

private fun getScenarioLogScript(scenario: BuildScenario): List<BuildSimulationStep> = when (scenario) {
  BuildScenario.COMPILATION_ERROR -> listOf(
    BuildSimulationStep(BuildLogLevel.INFO, "Starting Gradle Daemon (build session #402)...", delayMillis = 150),
    BuildSimulationStep(BuildLogLevel.TASK, "> Task :core:model:preBuild UP-TO-DATE", taskName = ":core:model:preBuild", moduleName = ":core:model"),
    BuildSimulationStep(BuildLogLevel.TASK, "> Task :core:model:compileDebugKotlin UP-TO-DATE", taskName = ":core:model:compileDebugKotlin", moduleName = ":core:model", isModuleComplete = true, delayMillis = 200),
    BuildSimulationStep(BuildLogLevel.TASK, "> Task :core:database:kspDebugKotlin UP-TO-DATE", taskName = ":core:database:kspDebugKotlin", moduleName = ":core:database", isModuleComplete = true, delayMillis = 250),
    BuildSimulationStep(BuildLogLevel.TASK, "> Task :core:network:compileDebugKotlin UP-TO-DATE", taskName = ":core:network:compileDebugKotlin", moduleName = ":core:network", isModuleComplete = true, delayMillis = 200),
    BuildSimulationStep(BuildLogLevel.TASK, "> Task :feature:rental:compileDebugKotlin UP-TO-DATE", taskName = ":feature:rental:compileDebugKotlin", moduleName = ":feature:rental", isModuleComplete = true, delayMillis = 220),
    BuildSimulationStep(BuildLogLevel.TASK, "> Task :app:compileDebugKotlin FAILED", taskName = ":app:compileDebugKotlin", moduleName = ":app", delayMillis = 350),
    BuildSimulationStep(
      BuildLogLevel.ERROR,
      "e: file:///app/src/main/java/com/example/ui/screens/HomeScreen.kt:66:76 Unresolved reference 'Icons.Default.DirectionsCar'.",
      taskName = ":app:compileDebugKotlin",
      detectedIssueSnippet = "e: file:///app/src/main/java/com/example/ui/screens/HomeScreen.kt:66:76 Unresolved reference 'Icons.Default.DirectionsCar'.",
      forcedSeverity = BuildErrorSeverity.CRITICAL,
      delayMillis = 250
    ),
    BuildSimulationStep(
      BuildLogLevel.WARN,
      "w: [ksp] Schema export directory is not provided to annotation processor. Database schema migrations cannot be exported.",
      taskName = ":app:kspDebugKotlin",
      detectedIssueSnippet = "w: [ksp] Schema export directory is not provided so we cannot export the schema. Set exportSchema to false.",
      forcedSeverity = BuildErrorSeverity.WARNING,
      delayMillis = 250
    ),
    BuildSimulationStep(
      BuildLogLevel.INFO,
      "i: [gradle] Note: Some input files use or override a deprecated API (LocalConfiguration.current).",
      taskName = ":app:compileDebugKotlin",
      detectedIssueSnippet = "i: Note: Some input files use deprecated Compose APIs. Consider updating to WindowSizeClass.",
      forcedSeverity = BuildErrorSeverity.INFO,
      delayMillis = 200
    ),
    BuildSimulationStep(BuildLogLevel.ERROR, "FAILURE: Build failed with an exception.", delayMillis = 150),
    BuildSimulationStep(BuildLogLevel.ERROR, "* What went wrong:\nExecution failed for task ':app:compileDebugKotlin'.\n> Compilation error. See log for details.", delayMillis = 100),
    BuildSimulationStep(BuildLogLevel.ERROR, "BUILD FAILED in 4s", delayMillis = 100)
  )

  BuildScenario.MANIFEST_ERROR -> listOf(
    BuildSimulationStep(BuildLogLevel.TASK, "> Task :core:model:compileDebugKotlin UP-TO-DATE", taskName = ":core:model:compileDebugKotlin", moduleName = ":core:model", isModuleComplete = true, delayMillis = 200),
    BuildSimulationStep(BuildLogLevel.TASK, "> Task :core:database:compileDebugKotlin UP-TO-DATE", taskName = ":core:database:compileDebugKotlin", moduleName = ":core:database", isModuleComplete = true, delayMillis = 200),
    BuildSimulationStep(BuildLogLevel.TASK, "> Task :core:network:compileDebugKotlin UP-TO-DATE", taskName = ":core:network:compileDebugKotlin", moduleName = ":core:network", isModuleComplete = true, delayMillis = 200),
    BuildSimulationStep(BuildLogLevel.TASK, "> Task :feature:rental:compileDebugKotlin UP-TO-DATE", taskName = ":feature:rental:compileDebugKotlin", moduleName = ":feature:rental", isModuleComplete = true, delayMillis = 200),
    BuildSimulationStep(BuildLogLevel.TASK, "> Task :app:processDebugMainManifest FAILED", taskName = ":app:processDebugMainManifest", moduleName = ":app", delayMillis = 350),
    BuildSimulationStep(
      BuildLogLevel.ERROR,
      "[AndroidManifest.xml:42] Manifest merger failed : Apps targeting Android 12 and higher are required to specify an explicit value for `android:exported` when the corresponding component has an intent filter defined.",
      taskName = ":app:processDebugMainManifest",
      detectedIssueSnippet = "[AndroidManifest.xml:42] Manifest merger failed : Apps targeting Android 12 and higher are required to specify an explicit value for `android:exported`.",
      forcedSeverity = BuildErrorSeverity.CRITICAL,
      delayMillis = 300
    ),
    BuildSimulationStep(
      BuildLogLevel.INFO,
      "i: [manifest] Default application orientation locked to portrait; tablet landscape multi-window supported.",
      taskName = ":app:processDebugMainManifest",
      detectedIssueSnippet = "i: [manifest] Adaptive screen sizing enabled.",
      forcedSeverity = BuildErrorSeverity.INFO,
      delayMillis = 200
    ),
    BuildSimulationStep(BuildLogLevel.ERROR, "BUILD FAILED in 6s", delayMillis = 100)
  )

  BuildScenario.ROOM_WARNING -> listOf(
    BuildSimulationStep(BuildLogLevel.TASK, "> Task :core:model:compileDebugKotlin UP-TO-DATE", taskName = ":core:model:compileDebugKotlin", isModuleComplete = true, delayMillis = 180),
    BuildSimulationStep(BuildLogLevel.TASK, "> Task :core:database:kspDebugKotlin", taskName = ":core:database:kspDebugKotlin", isModuleComplete = true, delayMillis = 220),
    BuildSimulationStep(
      BuildLogLevel.WARN,
      "w: [ksp] Schema export directory is not provided to the annotation processor so we cannot export the schema. Set exportSchema to false.",
      taskName = ":core:database:kspDebugKotlin",
      detectedIssueSnippet = "w: [ksp] Schema export directory is not provided so we cannot export the schema. You can either provide `room.schemaLocation` or set exportSchema to false.",
      forcedSeverity = BuildErrorSeverity.WARNING,
      delayMillis = 250
    ),
    BuildSimulationStep(BuildLogLevel.TASK, "> Task :core:network:compileDebugKotlin UP-TO-DATE", isModuleComplete = true, delayMillis = 180),
    BuildSimulationStep(BuildLogLevel.TASK, "> Task :feature:rental:compileDebugKotlin UP-TO-DATE", isModuleComplete = true, delayMillis = 180),
    BuildSimulationStep(BuildLogLevel.TASK, "> Task :app:compileDebugKotlin UP-TO-DATE", isModuleComplete = true, delayMillis = 180),
    BuildSimulationStep(BuildLogLevel.SUCCESS, "BUILD SUCCESSFUL in 8s (with 1 warning)", delayMillis = 100)
  )

  BuildScenario.DEPENDENCY_ERROR -> listOf(
    BuildSimulationStep(BuildLogLevel.TASK, "> Task :core:model:compileDebugKotlin UP-TO-DATE", isModuleComplete = true, delayMillis = 180),
    BuildSimulationStep(BuildLogLevel.TASK, "> Task :core:network:compileDebugKotlin FAILED", taskName = ":core:network:compileDebugKotlin", delayMillis = 300),
    BuildSimulationStep(
      BuildLogLevel.ERROR,
      "Could not find com.example.unknown:lib:1.0 in mavenCentral, google, or maven { url 'https://jitpack.io' }.",
      taskName = ":core:network:compileDebugKotlin",
      detectedIssueSnippet = "Could not resolve com.example.unknown:lib:1.0. Required by: project :core:network.",
      forcedSeverity = BuildErrorSeverity.CRITICAL,
      delayMillis = 250
    ),
    BuildSimulationStep(BuildLogLevel.ERROR, "BUILD FAILED in 3s", delayMillis = 100)
  )

  BuildScenario.MULTI_ISSUE_BUILD -> listOf(
    BuildSimulationStep(BuildLogLevel.INFO, "Starting Gradle build process...", delayMillis = 120),
    BuildSimulationStep(BuildLogLevel.TASK, "> Task :core:model:compileDebugKotlin UP-TO-DATE", taskName = ":core:model:compileDebugKotlin", moduleName = ":core:model", isModuleComplete = true, delayMillis = 200),
    BuildSimulationStep(BuildLogLevel.TASK, "> Task :core:database:kspDebugKotlin", taskName = ":core:database:kspDebugKotlin", moduleName = ":core:database", isModuleComplete = true, delayMillis = 250),
    BuildSimulationStep(
      BuildLogLevel.WARN,
      "w: [ksp] Schema export directory is not provided to the annotation processor. Set exportSchema to false in @Database.",
      taskName = ":core:database:kspDebugKotlin",
      detectedIssueSnippet = "w: [ksp] Schema export directory is not provided to annotation processor so we cannot export schema.",
      forcedSeverity = BuildErrorSeverity.WARNING,
      delayMillis = 250
    ),
    BuildSimulationStep(BuildLogLevel.TASK, "> Task :core:network:compileDebugKotlin UP-TO-DATE", taskName = ":core:network:compileDebugKotlin", moduleName = ":core:network", isModuleComplete = true, delayMillis = 200),
    BuildSimulationStep(BuildLogLevel.TASK, "> Task :feature:rental:compileDebugKotlin UP-TO-DATE", taskName = ":feature:rental:compileDebugKotlin", moduleName = ":feature:rental", isModuleComplete = true, delayMillis = 200),
    BuildSimulationStep(BuildLogLevel.TASK, "> Task :app:processDebugMainManifest UP-TO-DATE", taskName = ":app:processDebugMainManifest", delayMillis = 180),
    BuildSimulationStep(
      BuildLogLevel.INFO,
      "i: [build-cache] Configuration cache entry reused. 42 actionable tasks: 3 executed, 39 up-to-date.",
      taskName = ":app:buildCache",
      detectedIssueSnippet = "i: Configuration cache enabled. Tasks executing in parallel.",
      forcedSeverity = BuildErrorSeverity.INFO,
      delayMillis = 200
    ),
    BuildSimulationStep(BuildLogLevel.TASK, "> Task :app:compileDebugKotlin FAILED", taskName = ":app:compileDebugKotlin", moduleName = ":app", delayMillis = 350),
    BuildSimulationStep(
      BuildLogLevel.ERROR,
      "e: file:///app/src/main/java/com/example/ui/screens/HomeScreen.kt:66:76 Unresolved reference 'Icons.Default.DirectionsCar'.",
      taskName = ":app:compileDebugKotlin",
      detectedIssueSnippet = "e: file:///app/src/main/java/com/example/ui/screens/HomeScreen.kt:66:76 Unresolved reference 'Icons.Default.DirectionsCar'.",
      forcedSeverity = BuildErrorSeverity.CRITICAL,
      delayMillis = 250
    ),
    BuildSimulationStep(BuildLogLevel.ERROR, "BUILD FAILED in 7s (1 Critical Error, 1 Warning, 1 Info)", delayMillis = 100)
  )

  BuildScenario.CLEAN_SUCCESS -> listOf(
    BuildSimulationStep(BuildLogLevel.INFO, "Starting Gradle build...", delayMillis = 100),
    BuildSimulationStep(BuildLogLevel.TASK, "> Task :core:model:compileDebugKotlin UP-TO-DATE", taskName = ":core:model:compileDebugKotlin", moduleName = ":core:model", isModuleComplete = true, delayMillis = 150),
    BuildSimulationStep(BuildLogLevel.TASK, "> Task :core:database:compileDebugKotlin UP-TO-DATE", taskName = ":core:database:compileDebugKotlin", moduleName = ":core:database", isModuleComplete = true, delayMillis = 150),
    BuildSimulationStep(BuildLogLevel.TASK, "> Task :core:network:compileDebugKotlin UP-TO-DATE", taskName = ":core:network:compileDebugKotlin", moduleName = ":core:network", isModuleComplete = true, delayMillis = 150),
    BuildSimulationStep(BuildLogLevel.TASK, "> Task :feature:rental:compileDebugKotlin UP-TO-DATE", taskName = ":feature:rental:compileDebugKotlin", moduleName = ":feature:rental", isModuleComplete = true, delayMillis = 150),
    BuildSimulationStep(BuildLogLevel.TASK, "> Task :app:compileDebugKotlin UP-TO-DATE", taskName = ":app:compileDebugKotlin", moduleName = ":app", isModuleComplete = true, delayMillis = 150),
    BuildSimulationStep(BuildLogLevel.TASK, "> Task :app:assembleDebug UP-TO-DATE", taskName = ":app:assembleDebug", delayMillis = 150),
    BuildSimulationStep(BuildLogLevel.SUCCESS, "BUILD SUCCESSFUL in 12s", delayMillis = 100)
  )
}
