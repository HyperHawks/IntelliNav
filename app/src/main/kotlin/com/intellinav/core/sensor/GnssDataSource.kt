package com.intellinav.core.sensor

import com.intellinav.core.model.FixQuality
import com.intellinav.core.model.GeoPoint
import com.intellinav.core.model.GnssFix
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class GnssDataSource {
  private val _currentFix: MutableStateFlow<GnssFix> = MutableStateFlow(
    value = GnssFix(
      timestampMs = System.currentTimeMillis(),
      latitude = 19.0760,
      longitude = 72.8777,
      speedMps = 16.6f, // ~60 km/h
      bearingDegrees = 45f,
      horizontalAccuracyMeters = 3.2f,
      satellitesUsed = 16,
      hdop = 0.9f,
      quality = FixQuality.STANDARD_GPS,
      isBlackout = false,
    ),
  )
  val currentFix: Flow<GnssFix> = _currentFix.asStateFlow()

  private var isBlackoutForced: Boolean = false

  fun setBlackout(blackout: Boolean) {
    isBlackoutForced = blackout
    val prev: GnssFix = _currentFix.value
    _currentFix.value = prev.copy(
      isBlackout = blackout,
      quality = if (blackout) FixQuality.NO_FIX else FixQuality.STANDARD_GPS,
      satellitesUsed = if (blackout) 0 else 16,
      horizontalAccuracyMeters = if (blackout) 150f else 3.5f,
    )
  }

  fun isBlackoutActive(): Boolean = isBlackoutForced

  fun emitFix(
    latitude: Double,
    longitude: Double,
    speedMps: Float,
    bearingDegrees: Float,
    accuracyMeters: Float = 3.5f,
  ) {
    if (isBlackoutForced) {
      _currentFix.value = GnssFix(
        timestampMs = System.currentTimeMillis(),
        latitude = latitude,
        longitude = longitude,
        speedMps = 0f,
        bearingDegrees = bearingDegrees,
        horizontalAccuracyMeters = 150f,
        satellitesUsed = 0,
        hdop = 9.9f,
        quality = FixQuality.NO_FIX,
        isBlackout = true,
      )
      return
    }

    _currentFix.value = GnssFix(
      timestampMs = System.currentTimeMillis(),
      latitude = latitude,
      longitude = longitude,
      speedMps = speedMps,
      bearingDegrees = bearingDegrees,
      horizontalAccuracyMeters = accuracyMeters,
      satellitesUsed = 14,
      hdop = 0.95f,
      quality = FixQuality.STANDARD_GPS,
      isBlackout = false,
    )
  }
}
