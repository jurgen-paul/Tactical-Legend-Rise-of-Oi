package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.GearEntity
import com.example.data.db.HeroEntity
import com.example.data.model.GearType
import com.example.data.model.HeroClass
import com.example.data.model.Rarity
import com.example.ui.theme.*

@Composable
fun InventoryScreen(
    gearList: List<GearEntity>,
    heroes: List<HeroEntity>,
    onEquipItem: (String, GearEntity) -> Unit,
    onUnequipItem: (String, String, GearType) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedGearType by remember { mutableStateOf<GearType?>(null) }
    var showOnlyEquipped by remember { mutableStateOf(false) }
    var showOnlyVault by remember { mutableStateOf(false) }
    var selectedGearToEquip by remember { mutableStateOf<GearEntity?>(null) }

    val filteredGear = gearList.filter { gear ->
        val matchesType = selectedGearType == null || gear.type == selectedGearType
        val matchesEquipped = if (showOnlyEquipped) gear.isEquipped else true
        val matchesVault = if (showOnlyVault) !gear.isEquipped else true
        matchesType && matchesEquipped && matchesVault
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground)
            .testTag("inventory_screen")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Title
            item {
                Column {
                    Text(
                        text = "SQUAD INVENTORY",
                        color = CyberPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Inspect stat modifiers, manage loadouts, and equip tactical gear.",
                        color = CyberSubtext,
                        fontSize = 12.sp
                    )
                }
            }

            // Inventory Summary Bar
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CyberSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CyberBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        InventorySummaryItem(
                            label = "TOTAL GEAR",
                            value = "${gearList.size}",
                            color = CyberPrimary
                        )
                        Divider(
                            modifier = Modifier
                                .height(24.dp)
                                .width(1.dp),
                            color = CyberBorder
                        )
                        InventorySummaryItem(
                            label = "EQUIPPED",
                            value = "${gearList.count { it.isEquipped }}",
                            color = CyberGreen
                        )
                        Divider(
                            modifier = Modifier
                                .height(24.dp)
                                .width(1.dp),
                            color = CyberBorder
                        )
                        InventorySummaryItem(
                            label = "VAULT STORAGE",
                            value = "${gearList.count { !it.isEquipped }}",
                            color = CyberTertiary
                        )
                    }
                }
            }

            // Filter Chips
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Type Filter Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedGearType == null,
                            onClick = { selectedGearType = null },
                            label = { Text("ALL TYPE", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.testTag("filter_type_all")
                        )

                        GearType.values().forEach { type ->
                            FilterChip(
                                selected = selectedGearType == type,
                                onClick = {
                                    selectedGearType = if (selectedGearType == type) null else type
                                },
                                label = { Text(type.label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                modifier = Modifier.testTag("filter_type_${type.name}")
                            )
                        }
                    }

                    // Status Filter Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = !showOnlyEquipped && !showOnlyVault,
                            onClick = {
                                showOnlyEquipped = false
                                showOnlyVault = false
                            },
                            label = { Text("ALL STATUS", fontSize = 10.sp) }
                        )

                        FilterChip(
                            selected = showOnlyEquipped,
                            onClick = {
                                showOnlyEquipped = !showOnlyEquipped
                                if (showOnlyEquipped) showOnlyVault = false
                            },
                            label = { Text("EQUIPPED ONLY", fontSize = 10.sp) }
                        )

                        FilterChip(
                            selected = showOnlyVault,
                            onClick = {
                                showOnlyVault = !showOnlyVault
                                if (showOnlyVault) showOnlyEquipped = false
                            },
                            label = { Text("VAULT ONLY", fontSize = 10.sp) }
                        )
                    }
                }
            }

            // Gear Inventory List
            if (filteredGear.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Inbox,
                                contentDescription = null,
                                tint = CyberSubtext,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "NO GEAR FOUND IN INVENTORY",
                                color = CyberOnSurface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Try adjusting filter options or craft new equipment at the Nanite Forge.",
                                color = CyberSubtext,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(filteredGear) { gear ->
                    val assignedHero = heroes.find { it.id == gear.assignedHeroId }

                    InventoryGearCard(
                        gear = gear,
                        assignedHero = assignedHero,
                        onEquipClick = { selectedGearToEquip = gear },
                        onUnequipClick = {
                            gear.assignedHeroId?.let { heroId ->
                                onUnequipItem(gear.id, heroId, gear.type)
                            }
                        }
                    )
                }
            }
        }

        // Equip Modal with Stat Modifier Preview
        if (selectedGearToEquip != null) {
            val gearToEquip = selectedGearToEquip!!

            EquipModalDialog(
                gear = gearToEquip,
                heroes = heroes,
                gearList = gearList,
                onDismiss = { selectedGearToEquip = null },
                onConfirmEquip = { targetHero ->
                    onEquipItem(targetHero.id, gearToEquip)
                    selectedGearToEquip = null
                }
            )
        }
    }
}

