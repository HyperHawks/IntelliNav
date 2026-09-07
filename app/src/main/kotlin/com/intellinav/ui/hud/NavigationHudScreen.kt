package com.intellinav.ui.hud

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Alignment.Companion.TopCenter
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.font.FontWeight.Companion.Medium
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.intellinav.ui.MainUiState
import com.intellinav.ui.MainViewModel
import com.intellinav.ui.hud.components.BlackoutControlPanel
import com.intellinav.ui.hud.components.ManeuverCard
import com.intellinav.ui.hud.components.NavigationCanvasMap
import com.intellinav.ui.theme.AlertAmber
import com.intellinav.ui.theme.DarkNavyBackground
import com.intellinav.ui.theme.DarkNavyCard
import com.intellinav.ui.theme.FusedCyan
import com.intellinav.ui.theme.NeonGreen
import com.intellinav.ui.theme.TextPrimaryWhite
import com.intellinav.ui.theme.TextSecondaryMuted

@Composable
fun NavigationHudScreen(
  viewModel: MainViewModel,
  modifier: Modifier = Modifier,
) {
  val uiState: MainUiState by viewModel.uiState.collectAsStateWithLifecycle()

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(color = DarkNavyBackground),
  ) {
    // 1. Full-screen Interactive Navigation Canvas Map
    NavigationCanvasMap(
      uiState = uiState,
      modifier = Modifier.fillMaxSize(),
    )

    // 2. Top HUD Status Bar
    TopHudStatusBar(
      uiState = uiState,
      modifier = Modifier
        .align(alignment = TopCenter)
        .padding(horizontal = 16.dp, vertical = 12.dp),
    )

    // 3. Bottom Overlays (Maneuver guidance + Simulation Controls)
    Column(
      modifier = Modifier
        .align(alignment = BottomCenter)
        .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
      ManeuverCard(uiState = uiState)
      Spacer(modifier = Modifier.height(height = 8.dp))
      BlackoutControlPanel(
        uiState = uiState,
        onToggleBlackout = viewModel::toggleBlackout,
        onSelectScenario = viewModel::selectScenario,
        onSelectVehicle = viewModel::selectVehicleProfile,
        onTogglePlayPause = viewModel::togglePlayPause,
        onReset = viewModel::resetSimulation,
        onSetSpeed = viewModel::setSimulationSpeed,
      )
    }
  }
}

@Composable
private fun TopHudStatusBar(
  uiState: MainUiState,
  modifier: Modifier = Modifier,
) {
  val inBlackout: Boolean = uiState.rawGnssFix.isBlackout

  Card(
    modifier = modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = DarkNavyCard.copy(alpha = 0.92f)),
    shape = RoundedCornerShape(size = 12.dp),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = CenterVertically,
    ) {
      // App Branding & Hackathon badge
      Row(verticalAlignment = CenterVertically) {
        Box(
          modifier = Modifier
            .size(size = 10.dp)
            .background(color = if (inBlackout) AlertAmber else NeonGreen, shape = CircleShape),
        )
        Spacer(modifier = Modifier.width(width = 8.dp))
        Column {
          Row(verticalAlignment = CenterVertically) {
            Text(
              text = "IntelliNav",
              color = TextPrimaryWhite,
              fontSize = 15.sp,
              fontWeight = Bold,
            )
            Spacer(modifier = Modifier.width(width = 6.dp))
            Box(
              modifier = Modifier
                .background(color = FusedCyan.copy(alpha = 0.2f), shape = RoundedCornerShape(size = 4.dp))
                .padding(horizontal = 4.dp, vertical = 1.dp),
            ) {
              Text(
                text = "SIH 2026",
                color = FusedCyan,
                fontSize = 9.sp,
                fontWeight = Bold,
              )
            }
          }
          Text(
            text = uiState.activeVehicleProfile.name,
            color = TextSecondaryMuted,
            fontSize = 11.sp,
          )
        }
      }

      // GNSS / Dead Reckoning Status Indicator
      Row(verticalAlignment = CenterVertically) {
        Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
          Text(
            text = if (inBlackout) "DEAD RECKONING" else "GNSS FUSED",
            color = if (inBlackout) AlertAmber else NeonGreen,
            fontSize = 12.sp,
            fontWeight = Bold,
          )
          Text(
            text = if (inBlackout) "Tunnel • Cov: ±${String.format(locale = java.util.Locale.US, format = "%.1fm", uiState.navigationState.confidenceRadiusMeters)}" else "${uiState.rawGnssFix.satellitesUsed} SVs • 100Hz IMU",
            color = TextSecondaryMuted,
            fontSize = 10.sp,
            fontWeight = Medium,
          )
        }
      }
    }
  }
}
// GNSS Blackout Simulation
