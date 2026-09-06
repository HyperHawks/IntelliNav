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
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TableChart
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
import com.intellinav.core.benchmark.BenchmarkEvaluation
import com.intellinav.core.benchmark.IoVnbdDatasetBenchmark
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

    // 2. IO-VNBD Dataset Evaluation Table (Slide 4 & 6)
    IoVnbdEvaluationTableCard()

    Spacer(modifier = Modifier.height(height = 14.dp))

    // 3. Demographic Reach & Economic Impact Card (Slide 5)
    DemographicImpactCard()

    Spacer(modifier = Modifier.height(height = 14.dp))

    // 4. SWOT Analysis Card (Slide 4)
    SwotAnalysisCard()

    Spacer(modifier = Modifier.height(height = 14.dp))

    // 5. Technical Feasibility & System Specifications
    TechnicalFeasibilityCard()

    Spacer(modifier = Modifier.height(height = 14.dp))

    // 6. Academic Research References Card (Slide 6)
    ResearchReferencesCard()

    Spacer(modifier = Modifier.height(height = 24.dp))
  }
}

@Composable
private fun IoVnbdEvaluationTableCard() {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = DarkNavyCard),
    shape = RoundedCornerShape(size = 14.dp),
  ) {
    Column(modifier = Modifier.padding(all = 14.dp)) {
      Row(verticalAlignment = CenterVertically) {
        Icon(
          imageVector = Icons.Default.TableChart,
          contentDescription = "IO-VNBD Table",
          tint = NeonGreen,
          modifier = Modifier.size(size = 20.dp),
        )
        Spacer(modifier = Modifier.width(width = 8.dp))
        Text(
          text = "IO-VNBD Benchmark Dataset Validation",
          color = TextPrimaryWhite,
          fontSize = 14.sp,
          fontWeight = Bold,
        )
      }
      Text(
        text = "Ref: Onyekpe et al. (Data in Brief 2021) & Brossard et al. (IEEE T-IV 2020)",
        color = TextSecondaryMuted,
        fontSize = 10.sp,
      )

      Spacer(modifier = Modifier.height(height = 10.dp))

      BenchmarkRowItem(evaluation = IoVnbdDatasetBenchmark.AtalTunnelEvaluation)
      Spacer(modifier = Modifier.height(height = 8.dp))
      BenchmarkRowItem(evaluation = IoVnbdDatasetBenchmark.MumbaiUnderpassEvaluation)
    }
  }
}

@Composable
private fun BenchmarkRowItem(evaluation: BenchmarkEvaluation) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .background(color = DarkNavyBackground, shape = RoundedCornerShape(size = 8.dp))
      .padding(all = 10.dp),
  ) {
    Text(text = evaluation.datasetName, color = FusedCyan, fontSize = 12.sp, fontWeight = SemiBold)
    Spacer(modifier = Modifier.height(height = 4.dp))
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Column {
        Text(text = "Track Length", color = TextSecondaryMuted, fontSize = 10.sp)
        Text(text = "${evaluation.testTrackLengthMeters.toInt()} m", color = TextPrimaryWhite, fontSize = 12.sp, fontWeight = Bold)
      }
      Column {
        Text(text = "Standard GPS", color = HazardRed, fontSize = 10.sp)
        Text(text = "FROZEN", color = HazardRed, fontSize = 12.sp, fontWeight = Bold)
      }
      Column {
        Text(text = "IntelliNav RMSE", color = NeonGreen, fontSize = 10.sp)
        Text(text = "${String.format(locale = Locale.US, format = "%.1f", evaluation.intelliNavRmseMeters)} m", color = NeonGreen, fontSize = 12.sp, fontWeight = Bold)
      }
      Column {
        Text(text = "Drift %", color = FusedCyan, fontSize = 10.sp)
        Text(text = "${String.format(locale = Locale.US, format = "%.2f", evaluation.driftPercentageOfDistance)}%", color = FusedCyan, fontSize = 12.sp, fontWeight = Bold)
      }
      Column {
        Text(text = "Inference", color = TextSecondaryMuted, fontSize = 10.sp)
        Text(text = "${evaluation.inferenceLatencyMs} ms", color = TextPrimaryWhite, fontSize = 12.sp, fontWeight = Bold)
      }
    }
  }
}

