package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TacticalDarkColorScheme = darkColorScheme(
    primary = CyberPrimary,
    onPrimary = Color.Black,
    primaryContainer = CyberSurfaceVariant,
    onPrimaryContainer = CyberPrimary,
    secondary = CyberSecondary,
    onSecondary = Color.White,
    secondaryContainer = CyberSecondary.copy(alpha = 0.2f),
    onSecondaryContainer = CyberSecondary,
    tertiary = CyberTertiary,
    onTertiary = Color.Black,
    tertiaryContainer = CyberTertiary.copy(alpha = 0.2f),
    onTertiaryContainer = CyberTertiary,
    background = CyberBackground,
    onBackground = CyberOnSurface,
    surface = CyberSurface,
    onSurface = CyberOnSurface,
    surfaceVariant = CyberSurfaceVariant,
    onSurfaceVariant = CyberSubtext,
    outline = CyberBorder,
    outlineVariant = CyberBorder.copy(alpha = 0.5f),
    scrim = CyberOverlay
)

@Composable
fun TacticalLegendTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TacticalDarkColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun BlackOpsTheme(
    content: @Composable () -> Unit
) {
    TacticalLegendTheme(content = content)
}

