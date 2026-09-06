package com.intellinav.core.mapmatching

import com.intellinav.core.model.GeoPoint
import com.intellinav.core.model.MapMatchedResult
import com.intellinav.core.model.NavigationMode
import com.intellinav.core.model.NavigationState
import com.intellinav.core.model.RoadSegment
import com.intellinav.core.simulation.NavigationScenario
import com.intellinav.core.simulation.TunnelScenarios
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MapMatcherTest {

  @Test
  fun testRoadSegmentProjection() {
    val p1: GeoPoint = GeoPoint(latitude = 19.0000, longitude = 72.8000)
    val p2: GeoPoint = GeoPoint(latitude = 19.0100, longitude = 72.8000) // Straight North

    val segment: RoadSegment = RoadSegment(
      id = "seg_test",
      name = "Test North Highway",
      startNodeId = "n1",
      endNodeId = "n2",
      polyline = listOf(p1, p2),
      headingDegrees = 0f,
      lengthMeters = 1113.0,
      isTunnel = false,
    )

    // Test point offset slightly to the East
    val queryPoint: GeoPoint = GeoPoint(latitude = 19.0050, longitude = 72.8002)
    val result: MapMatchedResult = segment.projectPoint(point = queryPoint)

    // Matched point longitude should be snapped onto the segment line (72.8000)
    assertEquals(72.8000, result.matchedPoint.longitude, 0.0002)
    assertTrue("Lateral offset should be ~20 meters", result.lateralOffsetMeters in 15.0..30.0)
    assertTrue(result.confidence > 0.3f)
  }

  @Test
  fun testHmmMapMatcherSnapsStateToTunnelRoad() {
    val scenario: NavigationScenario = TunnelScenarios.AtalTunnel
    val matcher: HmmMapMatcher = HmmMapMatcher(osmGraph = scenario.osmGraph)

    // Create a navigation state slightly off the tunnel centerline
    val tunnelWaypoints = scenario.waypoints.filter { it.isTunnelBlackout }
    assertTrue(tunnelWaypoints.isNotEmpty())

    val centerPt: GeoPoint = tunnelWaypoints[2].geoPoint
    val noisyPt: GeoPoint = GeoPoint(
      latitude = centerPt.latitude,
      longitude = centerPt.longitude + 0.0001, // ~10m offset
    )

    val navState: NavigationState = NavigationState(
      geoPoint = noisyPt,
      headingDegrees = 4f,
      mode = NavigationMode.DEAD_RECKONING_TUNNEL,
    )

    val matched: MapMatchedResult = matcher.match(state = navState)

    assertEquals("atal_tunnel_main", matched.segmentId)
    assertTrue(matched.isTunnel)
    assertTrue(matched.confidence > 0.4f)
  }

  @Test
  fun testScenarioLoading() {
    val atal: NavigationScenario = TunnelScenarios.AtalTunnel
    assertEquals("atal_tunnel", atal.id)
    assertTrue(atal.totalDistanceMeters > 9000.0)
    assertTrue(atal.waypoints.size > 5)

    val mumbai: NavigationScenario = TunnelScenarios.MumbaiCoastalRoad
    assertEquals("mumbai_coastal", mumbai.id)
    assertTrue(mumbai.tunnelDistanceMeters > 2000.0)
  }
}
