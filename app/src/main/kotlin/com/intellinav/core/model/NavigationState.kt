package com.intellinav.core.model

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.PI

enum class NavigationMode {
  GNSS_FUSED,
  DEAD_RECKONING_TUNNEL,
  DEGRADED_URBAN_CANYON,
  STATIONARY_ZUPT,
}

data class GeoPoint(
  val latitude: Double,
  val longitude: Double,
  val altitudeMeters: Double = 0.0,
) {
  fun distanceToMeters(target: GeoPoint): Double {
    val earthRadiusMeters: Double = 6371000.0
    val lat1Rad: Double = latitude * PI / 180.0
    val lat2Rad: Double = target.latitude * PI / 180.0
    val deltaLatRad: Double = (target.latitude - latitude) * PI / 180.0
    val deltaLonRad: Double = (target.longitude - longitude) * PI / 180.0

    val sinDeltaLat: Double = sin(x = deltaLatRad / 2.0)
    val sinDeltaLon: Double = sin(x = deltaLonRad / 2.0)
    val a: Double = (sinDeltaLat * sinDeltaLat) + (cos(x = lat1Rad) * cos(x = lat2Rad) * sinDeltaLon * sinDeltaLon)
    val c: Double = 2.0 * kotlin.math.atan2(y = sqrt(x = a), x = sqrt(x = 1.0 - a))
    return earthRadiusMeters * c
  }

  fun offsetMeters(eastMeters: Double, northMeters: Double): GeoPoint {
    val earthRadiusMeters: Double = 6378137.0
    val deltaLat: Double = (northMeters / earthRadiusMeters) * (180.0 / PI)
    val latRad: Double = latitude * PI / 180.0
    val deltaLon: Double = (eastMeters / (earthRadiusMeters * cos(x = latRad))) * (180.0 / PI)
    return GeoPoint(
      latitude = latitude + deltaLat,
      longitude = longitude + deltaLon,
      altitudeMeters = altitudeMeters,
    )
  }
}

data class Position2D(
  val eastMeters: Double = 0.0,
  val northMeters: Double = 0.0,
)

data class Velocity2D(
  val vx: Double = 0.0,
  val vy: Double = 0.0,
) {
  val speedMps: Double
    get() = sqrt(x = (vx * vx) + (vy * vy))

  val speedKmh: Double
    get() = speedMps * 3.6
}

data class NavigationState(
  val timestampNs: Long = 0L,
  val geoPoint: GeoPoint = GeoPoint(latitude = 19.0760, longitude = 72.8777),
  val localPosition: Position2D = Position2D(),
  val velocity: Velocity2D = Velocity2D(),
  val headingDegrees: Float = 0f,
  val pitchDegrees: Float = 0f,
  val rollDegrees: Float = 0f,
  val mode: NavigationMode = NavigationMode.GNSS_FUSED,
  val confidenceRadiusMeters: Float = 2.5f,
  val timeInBlackoutMs: Long = 0L,
  val roadSnapped: Boolean = false,
  val matchedRoadName: String = "Highway NH-48",
  val forwardAccelerationMps2: Float = 0f,
)
