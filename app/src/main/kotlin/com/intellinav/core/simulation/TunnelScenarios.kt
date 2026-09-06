package com.intellinav.core.simulation

import com.intellinav.core.mapmatching.OsmGraph
import com.intellinav.core.model.GeoPoint
import com.intellinav.core.model.RoadNode
import com.intellinav.core.model.RoadSegment
import com.intellinav.core.model.VehicleProfile

data class ScenarioWaypoint(
  val geoPoint: GeoPoint,
  val speedMps: Float,
  val headingDegrees: Float,
  val isTunnelBlackout: Boolean,
  val roadName: String,
)

data class NavigationScenario(
  val id: String,
  val title: String,
  val location: String,
  val description: String,
  val totalDistanceMeters: Double,
  val tunnelDistanceMeters: Double,
  val recommendedVehicle: VehicleProfile,
  val waypoints: List<ScenarioWaypoint>,
  val osmGraph: OsmGraph,
)

object TunnelScenarios {

  val AtalTunnel: NavigationScenario by lazy {
    // Atal Tunnel (Rohtang) - 9.02 km long, 3,100m elevation
    val startLat: Double = 32.3638
    val startLon: Double = 77.1408
    val graph: OsmGraph = OsmGraph()

    val approachNodes: List<GeoPoint> = listOf(
      GeoPoint(latitude = startLat, longitude = startLon, altitudeMeters = 3050.0),
      GeoPoint(latitude = startLat + 0.005, longitude = startLon + 0.002, altitudeMeters = 3060.0),
    )

    // 9.02 km Tunnel Corridor (10 waypoints through tunnel)
    val tunnelNodes: MutableList<GeoPoint> = mutableListOf()
    for (i in 0..10) {
      val frac: Double = i / 10.0
      val lat: Double = startLat + 0.005 + (frac * 0.081) // ~9 km North
      val lon: Double = startLon + 0.002 + (frac * 0.006)
      tunnelNodes.add(
        element = GeoPoint(
          latitude = lat,
          longitude = lon,
          altitudeMeters = 3100.0,
        ),
      )
    }

    val exitNodes: List<GeoPoint> = listOf(
      tunnelNodes.last(),
      GeoPoint(latitude = tunnelNodes.last().latitude + 0.006, longitude = tunnelNodes.last().longitude + 0.002, altitudeMeters = 3080.0),
    )

    // Build segments
    val seg1: RoadSegment = RoadSegment(
      id = "atal_approach",
      name = "Manali-Leh Highway (Approach)",
      startNodeId = "n1",
      endNodeId = "n2",
      polyline = approachNodes,
      headingDegrees = 18f,
      lengthMeters = 600.0,
      isTunnel = false,
    )

    val seg2: RoadSegment = RoadSegment(
      id = "atal_tunnel_main",
      name = "Atal Tunnel Corridor (9.02 km)",
      startNodeId = "n2",
      endNodeId = "n3",
      polyline = tunnelNodes,
      headingDegrees = 4f,
      lengthMeters = 9020.0,
      isTunnel = true,
    )

    val seg3: RoadSegment = RoadSegment(
      id = "atal_exit",
      name = "North Portal Highway",
      startNodeId = "n3",
      endNodeId = "n4",
      polyline = exitNodes,
      headingDegrees = 12f,
      lengthMeters = 800.0,
      isTunnel = false,
    )

    graph.addSegment(segment = seg1)
    graph.addSegment(segment = seg2)
    graph.addSegment(segment = seg3)

    // Generate waypoints along the route
    val waypoints: MutableList<ScenarioWaypoint> = mutableListOf()
    waypoints.add(
      element = ScenarioWaypoint(
        geoPoint = approachNodes[0],
        speedMps = 16.6f,
        headingDegrees = 18f,
        isTunnelBlackout = false,
        roadName = "Manali-Leh Highway",
      ),
    )
    for (tn in tunnelNodes) {
      waypoints.add(
        element = ScenarioWaypoint(
          geoPoint = tn,
          speedMps = 22.2f, // 80 km/h in tunnel
          headingDegrees = 4f,
          isTunnelBlackout = true,
          roadName = "Atal Tunnel (9.02 km GNSS Blackout)",
        ),
      )
    }
    waypoints.add(
      element = ScenarioWaypoint(
        geoPoint = exitNodes[1],
        speedMps = 16.6f,
        headingDegrees = 12f,
        isTunnelBlackout = false,
        roadName = "North Portal Highway",
      ),
    )

    NavigationScenario(
      id = "atal_tunnel",
      title = "Atal Tunnel (9.02 km GNSS Blackout)",
      location = "Himachal Pradesh, India (Elevation 3,100m)",
      description = "World's longest highway tunnel above 10,000 ft. Complete GNSS blackout over 9 km, testing continuous AI dead-reckoning.",
      totalDistanceMeters = 10420.0,
      tunnelDistanceMeters = 9020.0,
      recommendedVehicle = VehicleProfile.TruckFreight,
      waypoints = waypoints,
      osmGraph = graph,
    )
  }

