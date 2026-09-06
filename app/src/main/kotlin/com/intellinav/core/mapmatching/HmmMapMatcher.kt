package com.intellinav.core.mapmatching

import com.intellinav.core.model.GeoPoint
import com.intellinav.core.model.MapMatchedResult
import com.intellinav.core.model.NavigationState
import com.intellinav.core.model.RoadSegment
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.PI

class HmmMapMatcher(
  private val osmGraph: OsmGraph,
  private val sigmaZ: Double = 6.0,  // GPS/dead reckoning lateral standard deviation
  private val beta: Double = 5.0,    // Transition score scaling parameter
) {
  private var lastSnappedSegment: RoadSegment? = null
  private var lastPoint: GeoPoint? = null

  fun match(state: NavigationState): MapMatchedResult {
    val candidates: List<RoadSegment> = osmGraph.findCandidateSegments(point = state.geoPoint)
    if (candidates.isEmpty()) {
      return MapMatchedResult(
        matchedPoint = state.geoPoint,
        segmentId = "unknown",
        roadName = state.matchedRoadName,
        lateralOffsetMeters = 0.0,
        alongSegmentDistanceMeters = 0.0,
        confidence = 0.5f,
        isTunnel = false,
      )
    }

    var bestLogProb: Double = -1e9
    var bestResult: MapMatchedResult? = null
    var bestSegment: RoadSegment? = null

    for (candidate in candidates) {
      val proj: MapMatchedResult = candidate.projectPoint(point = state.geoPoint)
      val dist: Double = proj.lateralOffsetMeters

      // 1. Emission probability: Gaussian on perpendicular distance
      val emissionLogProb: Double = -0.5 * (dist * dist) / (sigmaZ * sigmaZ)

      // 2. Heading alignment factor: cos(headingDiff)
      val headingDiffRad: Double = abs(n = (state.headingDegrees - candidate.headingDegrees) * PI / 180.0)
      val headingFactor: Double = max(a = 0.1, b = cos(x = headingDiffRad))
      val headingLogProb: Double = ln(x = headingFactor)

      // 3. Transition probability from last matched segment
      var transitionLogProb: Double = 0.0
      val prevSegment: RoadSegment? = lastSnappedSegment
      val prevPt: GeoPoint? = lastPoint
      if (prevSegment != null && prevPt != null) {
        val euclideanDist: Double = prevPt.distanceToMeters(target = state.geoPoint)
        val networkDist: Double = if (candidate.id == prevSegment.id) euclideanDist else euclideanDist * 1.2
        val deltaDist: Double = abs(n = networkDist - euclideanDist)
        transitionLogProb = -deltaDist / beta
      }

      val totalLogProb: Double = emissionLogProb + headingLogProb + transitionLogProb

      if (totalLogProb > bestLogProb) {
        bestLogProb = totalLogProb
        bestResult = proj
        bestSegment = candidate
      }
    }

    lastSnappedSegment = bestSegment
    lastPoint = state.geoPoint

    return bestResult ?: candidates.first().projectPoint(point = state.geoPoint)
  }

  fun reset() {
    lastSnappedSegment = null
    lastPoint = null
  }
}
