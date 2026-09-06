package com.intellinav.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.intellinav.core.fusion.FusionEngine
import com.intellinav.core.mapmatching.HmmMapMatcher
import com.intellinav.core.model.FixQuality
import com.intellinav.core.model.GeoPoint
import com.intellinav.core.model.GnssFix
import com.intellinav.core.model.ImuSample
import com.intellinav.core.model.MapMatchedResult
import com.intellinav.core.model.NavigationMode
import com.intellinav.core.model.NavigationState
import com.intellinav.core.model.TelemetryStats
import com.intellinav.core.model.VehicleProfile
import com.intellinav.core.sensor.GnssDataSource
import com.intellinav.core.sensor.MockSensorProvider
import com.intellinav.core.simulation.NavigationScenario
import com.intellinav.core.simulation.ScenarioWaypoint
import com.intellinav.core.simulation.TunnelScenarios
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

class MainViewModel : ViewModel() {
  private val _uiState: MutableStateFlow<MainUiState> = MutableStateFlow(value = MainUiState())
  val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

  private var fusionEngine: FusionEngine = FusionEngine(
    vehicleProfile = VehicleProfile.TwoWheeler,
    referenceOrigin = TunnelScenarios.AtalTunnel.waypoints.first().geoPoint,
  )
  private var mapMatcher: HmmMapMatcher = HmmMapMatcher(osmGraph = TunnelScenarios.AtalTunnel.osmGraph)
  private val sensorProvider: MockSensorProvider = MockSensorProvider()
  private val gnssDataSource: GnssDataSource = GnssDataSource()

  private var simulationJob: Job? = null
  private var currentWaypointIndex: Int = 0
  private var subStepFraction: Float = 0f
  private var lastFrozenGpsPoint: GeoPoint? = null

  init {
    startSimulationLoop()
  }

  fun toggleBlackout() {
    val newBlackout: Boolean = !_uiState.value.isBlackoutForced
    gnssDataSource.setBlackout(blackout = newBlackout)
    _uiState.update { state ->
      state.copy(isBlackoutForced = newBlackout)
    }
  }

  fun selectScenario(scenario: NavigationScenario) {
    currentWaypointIndex = 0
    subStepFraction = 0f
    lastFrozenGpsPoint = null

    val startOrigin: GeoPoint = scenario.waypoints.first().geoPoint
    fusionEngine = FusionEngine(
      vehicleProfile = _uiState.value.activeVehicleProfile,
      referenceOrigin = startOrigin,
    )
    mapMatcher = HmmMapMatcher(osmGraph = scenario.osmGraph)

    _uiState.update { state ->
      state.copy(
        currentScenario = scenario,
        waypointProgressIndex = 0,
        fusedPath = listOf(element = startOrigin),
        rawGpsPath = listOf(element = startOrigin),
        groundTruthPath = listOf(element = startOrigin),
        currentManeuverInstruction = "Proceed towards ${scenario.waypoints.first().roadName}",
        tunnelRemainingMeters = scenario.tunnelDistanceMeters,
      )
    }
  }

  fun selectVehicleProfile(profile: VehicleProfile) {
    fusionEngine.setVehicleProfile(profile = profile)
    _uiState.update { state ->
      state.copy(activeVehicleProfile = profile)
    }
  }

  fun setSimulationSpeed(multiplier: Float) {
    _uiState.update { state ->
      state.copy(simulationSpeedMultiplier = multiplier)
    }
  }

  fun togglePlayPause() {
    val willSimulate: Boolean = !_uiState.value.isSimulating
    _uiState.update { state -> state.copy(isSimulating = willSimulate) }
    if (willSimulate && simulationJob == null) {
      startSimulationLoop()
    }
  }

  fun resetSimulation() {
    selectScenario(scenario = _uiState.value.currentScenario)
  }

  private fun startSimulationLoop() {
    simulationJob?.cancel()
    simulationJob = viewModelScope.launch {
      while (true) {
        val delayMillis: Long = (50L / _uiState.value.simulationSpeedMultiplier).toLong().coerceAtLeast(minimumValue = 10L)
        delay(timeMillis = delayMillis)
        if (!_uiState.value.isSimulating) continue

        performSimulationTick()
      }
    }
  }

