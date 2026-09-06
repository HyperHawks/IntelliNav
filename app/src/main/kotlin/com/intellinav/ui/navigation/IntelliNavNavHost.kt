package com.intellinav.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import com.intellinav.ui.MainViewModel
import com.intellinav.ui.benchmark.BenchmarkScreen
import com.intellinav.ui.hud.NavigationHudScreen
import com.intellinav.ui.telemetry.TelemetryDashboardScreen
import com.intellinav.ui.theme.DarkNavyBackground
import com.intellinav.ui.theme.DarkNavyCard
import com.intellinav.ui.theme.FusedCyan
import com.intellinav.ui.theme.NeonGreen
import com.intellinav.ui.theme.TextPrimaryWhite
import com.intellinav.ui.theme.TextSecondaryMuted

enum class NavigationTab(
  val title: String,
  val icon: ImageVector,
) {
  HUD(title = "Navigation HUD", icon = Icons.Default.Navigation),
  TELEMETRY(title = "AI Telemetry", icon = Icons.Default.ElectricMeter),
  BENCHMARKS(title = "Research", icon = Icons.Default.Analytics),
}

@Composable
fun IntelliNavAppRoot(
  viewModel: MainViewModel,
  modifier: Modifier = Modifier,
) {
  var currentTab: NavigationTab by rememberSaveable { mutableStateOf(value = NavigationTab.HUD) }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    containerColor = DarkNavyBackground,
    bottomBar = {
      NavigationBar(
        containerColor = DarkNavyCard,
        contentColor = TextPrimaryWhite,
      ) {
        NavigationTab.values().forEach { tab ->
          val isSelected: Boolean = currentTab == tab
          NavigationBarItem(
            selected = isSelected,
            onClick = { currentTab = tab },
            icon = {
              Icon(
                imageVector = tab.icon,
                contentDescription = tab.title,
                tint = if (isSelected) NeonGreen else TextSecondaryMuted,
              )
            },
            label = {
              Text(
                text = tab.title,
                color = if (isSelected) NeonGreen else TextSecondaryMuted,
                fontSize = 11.sp,
              )
            },
            colors = NavigationBarItemDefaults.colors(
              indicatorColor = FusedCyan.copy(alpha = 0.15f),
            ),
          )
        }
      }
    },
  ) { innerPadding ->
    when (currentTab) {
      NavigationTab.HUD -> NavigationHudScreen(
        viewModel = viewModel,
        modifier = Modifier.padding(paddingValues = innerPadding),
      )
      NavigationTab.TELEMETRY -> TelemetryDashboardScreen(
        viewModel = viewModel,
        modifier = Modifier.padding(paddingValues = innerPadding),
      )
      NavigationTab.BENCHMARKS -> BenchmarkScreen(
        modifier = Modifier.padding(paddingValues = innerPadding),
      )
    }
  }
}
