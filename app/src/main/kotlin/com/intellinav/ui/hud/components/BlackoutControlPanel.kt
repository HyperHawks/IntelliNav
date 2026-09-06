package com.intellinav.ui.hud.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.SensorsOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.intellinav.core.model.VehicleProfile
import com.intellinav.core.simulation.NavigationScenario
import com.intellinav.core.simulation.TunnelScenarios
import com.intellinav.ui.MainUiState
import com.intellinav.ui.theme.AlertAmber
import com.intellinav.ui.theme.DarkNavyCard
import com.intellinav.ui.theme.FusedCyan
import com.intellinav.ui.theme.HazardRed
import com.intellinav.ui.theme.NeonGreen
import com.intellinav.ui.theme.TextPrimaryWhite
import com.intellinav.ui.theme.TextSecondaryMuted

@Composable
fun BlackoutControlPanel(
  uiState: MainUiState,
  onToggleBlackout: () -> Unit,
  onSelectScenario: (NavigationScenario) -> Unit,
  onSelectVehicle: (VehicleProfile) -> Unit,
  onTogglePlayPause: () -> Unit,
  onReset: () -> Unit,
  onSetSpeed: (Float) -> Unit,
  modifier: Modifier = Modifier,
) {
  var showScenarioDialog: Boolean by remember { mutableStateOf(value = false) }
  var showVehicleDialog: Boolean by remember { mutableStateOf(value = false) }

  Card(
    modifier = modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = DarkNavyCard),
    shape = RoundedCornerShape(size = 14.dp),
  ) {
    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        // Blackout trigger button
        Button(
          onClick = onToggleBlackout,
          colors = ButtonDefaults.buttonColors(
            containerColor = if (uiState.isBlackoutForced) HazardRed else AlertAmber,
            contentColor = Color.Black,
          ),
          shape = RoundedCornerShape(size = 8.dp),
        ) {
          Icon(
            imageVector = if (uiState.isBlackoutForced) Icons.Default.SensorsOff else Icons.Default.Sensors,
            contentDescription = "Toggle blackout",
            modifier = Modifier.size(size = 16.dp),
          )
          Spacer(modifier = Modifier.width(width = 6.dp))
          Text(
            text = if (uiState.isBlackoutForced) "GNSS Blackout FORCED" else "Cut GNSS Signal",
            fontSize = 12.sp,
            fontWeight = Bold,
          )
        }

        // Play/Pause & Reset
        Row(verticalAlignment = CenterVertically) {
          IconButton(onClick = onTogglePlayPause) {
            Icon(
              imageVector = if (uiState.isSimulating) Icons.Default.Pause else Icons.Default.PlayArrow,
              contentDescription = "Play/Pause",
              tint = TextPrimaryWhite,
            )
          }
          IconButton(onClick = onReset) {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = "Reset",
              tint = TextSecondaryMuted,
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(height = 8.dp))

      // Scenario & Vehicle Profile chips
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = CenterVertically,
      ) {
        // Scenario selector chip
        Box(
          modifier = Modifier
            .weight(weight = 1f)
            .background(color = DarkNavyCard.copy(alpha = 0.8f), shape = RoundedCornerShape(size = 6.dp))
            .clickable { showScenarioDialog = true }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        ) {
          Column {
            Text(text = "Scenario", color = TextSecondaryMuted, fontSize = 10.sp)
            Text(
              text = uiState.currentScenario.title,
              color = FusedCyan,
              fontSize = 12.sp,
              fontWeight = SemiBold,
              maxLines = 1,
            )
          }
        }

        Spacer(modifier = Modifier.width(width = 8.dp))

        // Vehicle Profile chip
        Box(
          modifier = Modifier
            .weight(weight = 1f)
            .background(color = DarkNavyCard.copy(alpha = 0.8f), shape = RoundedCornerShape(size = 6.dp))
            .clickable { showVehicleDialog = true }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        ) {
          Column {
            Text(text = "Vehicle Profile", color = TextSecondaryMuted, fontSize = 10.sp)
            Text(
              text = uiState.activeVehicleProfile.name,
              color = NeonGreen,
              fontSize = 12.sp,
              fontWeight = SemiBold,
              maxLines = 1,
            )
          }
        }

        Spacer(modifier = Modifier.width(width = 8.dp))

        // Speed Multipliers (1x, 2x, 5x)
        Row {
          listOf(1.0f, 2.0f, 5.0f).forEach { speed ->
            val isSelected: Boolean = uiState.simulationSpeedMultiplier == speed
            Box(
              modifier = Modifier
                .padding(horizontal = 2.dp)
                .background(
                  color = if (isSelected) NeonGreen else Color.Transparent,
                  shape = CircleShape,
                )
                .clickable { onSetSpeed(speed) }
                .padding(horizontal = 6.dp, vertical = 4.dp),
              contentAlignment = Center,
            ) {
              Text(
                text = "${speed.toInt()}x",
                color = if (isSelected) Color.Black else TextSecondaryMuted,
                fontSize = 11.sp,
                fontWeight = Bold,
              )
            }
          }
        }
      }
    }
  }

  // Scenario Selection Dialog
  if (showScenarioDialog) {
    androidx.compose.material3.AlertDialog(
      onDismissRequest = { showScenarioDialog = false },
      title = { Text(text = "Select Simulation Scenario", color = TextPrimaryWhite, fontWeight = Bold) },
      text = {
        Column {
          TunnelScenarios.AllScenarios.forEach { scenario ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  onSelectScenario(scenario)
                  showScenarioDialog = false
                }
                .padding(vertical = 10.dp),
              verticalAlignment = CenterVertically,
            ) {
              Column {
                Text(text = scenario.title, color = FusedCyan, fontWeight = SemiBold)
                Text(text = scenario.description, color = TextSecondaryMuted, fontSize = 12.sp)
              }
            }
          }
        }
      },
      confirmButton = {
        Button(onClick = { showScenarioDialog = false }) {
          Text(text = "Close")
        }
      },
      containerColor = DarkNavyCard,
    )
  }

  // Vehicle Profile Dialog
  if (showVehicleDialog) {
    androidx.compose.material3.AlertDialog(
      onDismissRequest = { showVehicleDialog = false },
      title = { Text(text = "Select Vehicle Profile", color = TextPrimaryWhite, fontWeight = Bold) },
      text = {
        Column {
          VehicleProfile.DefaultProfiles.forEach { profile ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  onSelectVehicle(profile)
                  showVehicleDialog = false
                }
                .padding(vertical = 10.dp),
              verticalAlignment = CenterVertically,
            ) {
              Column {
                Text(text = profile.name, color = NeonGreen, fontWeight = SemiBold)
                Text(text = profile.description, color = TextSecondaryMuted, fontSize = 12.sp)
              }
            }
          }
        }
      },
      confirmButton = {
        Button(onClick = { showVehicleDialog = false }) {
          Text(text = "Close")
        }
      },
      containerColor = DarkNavyCard,
    )
  }
}
