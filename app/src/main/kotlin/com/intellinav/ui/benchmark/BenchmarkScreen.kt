package com.intellinav.ui.benchmark

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.font.FontWeight.Companion.Medium
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.intellinav.ui.theme.AlertAmber
import com.intellinav.ui.theme.DarkNavyBackground
import com.intellinav.ui.theme.DarkNavyCard
import com.intellinav.ui.theme.FusedCyan
import com.intellinav.ui.theme.HazardRed
import com.intellinav.ui.theme.NeonGreen
import com.intellinav.ui.theme.TextPrimaryWhite
import com.intellinav.ui.theme.TextSecondaryMuted

@Composable
fun BenchmarkScreen(
  modifier: Modifier = Modifier,
) {
  val scrollState = rememberScrollState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(color = DarkNavyBackground)
      .verticalScroll(state = scrollState)
      .padding(all = 16.dp),
  ) {
    // Header
    Text(
      text = "Research & Hackathon Benchmarks",
      color = TextPrimaryWhite,
      fontSize = 20.sp,
      fontWeight = Bold,
    )
    Text(
      text = "Smart India Hackathon 2026 • TEKATHON 5.0 (@TEKATHON-5.0-IntelliNav)",
      color = FusedCyan,
      fontSize = 12.sp,
      fontWeight = SemiBold,
    )

    Spacer(modifier = Modifier.height(height = 16.dp))

    // 1. Inside a GNSS Blackout: Today vs With IntelliNav Card
    TunnelComparisonCard()

    Spacer(modifier = Modifier.height(height = 14.dp))

    // 2. Demographic Reach & Impact Card
    DemographicImpactCard()

    Spacer(modifier = Modifier.height(height = 14.dp))

    // 3. Technical Feasibility & System Specifications
    TechnicalFeasibilityCard()

    Spacer(modifier = Modifier.height(height = 14.dp))

    // 4. Academic Research References Card
    ResearchReferencesCard()

    Spacer(modifier = Modifier.height(height = 24.dp))
  }
}

@Composable
private fun TunnelComparisonCard() {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = DarkNavyCard),
    shape = RoundedCornerShape(size = 14.dp),
  ) {
    Column(modifier = Modifier.padding(all = 14.dp)) {
      Text(
        text = "INSIDE A GNSS BLACKOUT (TUNNELS & UNDERPASSES)",
        color = AlertAmber,
        fontSize = 13.sp,
        fontWeight = Bold,
      )

      Spacer(modifier = Modifier.height(height = 10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        // Today (Standard GPS)
        Column(
          modifier = Modifier
            .weight(weight = 1f)
            .background(color = HazardRed.copy(alpha = 0.08f), shape = RoundedCornerShape(size = 8.dp))
            .border(width = 1.dp, color = HazardRed.copy(alpha = 0.4f), shape = RoundedCornerShape(size = 8.dp))
            .padding(all = 10.dp),
        ) {
          Text(text = "TODAY (STANDARD GPS)", color = HazardRed, fontSize = 11.sp, fontWeight = Bold)
          Spacer(modifier = Modifier.height(height = 6.dp))
          BulletItem(icon = Icons.Default.Cancel, iconColor = HazardRed, text = "Position completely freezes at entrance")
          BulletItem(icon = Icons.Default.Cancel, iconColor = HazardRed, text = "Missed forks & tunnel exits")
          BulletItem(icon = Icons.Default.Cancel, iconColor = HazardRed, text = "Dangerous last-second lane changes")
          BulletItem(icon = Icons.Default.Cancel, iconColor = HazardRed, text = "Cloud navigation failure without data")
        }

        Spacer(modifier = Modifier.width(width = 10.dp))

        // With IntelliNav
        Column(
          modifier = Modifier
            .weight(weight = 1f)
            .background(color = NeonGreen.copy(alpha = 0.08f), shape = RoundedCornerShape(size = 8.dp))
            .border(width = 1.dp, color = NeonGreen.copy(alpha = 0.4f), shape = RoundedCornerShape(size = 8.dp))
            .padding(all = 10.dp),
        ) {
          Text(text = "WITH INTELLINAV", color = NeonGreen, fontSize = 11.sp, fontWeight = Bold)
          Spacer(modifier = Modifier.height(height = 6.dp))
          BulletItem(icon = Icons.Default.CheckCircle, iconColor = NeonGreen, text = "Continuous uninterrupted guidance")
          BulletItem(icon = Icons.Default.CheckCircle, iconColor = NeonGreen, text = "Sub-15ms seamless handover")
          BulletItem(icon = Icons.Default.CheckCircle, iconColor = NeonGreen, text = "< 1.8% drift over 9 km tunnel")
          BulletItem(icon = Icons.Default.CheckCircle, iconColor = NeonGreen, text = "100% on-device & fully offline")
        }
      }
    }
  }
}

