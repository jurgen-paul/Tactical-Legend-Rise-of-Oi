package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CampaignData
import com.example.data.model.CampaignMission
import com.example.ui.theme.*

@Composable
fun CampaignScreen(
    unlockedMissionId: Int,
    onLaunchMission: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .padding(16.dp)
            .testTag("campaign_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "CAMPAIGN OPERATIONS",
                    color = CyberPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Liberate District Oi sector by sector from corporate control.",
                    color = CyberSubtext,
                    fontSize = 12.sp
                )
            }
        }

        items(CampaignData.missions) { mission ->
            val isUnlocked = mission.id <= unlockedMissionId
            val isCurrent = mission.id == unlockedMissionId

            MissionCard(
                mission = mission,
                isUnlocked = isUnlocked,
                isCurrent = isCurrent,
                onLaunch = { onLaunchMission(mission.id) }
            )
        }
    }
}

@Composable
fun MissionCard(
    mission: CampaignMission,
    isUnlocked: Boolean,
    isCurrent: Boolean,
    onLaunch: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isCurrent) 1.5.dp else 1.dp,
                color = if (isCurrent) CyberPrimary else if (isUnlocked) CyberBorder else Color.DarkGray,
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) CyberSurface else CyberSurface.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = if (isCurrent) CyberGreen else if (isUnlocked) CyberSurfaceVariant else Color.Gray,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "CHAPTER ${mission.chapter} • MISSION 0${mission.id}",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                if (mission.isBossRaid) {
                    Surface(
                        color = CyberSecondary.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, CyberSecondary),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "BOSS RAID",
                            color = CyberSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = mission.title,
                color = if (isUnlocked) CyberOnSurface else CyberSubtext,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = mission.briefing,
                color = CyberSubtext,
                fontSize = 12.sp,
                modifier = Modifier.padding(vertical = 6.dp)
            )

            Divider(color = CyberBorder, modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "REWARDS: +${mission.rewardCredits} Credits | +${mission.rewardData} Data",
                        color = CyberTertiary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "REC POWER: ${mission.recommendedPower} PWR",
                        color = CyberSubtext,
                        fontSize = 10.sp
                    )
                }

                Button(
                    onClick = onLaunch,
                    enabled = isUnlocked,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCurrent) CyberPrimary else CyberSurfaceVariant,
                        disabledContainerColor = Color.DarkGray
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("launch_mission_${mission.id}_button")
                ) {
                    Icon(
                        imageVector = if (isUnlocked) Icons.Default.PlayArrow else Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isUnlocked) "DEPLOY" else "LOCKED",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
