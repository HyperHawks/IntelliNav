package com.intellinav.core.model

enum class FixQuality {
  NO_FIX,
  STANDARD_GPS,
  DGPS,
  RTK_FLOAT,
  RTK_FIXED,
  SIMULATED,
}

data class GnssSatelliteInfo(
  val svid: Int,
  val constellationType: Int,
  val snrDbHz: Float,
  val elevationDegrees: Float,
  val azimuthDegrees: Float,
  val usedInFix: Boolean,
)

data class GnssFix(
  val timestampMs: Long,
  val latitude: Double,
  val longitude: Double,
  val altitudeMeters: Double = 0.0,
  val speedMps: Float = 0f,
  val bearingDegrees: Float = 0f,
  val horizontalAccuracyMeters: Float = 10f,
  val verticalAccuracyMeters: Float = 15f,
  val satellitesUsed: Int = 0,
  val hdop: Float = 1.0f,
  val quality: FixQuality = FixQuality.STANDARD_GPS,
  val isBlackout: Boolean = false,
) {
  val isValid: Boolean
    get() {
      if (isBlackout) return false
      if (quality == FixQuality.NO_FIX) return false
      if (horizontalAccuracyMeters > 50f) return false
      return true
    }
}
