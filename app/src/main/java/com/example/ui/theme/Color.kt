package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.data.model.Rarity

// Call of Duty: Black Ops III Cyberpunk Tactical Palette
val CyberBackground = Color(0xFF090C10)      // Deep Black Ops Obsidian Canvas
val CyberSurface = Color(0xFF111722)         // Dark Metal Card Surface
val CyberSurfaceVariant = Color(0xFF1B2433)  // High-Tech Carbon Fiber Metal
val CyberPrimary = Color(0xFFFF5500)         // Iconic BO3 Ember Orange / Gold Primary
val CyberSecondary = Color(0xFF00E5FF)       // DNI Direct Neural Interface Electric Cyan
val CyberTertiary = Color(0xFFFFB703)        // Black Market Cryptokey Amber Gold
val CyberGreen = Color(0xFF05FF69)          // Tactical Ready Matrix Emerald
val CyberRed = Color(0xFFFF334B)            // BO3 Tactical Red / Danger
val CyberPurple = Color(0xFFA855F7)         // Zombies Element 115 Violet
val CyberBorder = Color(0xFF283548)          // Glowing Cyber Steel Border
val CyberYellow = Color(0xFFFFB703)          // BO3 Tactical Hazard Gold
val CyberOnSurface = Color(0xFFF1F5F9)       // Crisp High-Contrast HUD Text
val CyberSubtext = Color(0xFF94A3B8)         // Tactical Telemetry Subtext Gray
val CyberOverlay = Color(0xCC090C10)         // Translucent HUD Backdrop Overlay

// Rarity Color System
val RarityCommon = Color(0xFF94A3B8)
val RarityRare = Color(0xFF00E5FF)
val RarityEpic = Color(0xFFA855F7)
val RarityLegendary = Color(0xFFFF5500)

val Rarity.color: Color
    get() = when (this) {
        Rarity.COMMON -> RarityCommon
        Rarity.RARE -> RarityRare
        Rarity.EPIC -> RarityEpic
        Rarity.LEGENDARY -> RarityLegendary
    }


