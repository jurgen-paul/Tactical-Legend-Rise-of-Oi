package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun CodexScreen() {
    var selectedTab by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .padding(16.dp)
            .testTag("codex_screen")
    ) {
        Text(
            text = "OISTARS OPS 1 INTEL CODEX",
            color = CyberPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
        Text(
            text = "Archive of DNI Specialist lore, tactical grid mechanics, CDP hostiles, and Corvus AI.",
            color = CyberSubtext,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = CyberSurface,
            contentColor = CyberPrimary
        ) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                Text("SPECIALISTS & DNI", modifier = Modifier.padding(12.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                Text("CYBER TACTICS", modifier = Modifier.padding(12.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                Text("CDP & UNDEAD", modifier = Modifier.padding(12.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            when (selectedTab) {
                0 -> {
                    item {
                        CodexSectionCard(
                            title = "DIRECT NEURAL INTERFACE (DNI)",
                            subtitle = "Neural Augmentation Paradigm",
                            content = "In 2065, elite Black Ops soldiers are equipped with Direct Neural Interface (DNI) implants, connecting their minds directly to military communications, cyber warfare weapons, and tactical telemetry. Specialists can hack drones, overdrive physical capabilities, and bend time on the battlefield."
                        )
                    }
                    item {
                        CodexSectionCard(
                            title = "SPECIALIST OPERATIVES",
                            subtitle = "Cyber Core Classes",
                            content = "• RUIN (Vanguard): Overdrive movement speed and unleash Gravity Spikes shockwaves.\n• OUTRIDER (Sniper): Sparrow Explosive Compound Bow & Vision Pulse scanner.\n• PROPHET (Cipher): Tempest Arc Cannon and Glitch time recall.\n• BATTERY (Medic): Kinetic Armor weave and War Machine grenade launcher.\n• SERAPH (Samurai): High-caliber Annihilator revolver and Combat Focus."
                        )
                    }
                }
                1 -> {
                    item {
                        CodexSectionCard(
                            title = "COVER & DNI ELEVATION",
                            subtitle = "Grid Combat Mechanics",
                            content = "• COVER TILES: Reduces incoming ranged damage by 30%.\n• ELEVATED PLATFORMS: Grants +20% attack bonus when firing down at enemies.\n• DNI TERMINALS: Secure Cyber Terminals at key coordinates to win operations.\n• GOBBLEGUM BUFFS: Activate GobbleGums in the Arcade for permanent campaign multipliers."
                        )
                    }
                    item {
                        CodexSectionCard(
                            title = "ACTION POINT (AP) ECONOMY",
                            subtitle = "Turn Strategy",
                            content = "Each Specialist begins their turn with 4-5 AP. Moving costs 1 AP per grid cell, basic attacks cost 2 AP, and Specialist Cyber Core abilities cost 2-3 AP."
                        )
                    }
                }
                2 -> {
                    item {
                        CodexSectionCard(
                            title = "COMMON DEFENSE PACT (CDP)",
                            subtitle = "Corporate Military Hostiles",
                            content = "• R.A.P.S. DRONES: Rapid Autonomous Personal System rolling explosive units.\n• CERBERUS MECHS: Automated heavy turret walker platforms with high armor.\n• G.I. UNITS: Cybernetic infantry bots programmed for aggressive suppression."
                        )
                    }
                    item {
                        CodexSectionCard(
                            title = "ELEMENT 115 & CORVUS AI",
                            subtitle = "Anomalies & Bosses",
                            content = "• MORG CITY UNDEAD: Reanimated victims corrupted by Element 115 in Shadows of Evil.\n• MARGWA: Multi-headed Eldritch horror requiring targeted strikes on glowing heads.\n• CORVUS AI: Rogue Mindscape super-AI threatening to consume all human neural networks."
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CodexSectionCard(title: String, subtitle: String, content: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CyberBorder, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, color = CyberPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(text = subtitle, color = CyberTertiary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = content, color = CyberOnSurface, fontSize = 13.sp, lineHeight = 18.sp)
        }
    }
}
