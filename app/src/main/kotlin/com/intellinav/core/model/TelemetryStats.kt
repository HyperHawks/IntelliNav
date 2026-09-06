package com.intellinav.core.model

data class TelemetryStats(
  val imuFrequencyHz: Double = 100.0,
  val gnssFrequencyHz: Double = 1.0,
  val handoverLatencyMs: Long = 12L,
  val ukfTraceCovariance: Double = 0.45,
  val aiSpeedEstimateMps: Float = 0f,
  val gnssSpeedMps: Float = 0f,
  val lateralDriftPercent: Double = 0.85,
  val blackoutDurationSeconds: Double = 0.0,
  val totalDistanceMeters: Double = 0.0,
  val satellitesTracked: Int = 14,
  val zuptActive: Boolean = false,
  val nhcApplied: Boolean = true,
  val activeVehicleType: VehicleType = VehicleType.TWO_WHEELER,
)
