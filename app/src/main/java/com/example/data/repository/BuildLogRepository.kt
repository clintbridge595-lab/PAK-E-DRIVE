package com.example.data.repository

import com.example.data.gemini.BuildErrorSeverity
import com.example.data.gemini.IdentifiedBuildError
import com.example.data.model.BuildHealthStatus
import com.example.data.model.BuildIssueFilter
import com.example.data.model.BuildLogEntry
import com.example.data.model.BuildResultStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class BuildLogRepository private constructor() {

  private val _logs = MutableStateFlow<List<BuildLogEntry>>(emptyList())
  val logs: StateFlow<List<BuildLogEntry>> = _logs.asStateFlow()

  private val _buildHealth = MutableStateFlow(BuildHealthStatus())
  val buildHealth: StateFlow<BuildHealthStatus> = _buildHealth.asStateFlow()

  private val _identifiedErrors = MutableStateFlow<List<IdentifiedBuildError>>(emptyList())
  val identifiedErrors: StateFlow<List<IdentifiedBuildError>> = _identifiedErrors.asStateFlow()

  private val _isBuildActive = MutableStateFlow(false)
  val isBuildActive: StateFlow<Boolean> = _isBuildActive.asStateFlow()

  private val _filterState = MutableStateFlow(BuildIssueFilter())
  val filterState: StateFlow<BuildIssueFilter> = _filterState.asStateFlow()

  fun appendLog(entry: BuildLogEntry) {
    _logs.update { current ->
      // Keep max 1000 lines in buffer for performance
      if (current.size > 1000) current.drop(100) + entry else current + entry
    }
  }

  fun appendLogs(entries: List<BuildLogEntry>) {
    _logs.update { current ->
      val combined = current + entries
      if (combined.size > 1000) combined.takeLast(1000) else combined
    }
  }

  fun clearLogs() {
    _logs.value = emptyList()
  }

  fun setBuildActive(active: Boolean) {
    _isBuildActive.value = active
  }

  fun updateHealth(transform: (BuildHealthStatus) -> BuildHealthStatus) {
    _buildHealth.update(transform)
  }

  fun setBuildHealth(health: BuildHealthStatus) {
    _buildHealth.value = health
  }

  fun addIdentifiedError(error: IdentifiedBuildError) {
    _identifiedErrors.update { current ->
      // Avoid duplicate errors by summary & line
      if (current.any { it.summary == error.summary && it.lineNumber == error.lineNumber && it.sourceFile == error.sourceFile }) {
        current
      } else {
        current + error
      }
    }
    recalculateHealth()
  }

  fun setIdentifiedErrors(errors: List<IdentifiedBuildError>) {
    _identifiedErrors.value = errors
    recalculateHealth()
  }

  fun clearErrors() {
    _identifiedErrors.value = emptyList()
    recalculateHealth()
  }

  fun toggleFilter(severity: BuildErrorSeverity) {
    _filterState.update { current ->
      when (severity) {
        BuildErrorSeverity.CRITICAL -> current.copy(showCritical = !current.showCritical)
        BuildErrorSeverity.WARNING -> current.copy(showWarning = !current.showWarning)
        BuildErrorSeverity.INFO -> current.copy(showInfo = !current.showInfo)
      }
    }
  }

  fun setSingleFilter(severity: BuildErrorSeverity?) {
    _filterState.update {
      if (severity == null) {
        BuildIssueFilter(showCritical = true, showWarning = true, showInfo = true)
      } else {
        BuildIssueFilter(
          showCritical = severity == BuildErrorSeverity.CRITICAL,
          showWarning = severity == BuildErrorSeverity.WARNING,
          showInfo = severity == BuildErrorSeverity.INFO
        )
      }
    }
  }

  fun setFilter(filter: BuildIssueFilter) {
    _filterState.value = filter
  }

  private fun recalculateHealth() {
    val errors = _identifiedErrors.value
    val critical = errors.count { it.severity == BuildErrorSeverity.CRITICAL }
    val warnings = errors.count { it.severity == BuildErrorSeverity.WARNING }
    val info = errors.count { it.severity == BuildErrorSeverity.INFO }
    val total = errors.size

    val score = when {
      critical > 0 -> (100 - (critical * 25) - (warnings * 10)).coerceIn(10, 50)
      warnings > 0 -> (100 - (warnings * 12)).coerceIn(55, 85)
      else -> 100
    }

    val resultStatus = when {
      _isBuildActive.value -> BuildResultStatus.BUILDING
      critical > 0 -> BuildResultStatus.FAILED
      warnings > 0 -> BuildResultStatus.WARNINGS
      else -> BuildResultStatus.PASSED
    }

    _buildHealth.update { current ->
      current.copy(
        status = resultStatus,
        totalErrors = total,
        criticalErrors = critical,
        warningCount = warnings,
        infoCount = info,
        healthScore = score
      )
    }
  }

  companion object {
    @Volatile
    private var INSTANCE: BuildLogRepository? = null

    fun getInstance(): BuildLogRepository {
      return INSTANCE ?: synchronized(this) {
        INSTANCE ?: BuildLogRepository().also { INSTANCE = it }
      }
    }
  }
}
