package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.PlayerProfileEntity
import com.example.data.model.CampaignData
import com.example.ui.theme.*

@Composable
fun MainMenuScreen(
    profile: PlayerProfileEntity?,
    onNavigateToCampaign: () -> Unit,
    onNavigateToSquad: () -> Unit,
    onNavigateToArmory: () -> Unit,
    onNavigateToCodex: () -> Unit,
    onLaunchMission: (Int) -> Unit
) {
    val currentMissionId = profile?.unlockedMissionId ?: 1
    val latestMission = CampaignData.missions.find { it.id == currentMissionId } ?: CampaignData.missions.first()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .testTag("main_menu_screen"),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Hero Header Banner with Overlay
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            ) {
                Image(
                    painter = painterResource(id = AppAssets.bannerRes),
                    contentDescription = "Tactical Legend Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    CyberBackground.copy(alpha = 0.6f),
                                    CyberBackground
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(20.dp)
                ) {
                    Surface(
                        color = CyberSecondary,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Text(
                            text = "DISTRICT OI TACTICAL COMMAND",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = "TACTICAL LEGEND",
                        color = CyberPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "THE RISE OF THE OI",
                        color = CyberOnSurface,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }
            }
        }

        // Commander Status Bar
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(CyberSurfaceVariant)
                                .border(1.dp, CyberPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MilitaryTech,
                                contentDescription = "Rank",
                                tint = CyberTertiary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = profile?.playerName ?: "Commander Oi",
                                color = CyberOnSurface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = profile?.badgeRank ?: "Street Legend",
                                color = CyberSubtext,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ResourceChip(
                            icon = Icons.Default.MonetizationOn,
                            value = "${profile?.credits ?: 0}",
                            tint = CyberTertiary
                        )

                        ResourceChip(
                            icon = Icons.Default.Memory,
                            value = "${profile?.tacticalData ?: 0}",
                            tint = CyberPrimary
                        )
                    }
                }
            }
        }

        // Quick Launch Active Mission Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .border(1.dp, CyberPrimary, RoundedCornerShape(12.dp))
                    .clickable { onLaunchMission(latestMission.id) }
                    .testTag("quick_launch_mission_button"),
                colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Launch",
                                tint = CyberGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "ACTIVE OPERATION: CH ${latestMission.chapter}",
                                color = CyberGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = latestMission.title,
                            color = CyberOnSurface,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = latestMission.subtitle,
                            color = CyberSubtext,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = { onLaunchMission(latestMission.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("DEPLOY", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Hub Navigation Grid
        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "COMMAND CENTERS",
                    color = CyberSubtext,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HubButton(
                        title = "Campaign Roadmap",
                        subtitle = "4 Operations",
                        icon = Icons.Default.Map,
                        color = CyberPrimary,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("nav_campaign_button"),
                        onClick = onNavigateToCampaign
                    )

                    HubButton(
                        title = "Oi Squad Roster",
                        subtitle = "5 Heroes",
                        icon = Icons.Default.Groups,
                        color = CyberSecondary,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("nav_squad_button"),
                        onClick = onNavigateToSquad
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HubButton(
                        title = "Cyber Armory",
                        subtitle = "Craft & Equip",
                        icon = Icons.Default.Shield,
                        color = CyberTertiary,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("nav_armory_button"),
                        onClick = onNavigateToArmory
                    )

                    HubButton(
                        title = "Tactical Codex",
                        subtitle = "Lore & Intel",
                        icon = Icons.Default.MenuBook,
                        color = CyberPurple,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("nav_codex_button"),
                        onClick = onNavigateToCodex
                    )
                }
            }
        }

        // Quick Stats / Victories Section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "TACTICAL RECORD",
                        color = CyberSubtext,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatItem(
                            label = "Victories",
                            value = "${profile?.totalVictories ?: 0}",
                            icon = Icons.Default.EmojiEvents,
                            tint = CyberTertiary
                        )
                        StatItem(
                            label = "Unlocked Ops",
                            value = "${profile?.unlockedMissionId ?: 1} / 4",
                            icon = Icons.Default.CheckCircle,
                            tint = CyberGreen
                        )
                        StatItem(
                            label = "Squad Power",
                            value = "380 PWR",
                            icon = Icons.Default.FlashOn,
                            tint = CyberSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ResourceChip(icon: ImageVector, value: String, tint: Color) {
    Surface(
        color = CyberSurfaceVariant,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = value, color = CyberOnSurface, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun HubButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(18.dp))
            }

            Column {
                Text(text = title, color = CyberOnSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(text = subtitle, color = CyberSubtext, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, icon: ImageVector, tint: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(imageVector = icon, contentDescription = label, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, color = CyberOnSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text(text = label, color = CyberSubtext, fontSize = 10.sp)
    }
}
