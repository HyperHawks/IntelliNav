package com.intellinav.ui.telemetry

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.SatelliteAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap.Companion.Round
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.font.FontWeight.Companion.Medium
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.intellinav.ui.MainUiState
import com.intellinav.ui.MainViewModel
import com.intellinav.ui.theme.AlertAmber
import com.intellinav.ui.theme.DarkNavyBackground
import com.intellinav.ui.theme.DarkNavyCard
import com.intellinav.ui.theme.FusedCyan
import com.intellinav.ui.theme.HazardRed
import com.intellinav.ui.theme.NeonGreen
import com.intellinav.ui.theme.TextPrimaryWhite
import com.intellinav.ui.theme.TextSecondaryMuted
import java.util.Locale

@Composable
fun TelemetryDashboardScreen(
  viewModel: MainViewModel,
  modifier: Modifier = Modifier,
) {
  val uiState: MainUiState by viewModel.uiState.collectAsStateWithLifecycle()
  val scrollState = rememberScrollState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(color = DarkNavyBackground)
      .verticalScroll(state = scrollState)
      .padding(all = 16.dp),
  ) {
    Text(
      text = "Telemetry & AI Estimator Diagnostics",
      color = TextPrimaryWhite,
      fontSize = 20.sp,
      fontWeight = Bold,
    )
    Text(
      text = "Real-time 100 Hz sensor sync & UKF non-linear state estimation",
      color = TextSecondaryMuted,
      fontSize = 12.sp,
    )

    Spacer(modifier = Modifier.height(height = 16.dp))

    // 1. Live 100 Hz IMU Waveforms Card
    ImuWaveformCard(uiState = uiState)

    Spacer(modifier = Modifier.height(height = 14.dp))

    // 2. AI Kinematic Estimator vs GNSS Speed comparison
    AiEstimatorComparisonCard(uiState = uiState)

    Spacer(modifier = Modifier.height(height = 14.dp))

    // 3. 7-State UKF Sensor Fusion Diagnostics
    UkfDiagnosticsCard(uiState = uiState)

    Spacer(modifier = Modifier.height(height = 14.dp))

    // 4. GNSS Constellation & Blackout Handover Stats
    GnssBlackoutStatsCard(uiState = uiState)

    Spacer(modifier = Modifier.height(height = 24.dp))
  }
}