  val MumbaiCoastalRoad: NavigationScenario by lazy {
    // Mumbai Coastal Road Subsea Underpass (2.07 km)
    val startLat: Double = 18.9553
    val startLon: Double = 72.8028
    val graph: OsmGraph = OsmGraph()

    val approach: List<GeoPoint> = listOf(
      GeoPoint(latitude = startLat, longitude = startLon),
      GeoPoint(latitude = startLat + 0.003, longitude = startLon + 0.001),
    )

    val underpassPoints: MutableList<GeoPoint> = mutableListOf()
    for (i in 0..8) {
      val frac: Double = i / 8.0
      // Curve under Malabar Hill
      val lat: Double = startLat + 0.003 + (frac * 0.018)
      val lon: Double = startLon + 0.001 + (kotlin.math.sin(x = frac * kotlin.math.PI) * 0.003)
      underpassPoints.add(
        element = GeoPoint(
          latitude = lat,
          longitude = lon,
          altitudeMeters = -20.0,
        ),
      )
    }

    val exitPoints: List<GeoPoint> = listOf(
      underpassPoints.last(),
      GeoPoint(latitude = underpassPoints.last().latitude + 0.004, longitude = underpassPoints.last().longitude + 0.001),
    )

    val s1: RoadSegment = RoadSegment(
      id = "mumbai_approach",
      name = "Marine Drive Coastal Promenade",
      startNodeId = "m1",
      endNodeId = "m2",
      polyline = approach,
      headingDegrees = 15f,
      lengthMeters = 400.0,
      isTunnel = false,
    )

    val s2: RoadSegment = RoadSegment(
      id = "mumbai_underpass",
      name = "Malabar Hill Undersea Tunnel (2.07 km)",
      startNodeId = "m2",
      endNodeId = "m3",
      polyline = underpassPoints,
      headingDegrees = 10f,
      lengthMeters = 2070.0,
      isTunnel = true,
    )

    val s3: RoadSegment = RoadSegment(
      id = "mumbai_exit",
      name = "Worli Sea Face Connector",
      startNodeId = "m3",
      endNodeId = "m4",
      polyline = exitPoints,
      headingDegrees = 20f,
      lengthMeters = 500.0,
      isTunnel = false,
    )

    graph.addSegment(segment = s1)
    graph.addSegment(segment = s2)
    graph.addSegment(segment = s3)

    val waypoints: MutableList<ScenarioWaypoint> = mutableListOf()
    waypoints.add(
      element = ScenarioWaypoint(
        geoPoint = approach[0],
        speedMps = 19.4f,
        headingDegrees = 15f,
        isTunnelBlackout = false,
        roadName = "Marine Drive Coastal Promenade",
      ),
    )
    for (up in underpassPoints) {
      waypoints.add(
        element = ScenarioWaypoint(
          geoPoint = up,
          speedMps = 22.2f,
          headingDegrees = 10f,
          isTunnelBlackout = true,
          roadName = "Malabar Undersea Tunnel (Blackout)",
        ),
      )
    }
    waypoints.add(
      element = ScenarioWaypoint(
        geoPoint = exitPoints[1],
        speedMps = 16.6f,
        headingDegrees = 20f,
        isTunnelBlackout = false,
        roadName = "Worli Sea Face Connector",
      ),
    )

    NavigationScenario(
      id = "mumbai_coastal",
      title = "Coastal Road Underpass (2.07 km)",
      location = "Mumbai, Maharashtra, India",
      description = "Twin undersea tunnel under Malabar Hill with high underground curves, demonstrating lane-accurate dead reckoning without GNSS.",
      totalDistanceMeters = 2970.0,
      tunnelDistanceMeters = 2070.0,
      recommendedVehicle = VehicleProfile.TwoWheeler,
      waypoints = waypoints,
      osmGraph = graph,
    )
  }

  val AllScenarios: List<NavigationScenario> by lazy {
    listOf(
      AtalTunnel,
      MumbaiCoastalRoad,
    )
  }
}