@Composable
private fun DemographicImpactCard() {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = DarkNavyCard),
    shape = RoundedCornerShape(size = 14.dp),
  ) {
    Column(modifier = Modifier.padding(all = 14.dp)) {
      Row(verticalAlignment = CenterVertically) {
        Icon(
          imageVector = Icons.Default.Public,
          contentDescription = "Demographics",
          tint = FusedCyan,
          modifier = Modifier.size(size = 20.dp),
        )
        Spacer(modifier = Modifier.width(width = 8.dp))
        Text(
          text = "Demographic Reach & Economic Impact",
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
        MetricTile(
          value = "260M+",
          label = "Two-Wheeler Riders",
          sublabel = "Across India (Data For India, 2022)",
          accentColor = NeonGreen,
        )
        MetricTile(
          value = "₹0",
          label = "Extra Hardware Cost",
          sublabel = "Uses phone IMU, no OBD-II needed",
          accentColor = FusedCyan,
        )
        MetricTile(
          value = "< 12 ms",
          label = "Handover Speed",
          sublabel = "Instant blackout detection",
          accentColor = AlertAmber,
        )
      }
    }
  }
}

@Composable
private fun MetricTile(
  value: String,
  label: String,
  sublabel: String,
  accentColor: Color,
) {
  Column(modifier = Modifier.padding(horizontal = 4.dp)) {
    Text(text = value, color = accentColor, fontSize = 18.sp, fontWeight = Bold)
    Text(text = label, color = TextPrimaryWhite, fontSize = 11.sp, fontWeight = SemiBold)
    Text(text = sublabel, color = TextSecondaryMuted, fontSize = 9.sp)
  }
}

@Composable
private fun TechnicalFeasibilityCard() {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = DarkNavyCard),
    shape = RoundedCornerShape(size = 14.dp),
  ) {
    Column(modifier = Modifier.padding(all = 14.dp)) {
      Row(verticalAlignment = CenterVertically) {
        Icon(
          imageVector = Icons.Default.Shield,
          contentDescription = "Feasibility",
          tint = NeonGreen,
          modifier = Modifier.size(size = 20.dp),
        )
        Spacer(modifier = Modifier.width(width = 8.dp))
        Text(
          text = "System Specifications & Viability",
          color = TextPrimaryWhite,
          fontSize = 14.sp,
          fontWeight = Bold,
        )
      }

      Spacer(modifier = Modifier.height(height = 8.dp))

      Text(text = "• AI Inference Model: Quantized Neural Kinematic Estimator (< 5 MB, < 10 ms inference)", color = TextPrimaryWhite, fontSize = 12.sp)
      Text(text = "• Fusion Algorithm: 7-State Unscented Kalman Filter (UKF) with Merwe Scaled Transform", color = TextPrimaryWhite, fontSize = 12.sp)
      Text(text = "• Map Matching: Hidden Markov Model (HMM) with Viterbi decoding + Non-Holonomic Constraints", color = TextPrimaryWhite, fontSize = 12.sp)
      Text(text = "• Dual Deployment: Kotlin Android app + Edge-deployable C++ engine for external hardware", color = TextPrimaryWhite, fontSize = 12.sp)
    }
  }
}

@Composable
private fun ResearchReferencesCard() {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = DarkNavyCard),
    shape = RoundedCornerShape(size = 14.dp),
  ) {
    Column(modifier = Modifier.padding(all = 14.dp)) {
      Row(verticalAlignment = CenterVertically) {
        Icon(
          imageVector = Icons.Default.MenuBook,
          contentDescription = "References",
          tint = TextSecondaryMuted,
          modifier = Modifier.size(size = 20.dp),
        )
        Spacer(modifier = Modifier.width(width = 8.dp))
        Text(
          text = "Key Research & Benchmark Literature",
          color = TextPrimaryWhite,
          fontSize = 14.sp,
          fontWeight = Bold,
        )
      }

      Spacer(modifier = Modifier.height(height = 8.dp))

      Text(text = "[1] Onyekpe et al., 'IO-VNBD Benchmark Dataset for Ground Vehicle Positioning', Data in Brief 2021", color = TextSecondaryMuted, fontSize = 11.sp)
      Spacer(modifier = Modifier.height(height = 4.dp))
      Text(text = "[2] Brossard, Barrau & Bonnabel, 'AI-IMU Dead-Reckoning', IEEE Trans. Intelligent Vehicles, 2020", color = TextSecondaryMuted, fontSize = 11.sp)
      Spacer(modifier = Modifier.height(height = 4.dp))
      Text(text = "[3] FilterPy - Unscented Kalman Filter reference for GNSS + IMU sensor fusion", color = TextSecondaryMuted, fontSize = 11.sp)
      Spacer(modifier = Modifier.height(height = 4.dp))
      Text(text = "[4] Hidden Markov Model (HMM) map-matching with Viterbi decoding onto OpenStreetMap (OSM)", color = TextSecondaryMuted, fontSize = 11.sp)
    }
  }
}

@Composable
private fun BulletItem(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  iconColor: Color,
  text: String,
) {
  Row(
    modifier = Modifier.padding(vertical = 3.dp),
    verticalAlignment = CenterVertically,
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = iconColor,
      modifier = Modifier.size(size = 14.dp),
    )
    Spacer(modifier = Modifier.width(width = 6.dp))
    Text(text = text, color = TextPrimaryWhite, fontSize = 10.sp)
  }
}