@Composable
private fun ImuWaveformCard(uiState: MainUiState) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = DarkNavyCard),
    shape = RoundedCornerShape(size = 14.dp),
  ) {
    Column(modifier = Modifier.padding(all = 14.dp)) {
      Row(verticalAlignment = CenterVertically) {
        Icon(
          imageVector = Icons.Default.ElectricMeter,
          contentDescription = "Waveforms",
          tint = NeonGreen,
          modifier = Modifier.size(size = 20.dp),
        )
        Spacer(modifier = Modifier.width(width = 8.dp))
        Text(
          text = "100 Hz Real-Time IMU Signal Stream",
          color = TextPrimaryWhite,
          fontSize = 14.sp,
          fontWeight = Bold,
        )
      }

      Spacer(modifier = Modifier.height(height = 8.dp))

      // Waveform Canvas
      Canvas(
        modifier = Modifier
          .fillMaxWidth()
          .height(height = 90.dp)
          .background(color = DarkNavyBackground, shape = RoundedCornerShape(size = 8.dp)),
      ) {
        val w: Float = size.width
        val h: Float = size.height
        val midY: Float = h / 2f

        // Center line
        drawLine(
          color = TextSecondaryMuted.copy(alpha = 0.2f),
          start = Offset(x = 0f, y = midY),
          end = Offset(x = w, y = midY),
          strokeWidth = 1f,
        )

        // Draw Accel waveform (Neon Green)
        val accelSamples: List<Float> = uiState.recentAccelSamples
        if (accelSamples.size > 1) {
          val stepX: Float = w / (accelSamples.size - 1)
          val accelPath: Path = Path()
          accelPath.moveTo(x = 0f, y = midY - (accelSamples.first() * 15f))
          for (i in 1 until accelSamples.size) {
            val px: Float = i * stepX
            val py: Float = (midY - (accelSamples[i] * 15f)).coerceIn(minimumValue = 5f, maximumValue = h - 5f)
            accelPath.lineTo(x = px, y = py)
          }
          drawPath(path = accelPath, color = NeonGreen, style = Stroke(width = 2.5f, cap = Round))
        }

        // Draw Gyro waveform (Fused Cyan)
        val gyroSamples: List<Float> = uiState.recentGyroSamples
        if (gyroSamples.size > 1) {
          val stepX: Float = w / (gyroSamples.size - 1)
          val gyroPath: Path = Path()
          gyroPath.moveTo(x = 0f, y = midY - (gyroSamples.first() * 120f))
          for (i in 1 until gyroSamples.size) {
            val px: Float = i * stepX
            val py: Float = (midY - (gyroSamples[i] * 120f)).coerceIn(minimumValue = 5f, maximumValue = h - 5f)
            gyroPath.lineTo(x = px, y = py)
          }
          drawPath(path = gyroPath, color = FusedCyan, style = Stroke(width = 2f, cap = Round))
        }
      }

      Spacer(modifier = Modifier.height(height = 6.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(verticalAlignment = CenterVertically) {
          Box(modifier = Modifier.size(size = 8.dp).background(color = NeonGreen, shape = CircleShape))
          Spacer(modifier = Modifier.width(width = 4.dp))
          Text(text = "Forward Accel (m/s²)", color = TextSecondaryMuted, fontSize = 10.sp)
        }
        Row(verticalAlignment = CenterVertically) {
          Box(modifier = Modifier.size(size = 8.dp).background(color = FusedCyan, shape = CircleShape))
          Spacer(modifier = Modifier.width(width = 4.dp))
          Text(text = "Yaw Rate (rad/s)", color = TextSecondaryMuted, fontSize = 10.sp)
        }
      }
    }
  }
}

@Composable
private fun AiEstimatorComparisonCard(uiState: MainUiState) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = DarkNavyCard),
    shape = RoundedCornerShape(size = 14.dp),
  ) {
    Column(modifier = Modifier.padding(all = 14.dp)) {
      Row(verticalAlignment = CenterVertically) {
        Icon(
          imageVector = Icons.Default.Speed,
          contentDescription = "AI Estimator",
          tint = FusedCyan,
          modifier = Modifier.size(size = 20.dp),
        )
        Spacer(modifier = Modifier.width(width = 8.dp))
        Text(
          text = "AI Kinematic Estimator (Without OBD-II)",
          color = TextPrimaryWhite,
          fontSize = 14.sp,
          fontWeight = Bold,
        )
      }

      Spacer(modifier = Modifier.height(height = 10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        // AI Inferred Speed
        Column(modifier = Modifier.weight(weight = 1f)) {
          Text(text = "AI Inferred Speed", color = TextSecondaryMuted, fontSize = 11.sp)
          Text(
            text = String.format(
              locale = Locale.US,
              format = "%.1f km/h",
              uiState.telemetryStats.aiSpeedEstimateMps * 3.6f,
            ),
            color = FusedCyan,
            fontSize = 18.sp,
            fontWeight = Bold,
          )
          Text(text = "Neural IMU window", color = TextSecondaryMuted, fontSize = 10.sp)
        }

        // GNSS Speed
        Column(modifier = Modifier.weight(weight = 1f)) {
          Text(text = "GNSS Speed", color = TextSecondaryMuted, fontSize = 11.sp)
          Text(
            text = if (uiState.rawGnssFix.isBlackout) "0.0 km/h (LOST)" else String.format(
              locale = Locale.US,
              format = "%.1f km/h",
              uiState.telemetryStats.gnssSpeedMps * 3.6f,
            ),
            color = if (uiState.rawGnssFix.isBlackout) HazardRed else NeonGreen,
            fontSize = 18.sp,
            fontWeight = Bold,
          )
          Text(
            text = if (uiState.rawGnssFix.isBlackout) "Signal denied" else "Doppler / Fix",
            color = TextSecondaryMuted,
            fontSize = 10.sp,
          )
        }
      }
    }
  }
}

