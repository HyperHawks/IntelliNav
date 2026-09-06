package com.intellinav.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme: ColorScheme = darkColorScheme(
  primary = NeonGreen,
  secondary = FusedCyan,
  tertiary = AlertAmber,
  background = DarkNavyBackground,
  surface = DarkNavySurface,
  surfaceVariant = DarkNavyCard,
  onPrimary = DarkNavyBackground,
  onSecondary = DarkNavyBackground,
  onBackground = TextPrimaryWhite,
  onSurface = TextPrimaryWhite,
  onSurfaceVariant = TextSecondaryMuted,
  error = HazardRed,
)

@Composable
fun IntelliNavTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = DarkColorScheme,
    typography = IntelliNavTypography,
    content = content,
  )
}
