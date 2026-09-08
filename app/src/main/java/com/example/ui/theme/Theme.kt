package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = Color(0xFF3860FF),
    onPrimary = Color.White,
    primaryContainer = NavyLight,
    onPrimaryContainer = Color.White,
    secondary = OrangeAccent,
    onSecondary = Color.White,
    background = Color(0xFF090E1F),
    surface = Color(0xFF101730),
    onBackground = Color.White,
    onSurface = Color.White,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = NavyPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8EEFF),
    onPrimaryContainer = NavyDark,
    secondary = OrangeAccent,
    onSecondary = Color.White,
    secondaryContainer = OrangeLight,
    onSecondaryContainer = Color(0xFF6B2F00),
    background = BackgroundLight,
    surface = SurfaceWhite,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    outline = BorderStroke,
    surfaceVariant = Color(0xFFF1F4F9),
    onSurfaceVariant = TextSecondaryMuted
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false, // Keep clean high-trust light branding by default
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

