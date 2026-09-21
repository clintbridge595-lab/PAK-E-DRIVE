package com.example

import com.example.data.gemini.BuildErrorSeverity
import com.example.data.gemini.IdentifiedBuildError
import com.example.data.model.*
import com.example.data.repository.BuildLogRepository
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class BuildLogMonitoringTest {

  private lateinit var repository: BuildLogRepository

  @Before
  fun setUp() {
    repository = BuildLogRepository.getInstance()
    repository.clearLogs()
    repository.clearErrors()
    repository.setFilter(BuildIssueFilter(showCritical = true, showWarning = true, showInfo = true))
  }

  @Test
  fun testBuildIssueFilter_matching() {
    val filter = BuildIssueFilter(showCritical = true, showWarning = false, showInfo = false)
    assertTrue(filter.matches(BuildErrorSeverity.CRITICAL))
    assertFalse(filter.matches(BuildErrorSeverity.WARNING))
    assertFalse(filter.matches(BuildErrorSeverity.INFO))

    val allFilter = BuildIssueFilter(showCritical = true, showWarning = true, showInfo = true)
    assertTrue(allFilter.isAllSelected)
  }

  @Test
  fun testBuildHealthStatus_scoreCalculation() {
    val healthyStatus = BuildHealthStatus(
      totalErrors = 0,
      criticalErrors = 0,
      warningCount = 0,
      infoCount = 0,
      modulesBuilt = 5,
      totalModules = 5,
      status = BuildResultStatus.PASSED
    )
    assertEquals(100, healthyStatus.healthScore)

    val failureStatus = BuildHealthStatus(
      totalErrors = 3,
      criticalErrors = 2,
      warningCount = 1,
      infoCount = 0,
      modulesBuilt = 2,
      totalModules = 5,
      status = BuildResultStatus.FAILED
    )
    // 100 - (2 * 25) - (1 * 10) = 40
    assertEquals(40, failureStatus.healthScore)
  }

  @Test
  fun testRepository_logAndErrorTracking() {
    val testError = IdentifiedBuildError(
      errorType = "Unresolved Reference",
      severity = BuildErrorSeverity.CRITICAL,
      sourceFile = "HomeScreen.kt",
      lineNumber = 66,
      summary = "Icons.Default.DirectionsCar unresolved",
      rootCause = "Missing material-icons-extended dependency",
      suggestedSolutions = listOf("Use core icons"),
      proposedFixCode = "Icon(Icons.Filled.DirectionsCar, ...)",
      originalSnippet = "Icons.Default.DirectionsCar",
      fixExplanation = "Use core icons bundle"
    )

    repository.addIdentifiedError(testError)
    assertEquals(1, repository.identifiedErrors.value.size)
    assertEquals(BuildResultStatus.FAILED, repository.buildHealth.value.status)
    assertEquals(1, repository.buildHealth.value.criticalErrors)

    val testWarn = IdentifiedBuildError(
      errorType = "Room Schema Export",
      severity = BuildErrorSeverity.WARNING,
      sourceFile = "AppDatabase.kt",
      lineNumber = 10,
      summary = "Schema export warning",
      rootCause = "Schema location not configured",
      suggestedSolutions = listOf("Set exportSchema to false"),
      proposedFixCode = "exportSchema = false",
      originalSnippet = "exportSchema = true",
      fixExplanation = "Disable exportSchema"
    )

    repository.addIdentifiedError(testWarn)
    assertEquals(2, repository.identifiedErrors.value.size)
    assertEquals(1, repository.buildHealth.value.criticalErrors)
    assertEquals(1, repository.buildHealth.value.warningCount)
  }

  @Test
  fun testBuildScenarios_definedAndValid() {
    val scenarios = BuildScenario.values()
    assertTrue(scenarios.isNotEmpty())
    scenarios.forEach { scenario ->
      assertTrue(scenario.displayName.isNotBlank())
      assertTrue(scenario.description.isNotBlank())
    }
  }
}
