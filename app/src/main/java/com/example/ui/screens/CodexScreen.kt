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
            text = "TACTICAL CODEX & INTEL",
            color = CyberPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
        Text(
            text = "Archive of 'The Oi' lore, battlefield mechanics, and enemy blueprints.",
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
                Text("LORE & OI", modifier = Modifier.padding(12.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                Text("TACTICS", modifier = Modifier.padding(12.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                Text("HOSTILES", modifier = Modifier.padding(12.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            when (selectedTab) {
                0 -> {
                    item {
                        CodexSectionCard(
                            title = "THE RISE OF THE OI",
                            subtitle = "Origins of District Oi",
                            content = "In the neon-drenched dystopian sprawl of Neo-Aegis, mega-corporations reduced District 7 to an automated police state. Out of the oppressed alleys rose 'The Oi'—a street guild of tactical geniuses, rogue cyber-hackers, and urban enforcers sworn to protect the citizens and destroy corporate corruption."
                        )
                    }
                    item {
                        CodexSectionCard(
                            title = "THE SOVEREIGN CODE",
                            subtitle = "Guild Philosophy",
                            content = "1. Never leave a squad mate behind on the grid.\n2. Control the terminals, control the flow of data.\n3. Turn corporate weapons back on their creators."
                        )
                    }
                }
                1 -> {
                    item {
                        CodexSectionCard(
                            title = "COVER & TERRAIN ADVANTAGE",
                            subtitle = "Grid Combat Mechanics",
                            content = "• COVER TILES: Reduces incoming ranged damage by 30%.\n• ELEVATED TILES: Grants +20% attack bonus when firing at lower elevation.\n• HAZARD LEAKS: Deals 15 toxic damage per turn.\n• CONTROL POINTS: Holding Cyber Terminals for 2 turns triggers instant victory."
                        )
                    }
                    item {
                        CodexSectionCard(
                            title = "ACTION POINT (AP) ECONOMY",
                            subtitle = "Turn Strategy",
                            content = "Each hero starts their turn with 4-5 AP. Moving costs 1 AP per tile, Basic Attacks cost 2 AP, and Ultimate Skills cost 2-3 AP. Combine movement and cover placement before unleashing skills."
                        )
                    }
                }
                2 -> {
                    item {
                        CodexSectionCard(
                            title = "CORPORATE DRONES & ENFORCERS",
                            subtitle = "Standard Patrol Units",
                            content = "Automated security drones with medium mobility and kinetic rifles. Susceptible to Cipher hacker stuns."
                        )
                    }
                    item {
                        CodexSectionCard(
                            title = "HEAVY TITAN MECHS & BOSSES",
                            subtitle = "Classified Threats",
                            content = "XERXES-9 TITAN is equipped with missile barrage systems and kinetic shield matrices. Focus Vanguard taunts and Sniper armor-piercing rounds to crack its armor."
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
