package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.data.model.Rarity

// Dystopian Cyberpunk Palette (Electric Neon + Obsidian Deep Space Canvas)
val CyberBackground = Color(0xFF0A0D14)      // Deep Obsidian Dystopian Canvas
val CyberSurface = Color(0xFF121824)         // Dark Cyber Steel Card Surface
val CyberSurfaceVariant = Color(0xFF1E2638)  // High-Tech Metal Surface Accent
val CyberPrimary = Color(0xFF00E5FF)         // Electric Cyber Cyan Primary
val CyberSecondary = Color(0xFFFF2A6D)       // Neon Crimson Pulse Secondary Accent
val CyberTertiary = Color(0xFFFFB703)        // High-Voltage Amber Gold Accent
val CyberGreen = Color(0xFF05FF69)          // Matrix Emerald Green
val CyberPurple = Color(0xFFA855F7)         // Quantum Violet Intelligence
val CyberBorder = Color(0xFF2A364F)          // Glowing Cyber Steel Border
val CyberYellow = Color(0xFFFFB703)          // Amber Hazard Gold
val CyberOnSurface = Color(0xFFF1F5F9)       // Pure Crisp High-Contrast Text
val CyberSubtext = Color(0xFF94A3B8)         // Dystopian Cyber Subtext Gray
val CyberOverlay = Color(0xCC0A0D14)         // Translucent HUD Backdrop Overlay

// Rarity Color System
val RarityCommon = Color(0xFF94A3B8)
val RarityRare = Color(0xFF00E5FF)
val RarityEpic = Color(0xFFA855F7)
val RarityLegendary = Color(0xFFFFB703)

val Rarity.color: Color
    get() = when (this) {
        Rarity.COMMON -> RarityCommon
        Rarity.RARE -> RarityRare
        Rarity.EPIC -> RarityEpic
        Rarity.LEGENDARY -> RarityLegendary
    }


