package com.intellinav.ui.hud.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.BottomStart
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect.Companion.dashPathEffect
import androidx.compose.ui.graphics.StrokeCap.Companion.Round
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.intellinav.core.model.GeoPoint
import com.intellinav.core.model.RoadSegment
import com.intellinav.ui.MainUiState
import com.intellinav.ui.theme.AlertAmber
import com.intellinav.ui.theme.DarkNavyBackground
import com.intellinav.ui.theme.DarkNavyCard
import com.intellinav.ui.theme.FusedCyan
import com.intellinav.ui.theme.HazardRed
import com.intellinav.ui.theme.NeonGreen
import com.intellinav.ui.theme.RoadAsphalt
import com.intellinav.ui.theme.TextPrimaryWhite
import com.intellinav.ui.theme.TextSecondaryMuted
import com.intellinav.ui.theme.TunnelCorridorColor
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.PI

@Composable
fun NavigationCanvasMap(
  uiState: MainUiState,
  modifier: Modifier = Modifier,
) {
  Box(modifier = modifier.fillMaxSize().background(color = DarkNavyBackground)) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val canvasWidth: Float = size.width
      val canvasHeight: Float = size.height

      // Compute bounding box around current scenario to fit neatly on screen
      val scenarioWaypoints: List<GeoPoint> = uiState.currentScenario.waypoints.map { it.geoPoint }
      if (scenarioWaypoints.isEmpty()) return@Canvas

      val minLat: Double = scenarioWaypoints.minOf { it.latitude }
      val maxLat: Double = scenarioWaypoints.maxOf { it.latitude }
      val minLon: Double = scenarioWaypoints.minOf { it.longitude }
      val maxLon: Double = scenarioWaypoints.maxOf { it.longitude }

      val latSpan: Double = max(a = 1e-4, b = maxLat - minLat)
      val lonSpan: Double = max(a = 1e-4, b = maxLon - minLon)

      val paddingPx: Float = 60f
      val usableWidth: Float = canvasWidth - (2 * paddingPx)
      val usableHeight: Float = canvasHeight - (2 * paddingPx)

      fun geoToScreen(geo: GeoPoint): Offset {
        val normX: Float = ((geo.longitude - minLon) / lonSpan).toFloat()
        // Invert Y because screen coordinates increase downward
        val normY: Float = (1.0f - ((geo.latitude - minLat) / latSpan).toFloat())
        return Offset(
          x = paddingPx + (normX * usableWidth),
          y = paddingPx + (normY * usableHeight),
        )
      }

      // 1. Draw Offline OSM Road Network
      val roadSegments: List<RoadSegment> = uiState.currentScenario.osmGraph.getSegments()
      for (segment in roadSegments) {
        if (segment.polyline.size < 2) continue
        val roadPath: Path = Path()
        val startOffset: Offset = geoToScreen(geo = segment.polyline.first())
        roadPath.moveTo(x = startOffset.x, y = startOffset.y)
        for (i in 1 until segment.polyline.size) {
          val nextOffset: Offset = geoToScreen(geo = segment.polyline[i])
          roadPath.lineTo(x = nextOffset.x, y = nextOffset.y)
        }

        if (segment.isTunnel) {
          // Draw Tunnel Corridor (wider base with dashed amber border)
          drawPath(
            path = roadPath,
            color = TunnelCorridorColor,
            style = Stroke(width = 24f, cap = Round),
          )
          drawPath(
            path = roadPath,
            color = AlertAmber,
            style = Stroke(
              width = 4f,
              pathEffect = dashPathEffect(intervals = floatArrayOf(20f, 10f), phase = 0f),
              cap = Round,
            ),
          )
        } else {
          // Standard Surface Highway
          drawPath(
            path = roadPath,
            color = RoadAsphalt,
            style = Stroke(width = 18f, cap = Round),
          )
          drawPath(
            path = roadPath,
            color = TextSecondaryMuted.copy(alpha = 0.3f),
            style = Stroke(width = 2f, cap = Round),
          )
        }
      }

      // 2. Draw Unfused Raw GPS Trajectory Trail (Hazard Red)
      if (uiState.rawGpsPath.size > 1) {
        val gpsPath: Path = Path()
        val firstGps: Offset = geoToScreen(geo = uiState.rawGpsPath.first())
        gpsPath.moveTo(x = firstGps.x, y = firstGps.y)
        for (i in 1 until uiState.rawGpsPath.size) {
          val pt: Offset = geoToScreen(geo = uiState.rawGpsPath[i])
          gpsPath.lineTo(x = pt.x, y = pt.y)
        }
        drawPath(
          path = gpsPath,
          color = HazardRed.copy(alpha = 0.8f),
          style = Stroke(
            width = 3f,
            pathEffect = dashPathEffect(intervals = floatArrayOf(12f, 8f), phase = 0f),
          ),
        )
      }

      // 3. Draw IntelliNav Fused / Dead Reckoning Trajectory Trail (Fused Cyan)
      if (uiState.fusedPath.size > 1) {
        val fusedPath: Path = Path()
        val firstFused: Offset = geoToScreen(geo = uiState.fusedPath.first())
        fusedPath.moveTo(x = firstFused.x, y = firstFused.y)
        for (i in 1 until uiState.fusedPath.size) {
          val pt: Offset = geoToScreen(geo = uiState.fusedPath[i])
          fusedPath.lineTo(x = pt.x, y = pt.y)
        }
        drawPath(
          path = fusedPath,
          color = FusedCyan,
          style = Stroke(width = 5f, cap = Round),
        )
      }

      // 4. Draw Current Vehicle Cursor & Heading Arrow
      val currentVehicleScreen: Offset = geoToScreen(geo = uiState.navigationState.geoPoint)
      val headingRad: Float = (uiState.navigationState.headingDegrees * PI / 180f).toFloat()

      // Pulse circle
      drawCircle(
        color = NeonGreen.copy(alpha = 0.25f),
        radius = 26f,
        center = currentVehicleScreen,
      )
      drawCircle(
        color = NeonGreen,
        radius = 12f,
        center = currentVehicleScreen,
      )

      // Heading arrow pointer
      val arrowLength: Float = 24f
      val arrowTip: Offset = Offset(
        x = currentVehicleScreen.x + (arrowLength * sin(x = headingRad)),
        y = currentVehicleScreen.y - (arrowLength * cos(x = headingRad)),
      )
      drawLine(
        color = TextPrimaryWhite,
        start = currentVehicleScreen,
        end = arrowTip,
        strokeWidth = 5f,
        cap = Round,
      )

      // 5. Draw Frozen GPS Ghost Marker if in tunnel
      if (uiState.rawGnssFix.isBlackout) {
        val frozenGpsScreen: Offset = geoToScreen(geo = uiState.rawGpsPath.lastOrNull() ?: uiState.navigationState.geoPoint)
        drawCircle(
          color = HazardRed.copy(alpha = 0.3f),
          radius = 18f,
          center = frozenGpsScreen,
        )
        drawCircle(
          color = HazardRed,
          radius = 8f,
          center = frozenGpsScreen,
        )
      }
    }

    // Floating Map Legend
    Card(
      modifier = Modifier
        .align(alignment = BottomStart)
        .padding(all = 16.dp),
      colors = CardDefaults.cardColors(containerColor = DarkNavyCard.copy(alpha = 0.9f)),
      shape = RoundedCornerShape(size = 8.dp),
    ) {
      androidx.compose.foundation.layout.Column(modifier = Modifier.padding(all = 10.dp)) {
        androidx.compose.foundation.layout.Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .androidx.compose.foundation.layout.size(size = 10.dp)
              .background(color = FusedCyan, shape = RoundedCornerShape(size = 2.dp)),
          )
          androidx.compose.foundation.layout.Spacer(modifier = Modifier.androidx.compose.foundation.layout.width(width = 8.dp))
          Text(text = "IntelliNav AI Dead Reckoning (Continuous)", color = TextPrimaryWhite, fontSize = 11.sp)
        }
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.androidx.compose.foundation.layout.height(height = 6.dp))
        androidx.compose.foundation.layout.Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .androidx.compose.foundation.layout.size(size = 10.dp)
              .background(color = HazardRed, shape = RoundedCornerShape(size = 2.dp)),
          )
          androidx.compose.foundation.layout.Spacer(modifier = Modifier.androidx.compose.foundation.layout.width(width = 8.dp))
          Text(text = "Standard GPS (Frozen in Tunnel)", color = HazardRed, fontSize = 11.sp)
        }
      }
    }
  }
}
// Added final map refinements
