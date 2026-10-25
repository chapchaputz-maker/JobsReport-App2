package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val JobsReportColorScheme = darkColorScheme(
  primary = PrimaryBlue,
  onPrimary = Color.White,
  primaryContainer = PrimaryBlue.copy(alpha = 0.2f),
  onPrimaryContainer = LightBlue,
  secondary = EmeraldGreen,
  tertiary = VioletPurple,
  background = BgDeep,
  onBackground = TextPrimary,
  surface = BgSurface,
  onSurface = TextPrimary,
  surfaceVariant = BgCard,
  onSurfaceVariant = TextSecondary,
  outline = BorderWhite10,
  error = DangerRed
)

@Composable
fun JobsReportTheme(
  darkTheme: Boolean = true, // JobsReport is designed with a dark deep background
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = JobsReportColorScheme,
    typography = Typography,
    content = content
  )
}

