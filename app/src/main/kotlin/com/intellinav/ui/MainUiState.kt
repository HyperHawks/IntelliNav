package com.intellinav.ui

import com.intellinav.core.model.GeoPoint
import com.intellinav.core.model.GnssFix
import com.intellinav.core.model.NavigationState
import com.intellinav.core.model.TelemetryStats
import com.intellinav.core.model.VehicleProfile
import com.intellinav.core.simulation.NavigationScenario
import com.intellinav.core.simulation.TunnelScenarios

data class MainUiState(
  val currentScenario: NavigationScenario = TunnelScenarios.AtalTunnel,
  val activeVehicleProfile: VehicleProfile = VehicleProfile.TwoWheeler,
  val navigationState: NavigationState = NavigationState(),
  val rawGnssFix: GnssFix = GnssFix(
    timestampMs = System.currentTimeMillis(),
    latitude = 32.3638,
    longitude = 77.1408,
  ),
  val telemetryStats: TelemetryStats = TelemetryStats(),
  val isBlackoutForced: Boolean = false,
  val isSimulating: Boolean = true,
  val simulationSpeedMultiplier: Float = 1.0f,
  val waypointProgressIndex: Int = 0,
  val fusedPath: List<GeoPoint> = emptyList(),
  val rawGpsPath: List<GeoPoint> = emptyList(),
  val groundTruthPath: List<GeoPoint> = emptyList(),
  val currentManeuverInstruction: String = "Proceed along Manali-Leh Highway",
  val distanceToNextManeuverMeters: Double = 600.0,
  val tunnelRemainingMeters: Double = 9020.0,
  val recentAccelSamples: List<Float> = listOf(0.1f, -0.2f, 0.4f, 0.1f, -0.1f, 0.3f),
  val recentGyroSamples: List<Float> = listOf(0.01f, -0.02f, 0.03f, -0.01f, 0.02f),
)
