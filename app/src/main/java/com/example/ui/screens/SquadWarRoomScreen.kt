package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.GearEntity
import com.example.data.db.HeroEntity
import com.example.data.model.HeroClass
import com.example.ui.theme.*

@Composable
fun SquadWarRoomScreen(
    heroes: List<HeroEntity>,
    gearList: List<GearEntity>,
    onToggleSquad: (String, Boolean) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .padding(16.dp)
            .testTag("squad_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "OI SQUAD WAR ROOM",
                    color = CyberPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Manage 'The Oi' tactical operatives, assign combat gear, and adjust deployment.",
                    color = CyberSubtext,
                    fontSize = 12.sp
                )
            }
        }

        items(heroes) { hero ->
            HeroCard(
                hero = hero,
                gearList = gearList,
                onToggleSquad = { onToggleSquad(hero.id, hero.isInSquad) }
            )
        }
    }
}

@Composable
fun HeroCard(
    hero: HeroEntity,
    gearList: List<GearEntity>,
    onToggleSquad: () -> Unit
) {
    val heroClass = hero.heroClass
    val weapon = gearList.find { it.id == hero.equippedWeaponId }
    val armor = gearList.find { it.id == hero.equippedArmorId }
    val core = gearList.find { it.id == hero.equippedCoreId }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (hero.isInSquad) CyberPrimary else CyberBorder,
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (hero.isInSquad) CyberPrimary.copy(alpha = 0.2f) else CyberSurfaceVariant)
                            .border(1.dp, if (hero.isInSquad) CyberPrimary else CyberBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (heroClass) {
                                HeroClass.VANGUARD -> "V"
                                HeroClass.SNIPER -> "S"
                                HeroClass.CIPHER -> "C"
                                HeroClass.MEDIC -> "M"
                                HeroClass.SAMURAI -> "B"
                            },
                            color = if (hero.isInSquad) CyberPrimary else CyberSubtext,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = hero.name,
                            color = CyberOnSurface,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "LVL ${hero.level} • ${heroClass.role}",
                            color = CyberSubtext,
                            fontSize = 11.sp
                        )
                    }
                }

                FilterChip(
                    selected = hero.isInSquad,
                    onClick = onToggleSquad,
                    label = {
                        Text(
                            text = if (hero.isInSquad) "DEPLOYED" else "RESERVE",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyberGreen.copy(alpha = 0.2f),
                        selectedLabelColor = CyberGreen
                    ),
                    modifier = Modifier.testTag("toggle_hero_${hero.id}_button")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stat Bars Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                HeroStatBadge("HP", "${heroClass.baseHp + (hero.level - 1) * 20 + (armor?.hpBonus ?: 0)}", CyberGreen)
                HeroStatBadge("ATK", "${heroClass.baseAtk + (hero.level - 1) * 4 + (weapon?.attackBonus ?: 0)}", CyberSecondary)
                HeroStatBadge("DEF", "${heroClass.baseDef + (hero.level - 1) * 2 + (armor?.defenseBonus ?: 0)}", CyberPrimary)
                HeroStatBadge("RNG", "${heroClass.baseRange}", CyberTertiary)
                HeroStatBadge("MOB", "${heroClass.baseMobility}", CyberPurple)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Equipped Gear Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GearSlotChip("WEAPON", weapon?.name ?: "Empty Slot", weapon != null)
                GearSlotChip("ARMOR", armor?.name ?: "Empty Slot", armor != null)
                GearSlotChip("CORE", core?.name ?: "Empty Slot", core != null)
            }
        }
    }
}

@Composable
fun HeroStatBadge(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text(text = label, color = CyberSubtext, fontSize = 10.sp)
    }
}

@Composable
fun RowScope.GearSlotChip(slot: String, name: String, isEquipped: Boolean) {
    Surface(
        modifier = Modifier.weight(1f),
        color = if (isEquipped) CyberSurfaceVariant else CyberSurface,
        border = BorderStroke(1.dp, if (isEquipped) CyberPrimary.copy(alpha = 0.5f) else CyberBorder),
        shape = RoundedCornerShape(6.dp)
    ) {
        Column(modifier = Modifier.padding(6.dp)) {
            Text(text = slot, color = CyberSubtext, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Text(text = name, color = if (isEquipped) CyberOnSurface else Color.Gray, fontSize = 10.sp, maxLines = 1)
        }
    }
}