@Composable
fun InventorySummaryItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = color, fontSize = 16.sp, fontWeight = FontWeight.Black)
        Text(text = label, color = CyberSubtext, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun InventoryGearCard(
    gear: GearEntity,
    assignedHero: HeroEntity?,
    onEquipClick: () -> Unit,
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
            .border(1.dp, rarityColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .testTag("inventory_gear_card_${gear.id}"),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Rarity Tag, Gear Type, Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = rarityColor.copy(alpha = 0.15f),
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

                    Text(
                        text = gear.type.label.uppercase(),
                        color = CyberSubtext,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (gear.isEquipped && assignedHero != null) {
                    Surface(
                        color = CyberGreen.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, CyberGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = CyberGreen,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "EQUIPPED: ${assignedHero.name}",
                                color = CyberGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Surface(
                        color = CyberSurfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "IN VAULT",
                            color = CyberSubtext,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Gear Name
            Text(
                text = gear.name,
                color = CyberOnSurface,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Stat Modifiers Box
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = CyberSurfaceVariant,
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatModifierChip(label = "ATK", value = gear.attackBonus, color = CyberSecondary)
                    StatModifierChip(label = "DEF", value = gear.defenseBonus, color = CyberPrimary)
                    StatModifierChip(label = "HP", value = gear.hpBonus, color = CyberGreen)
                    StatModifierChip(label = "CRIT", value = gear.critBonus, isPercentage = true, color = CyberTertiary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (gear.isEquipped && assignedHero != null) {
                    OutlinedButton(
                        onClick = onUnequipClick,
                        border = BorderStroke(1.dp, CyberSecondary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("unequip_button_${gear.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RemoveCircleOutline,
                            contentDescription = "Unequip",
                            tint = CyberSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "UNEQUIP FROM ${assignedHero.name.take(8).uppercase()}",
                            color = CyberSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Button(
                        onClick = onEquipClick,
                        colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("equip_button_${gear.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddModerator,
                            contentDescription = "Equip",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "EQUIP TO OPERATIVE",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatModifierChip(
    label: String,
    value: Int,
    isPercentage: Boolean = false,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = if (value > 0) "+$value${if (isPercentage) "%" else ""}" else "-",
            color = if (value > 0) color else CyberSubtext,
            fontWeight = FontWeight.Black,
            fontSize = 13.sp
        )
        Text(
            text = label,
            color = CyberSubtext,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun EquipModalDialog(
    gear: GearEntity,
    heroes: List<HeroEntity>,
    gearList: List<GearEntity>,
    onDismiss: () -> Unit,
    onConfirmEquip: (HeroEntity) -> Unit
) {
    var selectedHeroForEquip by remember { mutableStateOf(heroes.firstOrNull()) }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberOverlay),
        color = Color.Transparent
    ) {
        Box(contentAlignment = Alignment.Center) {
            Card(
                modifier = Modifier
                    .width(340.dp)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, CyberPrimary)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Modal Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "EQUIP LOADOUT",
                                color = CyberPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = gear.name,
                                color = CyberOnSurface,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = CyberOnSurface)
                        }
                    }

                    Divider(color = CyberBorder, modifier = Modifier.padding(vertical = 10.dp))

                    Text(
                        text = "SELECT OPERATIVE:",
                        color = CyberSubtext,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Hero Selector Buttons
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        heroes.forEach { hero ->
                            val isSelected = selectedHeroForEquip?.id == hero.id
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedHeroForEquip = hero },
                                color = if (isSelected) CyberSurfaceVariant else CyberSurface,
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (isSelected) CyberPrimary else CyberBorder
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) CyberPrimary.copy(alpha = 0.2f) else CyberSurfaceVariant),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = when (hero.heroClass) {
                                                    HeroClass.VANGUARD -> "V"
                                                    HeroClass.SNIPER -> "S"
                                                    HeroClass.CIPHER -> "C"
                                                    HeroClass.MEDIC -> "M"
                                                    HeroClass.SAMURAI -> "B"
                                                },
                                                color = if (isSelected) CyberPrimary else CyberSubtext,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column {
                                            Text(
                                                text = hero.name,
                                                color = CyberOnSurface,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = hero.heroClass.role,
                                                color = CyberSubtext,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }

                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.RadioButtonChecked,
                                            contentDescription = "Selected",
                                            tint = CyberPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Stat Delta Preview Comparison Box
                    val targetHero = selectedHeroForEquip
                    if (targetHero != null) {
                        Spacer(modifier = Modifier.height(12.dp))

                        val currentGearId = when (gear.type) {
                            GearType.WEAPON -> targetHero.equippedWeaponId
                            GearType.ARMOR -> targetHero.equippedArmorId
                            GearType.NANITE_CORE, GearType.CIPHER_CHIP -> targetHero.equippedCoreId
                        }
                        val currentlyEquippedGear = gearList.find { it.id == currentGearId }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "STAT MODIFIER PREVIEW:",
                                    color = CyberTertiary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                val atkDiff = gear.attackBonus - (currentlyEquippedGear?.attackBonus ?: 0)
                                val defDiff = gear.defenseBonus - (currentlyEquippedGear?.defenseBonus ?: 0)
                                val hpDiff = gear.hpBonus - (currentlyEquippedGear?.hpBonus ?: 0)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    StatDiffText("ATK", atkDiff)
                                    StatDiffText("DEF", defDiff)
                                    StatDiffText("HP", hpDiff)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            selectedHeroForEquip?.let { hero ->
                                onConfirmEquip(hero)
                            }
                        },
                        enabled = selectedHeroForEquip != null,
                        colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("confirm_equip_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "CONFIRM & EQUIP",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatDiffText(label: String, diff: Int) {
    val diffText = when {
        diff > 0 -> "+$diff"
        diff < 0 -> "$diff"
        else -> "0"
    }
    val diffColor = when {
        diff > 0 -> CyberGreen
        diff < 0 -> CyberSecondary
        else -> CyberSubtext
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = "$label: ", color = CyberSubtext, fontSize = 11.sp)
        Text(text = diffText, color = diffColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}
