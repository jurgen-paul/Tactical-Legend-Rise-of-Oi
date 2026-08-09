package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
    var selectedArmoryTab by remember { mutableStateOf(0) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .testTag("armory_screen")
    ) {
        // Armory Section Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CYBER ARMORY & FORGE",
                        color = CyberPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Manage equipment inventory and forge high-tech tactical gear.",
                        color = CyberSubtext,
                        fontSize = 12.sp
                    )
                }

                // Quick Forge Button in Header
                Button(
                    onClick = {
                        onCraftGear(
                            { snackbarMessage = "Crafted new Cyber Gear successfully!" },
                            { snackbarMessage = "Insufficient Credits or Data!" }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberTertiary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("armory_quick_forge_button")
                ) {
                    Icon(Icons.Default.Build, contentDescription = "Forge", tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("FORGE (300 CR)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Navigation Tabs
            TabRow(
                selectedTabIndex = selectedArmoryTab,
                containerColor = CyberSurface,
                contentColor = CyberPrimary
            ) {
                Tab(
                    selected = selectedArmoryTab == 0,
                    onClick = { selectedArmoryTab = 0 },
                    modifier = Modifier.testTag("armory_tab_inventory")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("INVENTORY (${gearList.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Tab(
                    selected = selectedArmoryTab == 1,
                    onClick = { selectedArmoryTab = 1 },
                    modifier = Modifier.testTag("armory_tab_forge")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("NANITE FORGE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Tab Content
        Box(modifier = Modifier.weight(1f)) {
            when (selectedArmoryTab) {
                0 -> {
                    InventoryScreen(
                        gearList = gearList,
                        heroes = heroes,
                        onEquipItem = onEquipItem,
                        onUnequipItem = onUnequipItem
                    )
                }
                1 -> {
                    ForgeTerminalTab(
                        profile = profile,
                        onCraftGear = {
                            onCraftGear(
                                { snackbarMessage = "Crafting Successful! Check Inventory." },
                                { snackbarMessage = "Insufficient Credits or Data!" }
                            )
                        }
                    )
                }
            }

            // Snackbar notification overlay
            snackbarMessage?.let { msg ->
                Snackbar(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomCenter),
                    action = {
                        TextButton(onClick = { snackbarMessage = null }) {
                            Text("DISMISS", color = CyberPrimary, fontWeight = FontWeight.Bold)
                        }
                    },
                    containerColor = CyberSurfaceVariant,
                    contentColor = CyberOnSurface
                ) {
                    Text(msg, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun ForgeTerminalTab(
    profile: PlayerProfileEntity?,
    onCraftGear: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, CyberTertiary, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.PrecisionManufacturing,
                    contentDescription = null,
                    tint = CyberTertiary,
                    modifier = Modifier.size(56.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "NANITE FORGE MATRIX",
                    color = CyberPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = "Synthesize military-grade cybernetics, high-frequency blades, kinetic weaves, and neural processors.",
                    color = CyberSubtext,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Divider(color = CyberBorder, modifier = Modifier.padding(vertical = 12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    ResourceBalanceBadge("CREDITS", "${profile?.credits ?: 0}", Icons.Default.MonetizationOn, CyberTertiary)
                    ResourceBalanceBadge("DATA", "${profile?.tacticalData ?: 0}", Icons.Default.Memory, CyberPrimary)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("FORGE COST PER ITEM:", color = CyberSubtext, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("• 300 Cyber Credits", color = CyberOnSurface, fontSize = 12.sp)
                        Text("• 150 Tactical Data", color = CyberOnSurface, fontSize = 12.sp)
                        Text("• Rarity Chance: 60% Common, 25% Rare, 10% Epic, 5% Legendary", color = CyberTertiary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onCraftGear,
                    colors = ButtonDefaults.buttonColors(containerColor = CyberTertiary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("forge_execute_button")
                ) {
                    Icon(Icons.Default.Build, contentDescription = "Forge", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SYNTHESIZE NEW GEAR", color = Color.White, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun ResourceBalanceBadge(label: String, amount: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = label, tint = color, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = amount, color = CyberOnSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Text(text = label, color = CyberSubtext, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}