@Composable
private fun SwotAnalysisCard() {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = DarkNavyCard),
    shape = RoundedCornerShape(size = 14.dp),
  ) {
    Column(modifier = Modifier.padding(all = 14.dp)) {
      Text(
        text = "SWOT ANALYSIS (TEKATHON 5.0)",
        color = TextPrimaryWhite,
        fontSize = 13.sp,
        fontWeight = Bold,
      )

      Spacer(modifier = Modifier.height(height = 10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        // Strengths
        Column(
          modifier = Modifier
            .weight(weight = 1f)
            .background(color = NeonGreen.copy(alpha = 0.08f), shape = RoundedCornerShape(size = 8.dp))
            .padding(all = 8.dp),
        ) {
          Text(text = "STRENGTHS", color = NeonGreen, fontSize = 11.sp, fontWeight = Bold)
          Text(text = "• Zero extra hardware", color = TextPrimaryWhite, fontSize = 10.sp)
          Text(text = "• 100% offline & on-device", color = TextPrimaryWhite, fontSize = 10.sp)
          Text(text = "• Dual app & edge deployment", color = TextPrimaryWhite, fontSize = 10.sp)
        }

        Spacer(modifier = Modifier.width(width = 8.dp))

        // Weaknesses
        Column(
          modifier = Modifier
            .weight(weight = 1f)
            .background(color = AlertAmber.copy(alpha = 0.08f), shape = RoundedCornerShape(size = 8.dp))
            .padding(all = 8.dp),
        ) {
          Text(text = "WEAKNESSES", color = AlertAmber, fontSize = 11.sp, fontWeight = Bold)
          Text(text = "• Long tunnel (>1km) drift", color = TextPrimaryWhite, fontSize = 10.sp)
          Text(text = "• Per-device phone bias", color = TextPrimaryWhite, fontSize = 10.sp)
          Text(text = "• Depends on training data", color = TextPrimaryWhite, fontSize = 10.sp)
        }
      }

      Spacer(modifier = Modifier.height(height = 8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        // Opportunities
        Column(
          modifier = Modifier
            .weight(weight = 1f)
            .background(color = FusedCyan.copy(alpha = 0.08f), shape = RoundedCornerShape(size = 8.dp))
            .padding(all = 8.dp),
        ) {
          Text(text = "OPPORTUNITIES", color = FusedCyan, fontSize = 11.sp, fontWeight = Bold)
          Text(text = "• Fleet & delivery partnerships", color = TextPrimaryWhite, fontSize = 10.sp)
          Text(text = "• OEM factory integration", color = TextPrimaryWhite, fontSize = 10.sp)
          Text(text = "• NDRF disaster response", color = TextPrimaryWhite, fontSize = 10.sp)
        }

        Spacer(modifier = Modifier.width(width = 8.dp))

        // Threats
        Column(
          modifier = Modifier
            .weight(weight = 1f)
            .background(color = HazardRed.copy(alpha = 0.08f), shape = RoundedCornerShape(size = 8.dp))
            .padding(all = 8.dp),
        ) {
          Text(text = "THREATS", color = HazardRed, fontSize = 11.sp, fontWeight = Bold)
          Text(text = "• Sensor API permission changes", color = TextPrimaryWhite, fontSize = 10.sp)
          Text(text = "• OEM-built INS competition", color = TextPrimaryWhite, fontSize = 10.sp)
          Text(text = "• OSM map quality differences", color = TextPrimaryWhite, fontSize = 10.sp)
        }
      }
    }
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
