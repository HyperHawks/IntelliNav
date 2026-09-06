package com.intellinav.core.model

import kotlin.math.sqrt
import kotlin.math.max
import kotlin.math.min

data class RoadNode(
  val id: String,
  val geoPoint: GeoPoint,
)

data class RoadSegment(
  val id: String,
  val name: String,
  val startNodeId: String,
  val endNodeId: String,
  val polyline: List<GeoPoint>,
  val headingDegrees: Float,
  val lengthMeters: Double,
  val speedLimitKmh: Int = 80,
  val isTunnel: Boolean = false,
) {
  fun projectPoint(point: GeoPoint): MapMatchedResult {
    if (polyline.size < 2) {
      val defaultDist: Double = if (polyline.isNotEmpty()) polyline.first().distanceToMeters(target = point) else 0.0
      return MapMatchedResult(
        matchedPoint = polyline.firstOrNull() ?: point,
        segmentId = id,
        roadName = name,
        lateralOffsetMeters = defaultDist,
        alongSegmentDistanceMeters = 0.0,
        confidence = 0.5f,
        isTunnel = isTunnel,
      )
    }

    var bestDist: Double = Double.MAX_VALUE
    var bestPoint: GeoPoint = polyline.first()
    var cumulativeLength: Double = 0.0
    var bestAlongLength: Double = 0.0

    for (i in 0 until polyline.size - 1) {
      val p1: GeoPoint = polyline[i]
      val p2: GeoPoint = polyline[i + 1]
      val segLen: Double = p1.distanceToMeters(target = p2)
      if (segLen <= 1e-3) continue

      // Linear interpolation projection in local tangent space
      val u: Double = calculateProjectionFraction(p1 = p1, p2 = p2, target = point)
      val clampedU: Double = max(a = 0.0, b = min(a = 1.0, b = u))
      val projected: GeoPoint = GeoPoint(
        latitude = p1.latitude + clampedU * (p2.latitude - p1.latitude),
        longitude = p1.longitude + clampedU * (p2.longitude - p1.longitude),
        altitudeMeters = p1.altitudeMeters + clampedU * (p2.altitudeMeters - p1.altitudeMeters),
      )
      val dist: Double = projected.distanceToMeters(target = point)
      if (dist < bestDist) {
        bestDist = dist
        bestPoint = projected
        bestAlongLength = cumulativeLength + (clampedU * segLen)
      }
      cumulativeLength += segLen
    }

    val conf: Float = (1.0 / (1.0 + (bestDist / 8.0))).toFloat()

    return MapMatchedResult(
      matchedPoint = bestPoint,
      segmentId = id,
      roadName = name,
      lateralOffsetMeters = bestDist,
      alongSegmentDistanceMeters = bestAlongLength,
      confidence = conf,
      isTunnel = isTunnel,
    )
  }

  private fun calculateProjectionFraction(p1: GeoPoint, p2: GeoPoint, target: GeoPoint): Double {
    val dx: Double = p2.longitude - p1.longitude
    val dy: Double = p2.latitude - p1.latitude
    val lenSq: Double = (dx * dx) + (dy * dy)
    if (lenSq <= 1e-12) return 0.0
    val targetDx: Double = target.longitude - p1.longitude
    val targetDy: Double = target.latitude - p1.latitude
    return ((targetDx * dx) + (targetDy * dy)) / lenSq
  }
}

data class MapMatchedResult(
  val matchedPoint: GeoPoint,
  val segmentId: String,
  val roadName: String,
  val lateralOffsetMeters: Double,
  val alongSegmentDistanceMeters: Double,
  val confidence: Float,
  val isTunnel: Boolean,
)
