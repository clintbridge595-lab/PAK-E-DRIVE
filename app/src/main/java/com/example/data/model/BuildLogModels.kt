package com.example.data.model

import com.example.data.gemini.BuildErrorSeverity
import java.util.UUID

enum class BuildLogLevel {
  INFO,
  WARN,
  ERROR,
  TASK,
  SUCCESS
}

enum class BuildResultStatus {
  IDLE,
  BUILDING,
  PASSED,
  WARNINGS,
  FAILED
}

data class BuildLogEntry(
  val id: String = UUID.randomUUID().toString(),
  val timestamp: Long = System.currentTimeMillis(),
  val level: BuildLogLevel = BuildLogLevel.INFO,
  val message: String,
  val taskName: String? = null,
  val moduleName: String? = null
)

data class BuildHealthStatus(
  val status: BuildResultStatus = BuildResultStatus.IDLE,
  val totalErrors: Int = 0,
  val criticalErrors: Int = 0,
  val warningCount: Int = 0,
  val infoCount: Int = 0,
  val modulesBuilt: Int = 0,
  val totalModules: Int = 5,
  val timeElapsedMillis: Long = 0L,
  val currentTask: String = "IDLE",
  val healthScore: Int = when {
    criticalErrors > 0 -> (100 - (criticalErrors * 25) - (warningCount * 10)).coerceIn(10, 50)
    warningCount > 0 -> (100 - (warningCount * 12)).coerceIn(55, 85)
    else -> 100
  }
) {
  val formattedElapsedTime: String
    get() {
      val totalSeconds = timeElapsedMillis / 1000
      val minutes = totalSeconds / 60
      val seconds = totalSeconds % 60
      val millis = (timeElapsedMillis % 1000) / 100
      return String.format("%02d:%02d.%d", minutes, seconds, millis)
    }

  val progressFraction: Float
    get() = if (totalModules > 0) (modulesBuilt.toFloat() / totalModules.toFloat()).coerceIn(0f, 1f) else 0f
}

data class BuildIssueFilter(
  val showCritical: Boolean = true,
  val showWarning: Boolean = true,
  val showInfo: Boolean = true
) {
  val isAllSelected: Boolean
    get() = showCritical && showWarning && showInfo

  fun matches(severity: BuildErrorSeverity): Boolean = when (severity) {
    BuildErrorSeverity.CRITICAL -> showCritical
    BuildErrorSeverity.WARNING -> showWarning
    BuildErrorSeverity.INFO -> showInfo
  }
}

enum class BuildScenario(
  val displayName: String,
  val description: String
) {
  COMPILATION_ERROR(
    "Kotlin Compilation Failure",
    "Unresolved reference in HomeScreen.kt with icon and property errors"
  ),
  MANIFEST_ERROR(
    "Manifest Merger Error",
    "Android 12+ requires explicit android:exported declaration"
  ),
  ROOM_WARNING(
    "Room Schema Export Warning",
    "Schema export directory is not provided to annotation processor"
  ),
  DEPENDENCY_ERROR(
    "Dependency Resolution Failure",
    "Could not find artifact or resolve maven repository dependency"
  ),
  MULTI_ISSUE_BUILD(
    "Full Build (Multiple Issues)",
    "Simulates real build discovering 1 Critical, 1 Warning, and 1 Info issue"
  ),
  CLEAN_SUCCESS(
    "Clean Successful Build",
    "All tasks up-to-date, zero errors found, 5/5 modules compiled"
  )
}
