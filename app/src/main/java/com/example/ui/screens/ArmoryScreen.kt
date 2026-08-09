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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.GearEntity
import com.example.data.db.HeroEntity
import com.example.data.db.PlayerProfileEntity
import com.example.data.model.GearType
import com.example.data.model.Rarity
import com.example.ui.theme.*

@Composable
fun ArmoryScreen(
    profile: PlayerProfileEntity?,
    gearList: List<GearEntity>,
    heroes: List<HeroEntity>,
    onCraftGear: (() -> Unit, () -> Unit) -> Unit,
    onEquipItem: (String, GearEntity) -> Unit,
    onUnequipItem: (String, String, GearType) -> Unit
) {
    var selectedFilter by remember { mutableStateOf<GearType?>(null) }
    var selectedGearToAssign by remember { mutableStateOf<GearEntity?>(null) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val filteredGear = if (selectedFilter == null) gearList else gearList.filter { it.type == selectedFilter }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .testTag("armory_screen")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "CYBER ARMORY & FORGE",
                        color = CyberPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Forge legendary cyber weapons, kinetic weaves, and nanite cores.",
                        color = CyberSubtext,
                        fontSize = 12.sp
                    )
                }
            }

            // Crafting Forge Banner Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CyberTertiary, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = CyberSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("NANITE FORGE TERMINAL", color = CyberTertiary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Cost: 300 Credits | 150 Data", color = CyberSubtext, fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                onCraftGear(
                                    { snackbarMessage = "Crafting Successful! New Cyber Gear added." },
                                    { snackbarMessage = "Insufficient Credits or Tactical Data!" }
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberTertiary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("forge_gear_button")
                        ) {
                            Icon(Icons.Default.Build, contentDescription = "Forge", tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("FORGE", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Category Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == null,
                        onClick = { selectedFilter = null },
                        label = { Text("ALL (${gearList.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )

                    GearType.values().forEach { type ->
                        FilterChip(
                            selected = selectedFilter == type,
                            onClick = { selectedFilter = if (selectedFilter == type) null else type },
                            label = { Text(type.label, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                        )
                    }
                }
            }

            // Inventory Item Cards
            items(filteredGear) { gear ->
                GearItemCard(
                    gear = gear,
                    assignedHeroName = heroes.find { it.id == gear.assignedHeroId }?.name,
                    onAssignClick = { selectedGearToAssign = gear },
                    onUnequipClick = {
                        gear.assignedHeroId?.let { heroId ->
                            onUnequipItem(gear.id, heroId, gear.type)
                        }
                    }
                )
            }
        }

        // Hero Selection Modal to Equip Gear
        if (selectedGearToAssign != null) {
            val gear = selectedGearToAssign!!
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CyberOverlay),
                color = Color.Transparent
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Card(
                        modifier = Modifier
                            .width(320.dp)
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CyberSurface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("EQUIP TO HERO", color = CyberPrimary, fontWeight = FontWeight.Bold)
                                IconButton(onClick = { selectedGearToAssign = null }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close", tint = CyberOnSurface)
                                }
                            }

                            Text("Select an 'Oi' operative to receive ${gear.name}:", color = CyberSubtext, fontSize = 12.sp)

                            Spacer(modifier = Modifier.height(12.dp))

                            heroes.forEach { hero ->
                                Button(
                                    onClick = {
                                        onEquipItem(hero.id, gear)
                                        selectedGearToAssign = null
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceVariant),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("${hero.name} (${hero.heroClass.role})", color = CyberOnSurface, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GearItemCard(
    gear: GearEntity,
    assignedHeroName: String?,
    onAssignClick: () -> Unit,
    onUnequipClick: () -> Unit
) {
    val rarityColor = when (gear.rarity) {
        Rarity.COMMON -> RarityCommon
        Rarity.RARE -> RarityRare
        Rarity.EPIC -> RarityEpic
        Rarity.LEGENDARY -> RarityLegendary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, rarityColor.copy(alpha = 0.6f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = rarityColor.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, rarityColor),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = gear.rarity.label.uppercase(),
                            color = rarityColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(text = gear.type.label, color = CyberSubtext, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(text = gear.name, color = CyberOnSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold)

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (gear.attackBonus > 0) Text("+${gear.attackBonus} ATK", color = CyberSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    if (gear.defenseBonus > 0) Text("+${gear.defenseBonus} DEF", color = CyberPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    if (gear.hpBonus > 0) Text("+${gear.hpBonus} HP", color = CyberGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    if (gear.critBonus > 0) Text("+${gear.critBonus}% CRIT", color = CyberTertiary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (gear.isEquipped && assignedHeroName != null) {
                OutlinedButton(
                    onClick = onUnequipClick,
                    border = BorderStroke(1.dp, CyberSecondary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("EQUIPPED (${assignedHeroName.take(6)})", color = CyberSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = onAssignClick,
                    colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("EQUIP", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