@Composable
private fun UkfDiagnosticsCard(uiState: MainUiState) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = DarkNavyCard),
    shape = RoundedCornerShape(size = 14.dp),
  ) {
    Column(modifier = Modifier.padding(all = 14.dp)) {
      Row(verticalAlignment = CenterVertically) {
        Icon(
          imageVector = Icons.Default.Memory,
          contentDescription = "UKF Diagnostics",
          tint = AlertAmber,
          modifier = Modifier.size(size = 20.dp),
        )
        Spacer(modifier = Modifier.width(width = 8.dp))
        Text(
          text = "7-State UKF & Constraint Engines",
          color = TextPrimaryWhite,
          fontSize = 14.sp,
          fontWeight = Bold,
        )
      }

      Spacer(modifier = Modifier.height(height = 10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Column {
          Text(text = "Uncertainty Radius (1-σ)", color = TextSecondaryMuted, fontSize = 11.sp)
          Text(
            text = String.format(
              locale = Locale.US,
              format = "±%.2f m",
              uiState.navigationState.confidenceRadiusMeters,
            ),
            color = TextPrimaryWhite,
            fontSize = 15.sp,
            fontWeight = SemiBold,
          )
        }

        Column {
          Text(text = "Non-Holonomic (NHC)", color = TextSecondaryMuted, fontSize = 11.sp)
          Text(
            text = "Active (v_lat ≈ 0)",
            color = NeonGreen,
            fontSize = 15.sp,
            fontWeight = SemiBold,
          )
        }

        Column {
          Text(text = "ZUPT Detector", color = TextSecondaryMuted, fontSize = 11.sp)
          Text(
            text = if (uiState.telemetryStats.zuptActive) "STATIONARY" else "MOVING",
            color = if (uiState.telemetryStats.zuptActive) AlertAmber else FusedCyan,
            fontSize = 15.sp,
            fontWeight = SemiBold,
          )
        }
      }
    }
  }
}

@Composable
private fun GnssBlackoutStatsCard(uiState: MainUiState) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = DarkNavyCard),
    shape = RoundedCornerShape(size = 14.dp),
  ) {
    Column(modifier = Modifier.padding(all = 14.dp)) {
      Row(verticalAlignment = CenterVertically) {
        Icon(
          imageVector = Icons.Default.SatelliteAlt,
          contentDescription = "GNSS Stats",
          tint = if (uiState.rawGnssFix.isBlackout) HazardRed else NeonGreen,
          modifier = Modifier.size(size = 20.dp),
        )
        Spacer(modifier = Modifier.width(width = 8.dp))
        Text(
          text = "GNSS Handover & Blackout Metrics",
          color = TextPrimaryWhite,
          fontSize = 14.sp,
          fontWeight = Bold,
        )
      }

      Spacer(modifier = Modifier.height(height = 10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Column {
          Text(text = "Handover Latency", color = TextSecondaryMuted, fontSize = 11.sp)
          Text(text = "${uiState.telemetryStats.handoverLatencyMs} ms", color = NeonGreen, fontSize = 15.sp, fontWeight = Bold)
        }
        Column {
          Text(text = "Tunnel Blackout Time", color = TextSecondaryMuted, fontSize = 11.sp)
          Text(
            text = String.format(
              locale = Locale.US,
              format = "%.1f s",
              uiState.telemetryStats.blackoutDurationSeconds,
            ),
            color = if (uiState.rawGnssFix.isBlackout) AlertAmber else TextSecondaryMuted,
            fontSize = 15.sp,
            fontWeight = Bold,
          )
        }
        Column {
          Text(text = "Lateral Drift %", color = TextSecondaryMuted, fontSize = 11.sp)
          Text(
            text = String.format(
              locale = Locale.US,
              format = "%.2f%%",
              uiState.telemetryStats.lateralDriftPercent,
            ),
            color = FusedCyan,
            fontSize = 15.sp,
            fontWeight = Bold,
          )
        }
      }
    }
  }
}
