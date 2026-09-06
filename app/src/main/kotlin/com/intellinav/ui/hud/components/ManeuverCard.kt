package com.intellinav.ui.hud.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.font.FontWeight.Companion.Medium
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.intellinav.ui.MainUiState
import com.intellinav.ui.theme.AlertAmber
import com.intellinav.ui.theme.DarkNavyCard
import com.intellinav.ui.theme.FusedCyan
import com.intellinav.ui.theme.HazardRed
import com.intellinav.ui.theme.NeonGreen
import com.intellinav.ui.theme.TextPrimaryWhite
import com.intellinav.ui.theme.TextSecondaryMuted

@Composable
fun ManeuverCard(
  uiState: MainUiState,
  modifier: Modifier = Modifier,
) {
  val isTunnel: Boolean = uiState.rawGnssFix.isBlackout
  val speedKmh: Int = (uiState.navigationState.velocity.speedKmh).toInt()

  Card(
    modifier = modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = DarkNavyCard),
    shape = RoundedCornerShape(size = 16.dp),
  ) {
    Column(modifier = Modifier.padding(all = 16.dp)) {
      // Tunnel / Blackout alert banner
      if (isTunnel) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(color = AlertAmber.copy(alpha = 0.15f), shape = RoundedCornerShape(size = 8.dp))
            .border(width = 1.dp, color = AlertAmber, shape = RoundedCornerShape(size = 8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
          verticalAlignment = CenterVertically,
        ) {
          Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Tunnel alert",
            tint = AlertAmber,
            modifier = Modifier.size(size = 18.dp),
          )
          Spacer(modifier = Modifier.width(width = 8.dp))
          Text(
            text = "GNSS BLACKOUT - AI INERTIAL TRACKING (12ms HANDOVER)",
            color = AlertAmber,
            fontSize = 11.sp,
            fontWeight = Bold,
          )
        }
        Spacer(modifier = Modifier.height(height = 10.dp))
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        // Maneuver icon + text
        Row(
          modifier = Modifier.weight(weight = 1f),
          verticalAlignment = CenterVertically,
        ) {
          Box(
            modifier = Modifier
              .size(size = 48.dp)
              .background(color = if (isTunnel) FusedCyan.copy(alpha = 0.2f) else NeonGreen.copy(alpha = 0.2f), shape = RoundedCornerShape(size = 12.dp)),
            contentAlignment = Center,
          ) {
            Icon(
              imageVector = Icons.Default.Navigation,
              contentDescription = "Maneuver",
              tint = if (isTunnel) FusedCyan else NeonGreen,
              modifier = Modifier.size(size = 28.dp),
            )
          }

          Spacer(modifier = Modifier.width(width = 12.dp))

          Column {
            Text(
              text = uiState.currentManeuverInstruction,
              color = TextPrimaryWhite,
              fontSize = 14.sp,
              fontWeight = SemiBold,
              maxLines = 2,
            )
            Text(
              text = "Next: ${uiState.navigationState.matchedRoadName}",
              color = TextSecondaryMuted,
              fontSize = 12.sp,
            )
          }
        }

        Spacer(modifier = Modifier.width(width = 12.dp))

        // Speedometer Display
        Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
          Row(verticalAlignment = androidx.compose.ui.Alignment.Bottom) {
            Text(
              text = "$speedKmh",
              color = TextPrimaryWhite,
              fontSize = 28.sp,
              fontWeight = Bold,
            )
            Spacer(modifier = Modifier.width(width = 4.dp))
            Text(
              text = "km/h",
              color = TextSecondaryMuted,
              fontSize = 12.sp,
              fontWeight = Medium,
              modifier = Modifier.padding(bottom = 4.dp),
            )
          }

          // Speed limit badge
          Box(
            modifier = Modifier
              .size(size = 26.dp)
              .background(color = Color.White, shape = CircleShape)
              .border(width = 2.5.dp, color = HazardRed, shape = CircleShape),
            contentAlignment = Center,
          ) {
            Text(
              text = "80",
              color = Color.Black,
              fontSize = 11.sp,
              fontWeight = Bold,
            )
          }
        }
      }
    }
  }
}