  private fun performSimulationTick() {
    val state: MainUiState = _uiState.value
    val waypoints: List<ScenarioWaypoint> = state.currentScenario.waypoints
    if (waypoints.isEmpty()) return

    if (currentWaypointIndex >= waypoints.size - 1) {
      currentWaypointIndex = 0
      subStepFraction = 0f
    }

    val wp1: ScenarioWaypoint = waypoints[currentWaypointIndex]
    val wp2: ScenarioWaypoint = waypoints[min(a = currentWaypointIndex + 1, b = waypoints.size - 1)]

    // Advance fraction
    subStepFraction += 0.04f
    if (subStepFraction >= 1.0f) {
      subStepFraction = 0f
      currentWaypointIndex++
    }

    // Ground Truth position
    val trueLat: Double = wp1.geoPoint.latitude + (subStepFraction * (wp2.geoPoint.latitude - wp1.geoPoint.latitude))
    val trueLon: Double = wp1.geoPoint.longitude + (subStepFraction * (wp2.geoPoint.longitude - wp1.geoPoint.longitude))
    val groundTruthPoint: GeoPoint = GeoPoint(latitude = trueLat, longitude = trueLon)

    val inTunnelBlackout: Boolean = wp1.isTunnelBlackout || state.isBlackoutForced

    // 1. Generate IMU sample from simulated kinematics
    val targetSpeed: Float = wp1.speedMps
    val imuSample: ImuSample = sensorProvider.generateSample(
      targetSpeedMps = targetSpeed,
      turnRateRadSec = 0f,
      isStop = false,
    )

    // 2. GNSS Fix generation
    val isGpsHealthy: Boolean = !inTunnelBlackout
    val currentGpsPoint: GeoPoint = if (isGpsHealthy) {
      lastFrozenGpsPoint = groundTruthPoint
      groundTruthPoint
    } else {
      // In tunnel blackout: Standard GPS freezes at the tunnel entrance!
      lastFrozenGpsPoint ?: groundTruthPoint
    }

    val gnssFix: GnssFix = GnssFix(
      timestampMs = System.currentTimeMillis(),
      latitude = currentGpsPoint.latitude,
      longitude = currentGpsPoint.longitude,
      speedMps = if (isGpsHealthy) targetSpeed else 0f,
      bearingDegrees = wp1.headingDegrees,
      horizontalAccuracyMeters = if (isGpsHealthy) 2.8f else 150f,
      satellitesUsed = if (isGpsHealthy) 16 else 0,
      quality = if (isGpsHealthy) FixQuality.STANDARD_GPS else FixQuality.NO_FIX,
      isBlackout = !isGpsHealthy,
    )

    // 3. Sensor Fusion Step (UKF)
    fusionEngine.processGnss(fix = gnssFix)
    fusionEngine.processImu(sample = imuSample)

    // 4. Offline Map Matching Step (OSM + HMM + NHC)
    val rawNavState: NavigationState = NavigationState(
      geoPoint = groundTruthPoint,
      headingDegrees = wp1.headingDegrees,
      mode = if (inTunnelBlackout) NavigationMode.DEAD_RECKONING_TUNNEL else NavigationMode.GNSS_FUSED,
    )
    val matched: MapMatchedResult = mapMatcher.match(state = rawNavState)

    // 5. Update UI State and History Trails
    val updatedFusedPath: MutableList<GeoPoint> = state.fusedPath.toMutableList()
    val updatedGpsPath: MutableList<GeoPoint> = state.rawGpsPath.toMutableList()
    val updatedTruthPath: MutableList<GeoPoint> = state.groundTruthPath.toMutableList()

    if (updatedFusedPath.size > 200) updatedFusedPath.removeAt(index = 0)
    if (updatedGpsPath.size > 200) updatedGpsPath.removeAt(index = 0)
    if (updatedTruthPath.size > 200) updatedTruthPath.removeAt(index = 0)

    updatedFusedPath.add(element = matched.matchedPoint)
    updatedGpsPath.add(element = currentGpsPoint)
    updatedTruthPath.add(element = groundTruthPoint)

    // Recent sensor waveform points for live meters
    val newAccelHistory: List<Float> = (state.recentAccelSamples + imuSample.linearAcceleration.y).takeLast(n = 30)
    val newGyroHistory: List<Float> = (state.recentGyroSamples + imuSample.angularVelocity.z).takeLast(n = 30)

    val remainingTunnelM: Double = if (inTunnelBlackout) {
      max(a = 0.0, b = state.currentScenario.tunnelDistanceMeters * (1.0 - (currentWaypointIndex.toDouble() / waypoints.size)))
    } else {
      state.currentScenario.tunnelDistanceMeters
    }

    val instruction: String = when {
      inTunnelBlackout -> "TUNNEL DEAD RECKONING ACTIVE: ${matched.roadName} (${remainingTunnelM.toInt()}m remaining)"
      matched.isTunnel -> "Approaching Tunnel Portal in 200m"
      else -> "Continue on ${matched.roadName}"
    }

    val stats: TelemetryStats = TelemetryStats(
      imuFrequencyHz = 100.0,
      gnssFrequencyHz = if (isGpsHealthy) 1.0 else 0.0,
      handoverLatencyMs = 12L,
      ukfTraceCovariance = if (inTunnelBlackout) 1.25 else 0.35,
      aiSpeedEstimateMps = targetSpeed,
      gnssSpeedMps = if (isGpsHealthy) targetSpeed else 0f,
      lateralDriftPercent = if (inTunnelBlackout) 0.65 else 0.12,
      blackoutDurationSeconds = if (inTunnelBlackout) (state.telemetryStats.blackoutDurationSeconds + 0.05) else 0.0,
      totalDistanceMeters = state.telemetryStats.totalDistanceMeters + (targetSpeed * 0.05),
      satellitesTracked = if (isGpsHealthy) 16 else 0,
      zuptActive = false,
      nhcApplied = true,
      activeVehicleType = state.activeVehicleProfile.type,
    )

    _uiState.update { prev ->
      prev.copy(
        waypointProgressIndex = currentWaypointIndex,
        navigationState = rawNavState.copy(
          geoPoint = matched.matchedPoint,
          matchedRoadName = matched.roadName,
          roadSnapped = true,
        ),
        rawGnssFix = gnssFix,
        telemetryStats = stats,
        fusedPath = updatedFusedPath,
        rawGpsPath = updatedGpsPath,
        groundTruthPath = updatedTruthPath,
        currentManeuverInstruction = instruction,
        tunnelRemainingMeters = remainingTunnelM,
        recentAccelSamples = newAccelHistory,
        recentGyroSamples = newGyroHistory,
      )
    }
  }

  override fun onCleared() {
    super.onCleared()
    simulationJob?.cancel()
  }
}
