package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = MetallicGold,
    onPrimary = DarkCharcoal,
    primaryContainer = RoyalNavyDark,
    onPrimaryContainer = MetallicGold,
    secondary = MetallicGold,
    onSecondary = DarkCharcoal,
    tertiary = CrimsonRedLight,
    onTertiary = Color.White,
    background = Color(0xFF0A1926),
    surface = Color(0xFF0F2236),
    onBackground = Color(0xFFF1F5F9),
    onSurface = Color(0xFFF1F5F9),
  )

private val LightColorScheme =
  lightColorScheme(
    primary = RoyalNavyBlue,
    onPrimary = PureWhite,
    primaryContainer = Color(0xFFE2EDF8),
    onPrimaryContainer = RoyalNavyBlue,
    secondary = MetallicGold,
    onSecondary = DarkCharcoal,
    secondaryContainer = GoldContainer,
    onSecondaryContainer = DarkCharcoal,
    tertiary = CrimsonRed,
    onTertiary = PureWhite,
    tertiaryContainer = CrimsonContainer,
    onTertiaryContainer = CrimsonRed,
    background = SlateLight,
    surface = PureWhite,
    onBackground = DarkCharcoal,
    onSurface = DarkCharcoal,
    surfaceVariant = SurfaceContainer,
    onSurfaceVariant = DarkCharcoal,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Preserve prestigious SJC branding
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

