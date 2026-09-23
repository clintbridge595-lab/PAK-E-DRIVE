package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.gemini.AnalysisResult
import com.example.data.gemini.BuildErrorSeverity
import com.example.data.gemini.GeminiBuildLogAnalyzer
import com.example.data.gemini.IdentifiedBuildError
import com.example.data.model.*
import com.example.data.repository.BuildLogRepository
import com.example.services.BuildOutputParserService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BuildLogViewModel(application: Application) : AndroidViewModel(application) {

  private val repository = BuildLogRepository.getInstance()

  val logs: StateFlow<List<BuildLogEntry>> = repository.logs
  val buildHealth: StateFlow<BuildHealthStatus> = repository.buildHealth
  val allIssues: StateFlow<List<IdentifiedBuildError>> = repository.identifiedErrors
  val isBuildActive: StateFlow<Boolean> = repository.isBuildActive
  val filterState: StateFlow<BuildIssueFilter> = repository.filterState

  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  private val _selectedScenario = MutableStateFlow(BuildScenario.CLEAN_SUCCESS)
  val selectedScenario: StateFlow<BuildScenario> = _selectedScenario.asStateFlow()

  private val _selectedIssue = MutableStateFlow<IdentifiedBuildError?>(null)
  val selectedIssue: StateFlow<IdentifiedBuildError?> = _selectedIssue.asStateFlow()

  private val _isAnalyzingWithGemini = MutableStateFlow(false)
  val isAnalyzingWithGemini: StateFlow<Boolean> = _isAnalyzingWithGemini.asStateFlow()

  private val _geminiResult = MutableStateFlow<AnalysisResult?>(null)
  val geminiResult: StateFlow<AnalysisResult?> = _geminiResult.asStateFlow()

  private val _appliedFixIds = MutableStateFlow<Set<String>>(emptySet())
  val appliedFixIds: StateFlow<Set<String>> = _appliedFixIds.asStateFlow()

  // Dynamic filtered issues based on user toggling Critical, Warning, Info
  val filteredIssues: StateFlow<List<IdentifiedBuildError>> = combine(
    allIssues,
    filterState
  ) { issues, filter ->
    issues.filter { filter.matches(it.severity) }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Dynamic filtered logs based on search query
  val filteredLogs: StateFlow<List<BuildLogEntry>> = combine(
    logs,
    _searchQuery
  ) { logList, query ->
    if (query.isBlank()) {
      logList
    } else {
      logList.filter {
        it.message.contains(query, ignoreCase = true) ||
          (it.taskName?.contains(query, ignoreCase = true) == true)
      }
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  init {
    // Populate an initial clean successful build so the system is 100% OK and error-free by default
    if (repository.logs.value.isEmpty()) {
      startBuildSimulation(BuildScenario.CLEAN_SUCCESS)
    }
  }

  fun startBuildSimulation(scenario: BuildScenario = _selectedScenario.value) {
    _selectedScenario.value = scenario
    _selectedIssue.value = null
    _geminiResult.value = null
    BuildOutputParserService.startBuildSimulation(getApplication(), scenario)
  }

  fun parseRawLog(rawLog: String) {
    _selectedIssue.value = null
    _geminiResult.value = null
    BuildOutputParserService.parseRawLog(getApplication(), rawLog)
  }

  fun stopBuild() {
    BuildOutputParserService.stopBuild(getApplication())
  }

  fun clearLogs() {
    repository.clearLogs()
    repository.clearErrors()
    _selectedIssue.value = null
    _geminiResult.value = null
  }

  fun setSearchQuery(query: String) {
    _searchQuery.value = query
  }

  fun setSelectedScenario(scenario: BuildScenario) {
    _selectedScenario.value = scenario
  }

  fun toggleSeverityFilter(severity: BuildErrorSeverity) {
    repository.toggleFilter(severity)
  }

  fun setSingleSeverityFilter(severity: BuildErrorSeverity?) {
    repository.setSingleFilter(severity)
  }

  fun selectAllFilters() {
    repository.setFilter(BuildIssueFilter(showCritical = true, showWarning = true, showInfo = true))
  }

  fun selectIssue(issue: IdentifiedBuildError?) {
    _selectedIssue.value = issue
    _geminiResult.value = null
    if (issue != null) {
      requestGeminiAiFix(issue)
    }
  }

  fun requestGeminiAiFix(issue: IdentifiedBuildError) {
    viewModelScope.launch {
      _isAnalyzingWithGemini.value = true
      try {
        val snippet = "${issue.errorType}\n${issue.sourceFile}:${issue.lineNumber ?: ""}\n${issue.originalSnippet}\n${issue.rootCause}"
        val result = GeminiBuildLogAnalyzer.analyzeBuildLog(snippet)
        _geminiResult.value = result
      } catch (_: Exception) {
        _geminiResult.value = AnalysisResult(
          isErrorIdentified = true,
          identifiedError = issue,
          rawSummary = issue.summary,
          isFixAvailable = issue.proposedFixCode.isNotBlank()
        )
      } finally {
        _isAnalyzingWithGemini.value = false
      }
    }
  }

  fun applyFix(issue: IdentifiedBuildError) {
    val key = "${issue.errorType}_${issue.sourceFile}_${issue.lineNumber}"
    _appliedFixIds.update { it + key }
  }

  fun isFixApplied(issue: IdentifiedBuildError): Boolean {
    val key = "${issue.errorType}_${issue.sourceFile}_${issue.lineNumber}"
    return _appliedFixIds.value.contains(key)
  }
}
