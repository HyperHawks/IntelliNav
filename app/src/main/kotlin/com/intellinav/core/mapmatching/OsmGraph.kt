package com.intellinav.core.mapmatching

import com.intellinav.core.model.GeoPoint
import com.intellinav.core.model.RoadNode
import com.intellinav.core.model.RoadSegment

class OsmGraph {
  private val nodes: MutableMap<String, RoadNode> = mutableMapOf()
  private val segments: MutableList<RoadSegment> = mutableListOf()

  fun addNode(node: RoadNode) {
    nodes[node.id] = node
  }

  fun addSegment(segment: RoadSegment) {
    segments.add(element = segment)
  }

  fun getSegments(): List<RoadSegment> = segments

  fun findCandidateSegments(point: GeoPoint, maxRadiusMeters: Double = 60.0): List<RoadSegment> {
    val candidates: MutableList<RoadSegment> = mutableListOf()
    for (seg in segments) {
      val result = seg.projectPoint(point = point)
      if (result.lateralOffsetMeters <= maxRadiusMeters) {
        candidates.add(element = seg)
      }
    }
    if (candidates.isEmpty()) return segments.take(n = 3)
    return candidates
  }

  fun clear() {
    nodes.clear()
    segments.clear()
  }
}
