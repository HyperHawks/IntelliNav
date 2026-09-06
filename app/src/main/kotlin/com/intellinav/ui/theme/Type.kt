package com.intellinav.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.font.FontWeight.Companion.Medium
import androidx.compose.ui.text.font.FontWeight.Companion.Normal
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.ui.unit.sp

val IntelliNavTypography: Typography = Typography(
  displayLarge = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = Bold,
    fontSize = 32.sp,
    lineHeight = 38.sp,
    letterSpacing = 0.sp,
  ),
  headlineMedium = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = Bold,
    fontSize = 22.sp,
    lineHeight = 28.sp,
    letterSpacing = 0.sp,
  ),
  titleLarge = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = SemiBold,
    fontSize = 18.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.sp,
  ),
  titleMedium = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = Medium,
    fontSize = 15.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.15.sp,
  ),
  bodyLarge = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = Normal,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.25.sp,
  ),
  bodyMedium = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = Normal,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.4.sp,
  ),
  labelSmall = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = Medium,
    fontSize = 10.sp,
    lineHeight = 14.sp,
    letterSpacing = 0.5.sp,
  ),
)
