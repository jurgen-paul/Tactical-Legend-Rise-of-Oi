package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.data.model.Rarity

// Professional Polish Dystopian Warm Tactical Palette
val CyberBackground = Color(0xFFFDF8F6)      // Warm Porcelain Canvas
val CyberSurface = Color(0xFFFFFFFF)         // Clean White Card Surface
val CyberSurfaceVariant = Color(0xFFF7EBE8)  // Warm Soft Rose-Clay Surface
val CyberPrimary = Color(0xFF8F4C38)         // Rich Terracotta Clay Primary
val CyberSecondary = Color(0xFFC84B31)       // Vivid Crimson Ember Accent
val CyberTertiary = Color(0xFFD97706)        // Amber Gold Tactical Accent
val CyberGreen = Color(0xFF16A34A)          // Tactical Emerald Green
val CyberPurple = Color(0xFF7C3AED)         // Intelligence Violet
val CyberBorder = Color(0xFFF0E0DC)          // Soft Warm Border
val CyberYellow = Color(0xFFD97706)          // Warm Cyber Gold Yellow
val CyberOnSurface = Color(0xFF201A19)       // Deep Charcoal Primary Text
val CyberSubtext = Color(0xFF53433F)         // Warm Medium Charcoal Subtext
val CyberOverlay = Color(0x99201A19)         // Translucent Backdrop Overlay
val RarityCommon = Color(0xFF78716C)
val RarityRare = Color(0xFF0284C7)
val RarityEpic = Color(0xFF7C3AED)
val RarityLegendary = Color(0xFFD97706)

val Rarity.color: Color
    get() = when (this) {
        Rarity.COMMON -> RarityCommon
        Rarity.RARE -> RarityRare
        Rarity.EPIC -> RarityEpic
        Rarity.LEGENDARY -> RarityLegendary
    }

