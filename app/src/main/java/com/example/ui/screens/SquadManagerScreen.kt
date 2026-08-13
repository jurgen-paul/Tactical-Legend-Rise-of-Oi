package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.GearEntity
import com.example.data.db.HeroEntity
import com.example.data.model.CampaignData
import com.example.data.model.CampaignMission
import com.example.data.model.HeroClass
import com.example.ui.theme.*
import com.example.util.SoundManager
import kotlinx.coroutines.launch

enum class SquadFilter(val label: String) {
    ALL("ALL UNITS"),
    DEPLOYED("DEPLOYED"),
    RESERVES("RESERVES"),
    VANGUARD("VANGUARD"),
    SNIPER("SNIPER"),
    CIPHER("CIPHER"),
    MEDIC("MEDIC"),
    SAMURAI("SAMURAI")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SquadManagerScreen(
    heroes: List<HeroEntity>,
    gearList: List<GearEntity>,
    unlockedMissionId: Int = 1,
    onToggleSquad: (String, Boolean) -> Unit,
    onLaunchMission: (Int) -> Unit = {},
    onNavigateToArmory: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf(SquadFilter.ALL) }
    var selectedMissionId by remember { mutableIntStateOf(minOf(unlockedMissionId, CampaignData.missions.maxOf { it.id })) }
    var selectedHeroForInspect by remember { mutableStateOf<HeroEntity?>(null) }
    var showMissionSelectDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val deployedHeroes = remember(heroes) { heroes.filter { it.isInSquad } }
    val totalDeployed = deployedHeroes.size

    // Calculate total squad power index
    val squadPower = remember(heroes, gearList) {
        deployedHeroes.sumOf { hero ->
            val heroClass = hero.heroClass
            val weapon = gearList.find { it.id == hero.equippedWeaponId }
            val armor = gearList.find { it.id == hero.equippedArmorId }
            val core = gearList.find { it.id == hero.equippedCoreId }

            val totalHp = heroClass.baseHp + (hero.level - 1) * 20 + (armor?.hpBonus ?: 0) + (core?.hpBonus ?: 0)
            val totalAtk = heroClass.baseAtk + (hero.level - 1) * 4 + (weapon?.attackBonus ?: 0) + (core?.attackBonus ?: 0)
            val totalDef = heroClass.baseDef + (hero.level - 1) * 2 + (armor?.defenseBonus ?: 0)
            (totalHp / 3) + (totalAtk * 3) + (totalDef * 2)
        }
    }

    val selectedMission = remember(selectedMissionId) {
        CampaignData.missions.find { it.id == selectedMissionId } ?: CampaignData.missions.first()
    }

    val filteredHeroes = remember(heroes, selectedFilter) {
        when (selectedFilter) {
            SquadFilter.ALL -> heroes
            SquadFilter.DEPLOYED -> heroes.filter { it.isInSquad }
            SquadFilter.RESERVES -> heroes.filter { !it.isInSquad }
            SquadFilter.VANGUARD -> heroes.filter { it.heroClass == HeroClass.VANGUARD }
            SquadFilter.SNIPER -> heroes.filter { it.heroClass == HeroClass.SNIPER }
            SquadFilter.CIPHER -> heroes.filter { it.heroClass == HeroClass.CIPHER }
            SquadFilter.MEDIC -> heroes.filter { it.heroClass == HeroClass.MEDIC }
            SquadFilter.SAMURAI -> heroes.filter { it.heroClass == HeroClass.SAMURAI }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = CyberBackground,
        modifier = modifier.testTag("squad_manager_screen")
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top Header HUD Card
                SquadHeaderHud(
                    totalDeployed = totalDeployed,
                    totalHeroes = heroes.size,
                    squadPower = squadPower,
                    deployedClasses = deployedHeroes.map { it.heroClass }.distinct()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Mission Deployment Quick Launcher Banner
                MissionLaunchBanner(
                    mission = selectedMission,
                    squadPower = squadPower,
                    onChangeMission = {
                        SoundManager.playClickSound()
                        showMissionSelectDialog = true
                    },
                    onLaunchMission = {
                        if (totalDeployed == 0) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Deploy at least 1 operative into the squad before launching!")
                            }
                        } else {
                            SoundManager.playVictorySound()
                            onLaunchMission(selectedMission.id)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Filter Bar
                SquadFilterRow(
                    selectedFilter = selectedFilter,
                    onSelectFilter = { filter ->
                        SoundManager.playClickSound()
                        selectedFilter = filter
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Operatives List
                if (filteredHeroes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.FilterListOff,
                                contentDescription = null,
                                tint = CyberSubtext,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "NO OPERATIVES IN THIS CATEGORY",
                                color = CyberSubtext,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredHeroes, key = { it.id }) { hero ->
                            OperativeCard(
                                hero = hero,
                                gearList = gearList,
                                onToggleSquad = {
                                    if (hero.isInSquad && totalDeployed <= 1) {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Minimum 1 operative required in squad!")
                                        }
                                    } else {
                                        if (!hero.isInSquad) {
                                            SoundManager.playShieldHealSound()
                                        } else {
                                            SoundManager.playClickSound()
                                        }
                                        onToggleSquad(hero.id, hero.isInSquad)
                                    }
                                },
                                onInspect = {
                                    SoundManager.playClickSound()
                                    selectedHeroForInspect = hero
                                }
                            )
                        }
                    }
                }
            }

            // Hero Inspection Modal Dialog
            selectedHeroForInspect?.let { hero ->
                OperativeInspectDialog(
                    hero = hero,
                    gearList = gearList,
                    onDismiss = { selectedHeroForInspect = null },
                    onToggleSquad = {
                        if (hero.isInSquad && totalDeployed <= 1) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Minimum 1 operative required in squad!")
                            }
                        } else {
                            onToggleSquad(hero.id, hero.isInSquad)
                            selectedHeroForInspect = null
                        }
                    },
                    onNavigateToArmory = {
                        selectedHeroForInspect = null
                        onNavigateToArmory()
                    }
                )
            }

            // Mission Selection Modal Dialog
            if (showMissionSelectDialog) {
                MissionSelectDialog(
                    unlockedMissionId = unlockedMissionId,
                    selectedMissionId = selectedMissionId,
                    onSelectMission = { missionId ->
                        selectedMissionId = missionId
                        showMissionSelectDialog = false
                    },
                    onDismiss = { showMissionSelectDialog = false }
                )
            }
        }
    }
}

@Composable
private fun SquadHeaderHud(
    totalDeployed: Int,
    totalHeroes: Int,
    squadPower: Int,
    deployedClasses: List<HeroClass>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, CyberPrimary)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "OISTARS OPS 1 SPECIALIST MANAGER",
                        color = CyberPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "DNI SPECIALIST ROSTER & CYBER CORE MATRIX",
                        color = CyberSubtext,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    color = CyberPrimary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, CyberPrimary)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = null,
                            tint = CyberYellow,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$squadPower PWR",
                            color = CyberYellow,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Deployed Count and Class Synergy Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = if (totalDeployed > 0) CyberGreen.copy(alpha = 0.2f) else CyberSurfaceVariant,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, if (totalDeployed > 0) CyberGreen else CyberBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = null,
                            tint = if (totalDeployed > 0) CyberGreen else CyberSubtext,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ROSTER: $totalDeployed / $totalHeroes DEPLOYED",
                            color = if (totalDeployed > 0) CyberGreen else CyberSubtext,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Class Role Chips
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    HeroClass.values().forEach { heroClass ->
                        val isPresent = deployedClasses.contains(heroClass)
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(if (isPresent) CyberPrimary else CyberSurfaceVariant)
                                .border(1.dp, if (isPresent) CyberPrimary else CyberBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = heroClass.name.first().toString(),
                                color = if (isPresent) Color.Black else CyberSubtext,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MissionLaunchBanner(
    mission: CampaignMission,
    squadPower: Int,
    onChangeMission: () -> Unit,
    onLaunchMission: () -> Unit
) {
    val isPowerSufficient = squadPower >= mission.recommendedPower

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (isPowerSufficient) CyberGreen.copy(alpha = 0.8f) else CyberYellow)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = null,
                        tint = CyberPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "TARGET: CH ${mission.chapter} • ${mission.title}",
                        color = CyberOnSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                TextButton(
                    onClick = onChangeMission,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "CHANGE",
                        color = CyberPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "REC PWR: ${mission.recommendedPower} PWR",
                        color = if (isPowerSufficient) CyberGreen else CyberYellow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "REWARDS: +${mission.rewardCredits} CR | +${mission.rewardData} DATA",
                        color = CyberSubtext,
                        fontSize = 10.sp
                    )
                }

                Button(
                    onClick = onLaunchMission,
                    colors = ButtonDefaults.buttonColors(containerColor = CyberGreen),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("launch_mission_deployment_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "DEPLOY SQUAD",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun SquadFilterRow(
    selectedFilter: SquadFilter,
    onSelectFilter: (SquadFilter) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(SquadFilter.values()) { filter ->
            val isSelected = filter == selectedFilter
            val tag = "filter_tab_${filter.name.lowercase()}"

            Button(
                onClick = { onSelectFilter(filter) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) CyberPrimary else CyberSurface
                ),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, if (isSelected) CyberPrimary else CyberBorder),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier
                    .height(32.dp)
                    .testTag(tag)
            ) {
                Text(
                    text = filter.label,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.Black else CyberSubtext
                )
            }
        }
    }
}

@Composable
private fun OperativeCard(
    hero: HeroEntity,
    gearList: List<GearEntity>,
    onToggleSquad: () -> Unit,
    onInspect: () -> Unit
) {
    val heroClass = hero.heroClass
    val weapon = gearList.find { it.id == hero.equippedWeaponId }
    val armor = gearList.find { it.id == hero.equippedArmorId }
    val core = gearList.find { it.id == hero.equippedCoreId }

    val totalHp = heroClass.baseHp + (hero.level - 1) * 20 + (armor?.hpBonus ?: 0) + (core?.hpBonus ?: 0)
    val totalAtk = heroClass.baseAtk + (hero.level - 1) * 4 + (weapon?.attackBonus ?: 0) + (core?.attackBonus ?: 0)
    val totalDef = heroClass.baseDef + (hero.level - 1) * 2 + (armor?.defenseBonus ?: 0)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hero_card_${hero.id}"),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = if (hero.isInSquad) 1.5.dp else 1.dp,
            color = if (hero.isInSquad) CyberPrimary else CyberBorder
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onInspect() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (hero.isInSquad) CyberPrimary.copy(alpha = 0.2f) else CyberSurfaceVariant)
                            .border(1.5.dp, if (hero.isInSquad) CyberPrimary else CyberBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = heroClass.name.first().toString(),
                            color = if (hero.isInSquad) CyberPrimary else CyberSubtext,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = hero.name,
                                color = CyberOnSurface,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = CyberPrimary.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "LVL ${hero.level}",
                                    color = CyberPrimary,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }

                        Text(
                            text = heroClass.role,
                            color = CyberSubtext,
                            fontSize = 11.sp
                        )
                    }
                }

                // Deploy / Reserve Action Button
                Button(
                    onClick = onToggleSquad,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hero.isInSquad) CyberGreen else CyberSurfaceVariant
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier
                        .height(32.dp)
                        .testTag("deploy_hero_${hero.id}_button")
                ) {
                    Icon(
                        imageVector = if (hero.isInSquad) Icons.Default.CheckCircle else Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (hero.isInSquad) Color.White else CyberSubtext
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (hero.isInSquad) "DEPLOYED" else "RESERVE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (hero.isInSquad) Color.White else CyberSubtext
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stat Metrics Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CyberSurfaceVariant, RoundedCornerShape(8.dp))
                    .padding(vertical = 6.dp, horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatMetric("HP", "$totalHp", CyberGreen)
                StatMetric("ATK", "$totalAtk", CyberSecondary)
                StatMetric("DEF", "$totalDef", CyberPrimary)
                StatMetric("RNG", "${heroClass.baseRange} tiles", CyberYellow)
                StatMetric("MOB", "${heroClass.baseMobility} tiles", CyberPurple)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Equipped Gear Loadout Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MiniGearSlotChip("WEAPON", weapon?.name ?: "Empty Slot", weapon != null)
                MiniGearSlotChip("ARMOR", armor?.name ?: "Empty Slot", armor != null)
                MiniGearSlotChip("CORE", core?.name ?: "Empty Slot", core != null)
            }
        }
    }
}

@Composable
private fun StatMetric(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = color, fontSize = 12.sp, fontWeight = FontWeight.Black)
        Text(text = label, color = CyberSubtext, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun RowScope.MiniGearSlotChip(slot: String, name: String, isEquipped: Boolean) {
    Surface(
        modifier = Modifier.weight(1f),
        color = if (isEquipped) CyberBackground else CyberSurfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, if (isEquipped) CyberPrimary.copy(alpha = 0.4f) else CyberBorder),
        shape = RoundedCornerShape(6.dp)
    ) {
        Column(modifier = Modifier.padding(4.dp)) {
            Text(text = slot, color = CyberSubtext, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            Text(
                text = name,
                color = if (isEquipped) CyberOnSurface else Color.Gray,
                fontSize = 9.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun OperativeInspectDialog(
    hero: HeroEntity,
    gearList: List<GearEntity>,
    onDismiss: () -> Unit,
    onToggleSquad: () -> Unit,
    onNavigateToArmory: () -> Unit
) {
    val heroClass = hero.heroClass
    val weapon = gearList.find { it.id == hero.equippedWeaponId }
    val armor = gearList.find { it.id == hero.equippedArmorId }
    val core = gearList.find { it.id == hero.equippedCoreId }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onNavigateToArmory,
                    border = BorderStroke(1.dp, CyberPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("ARMORY GEAR", color = CyberPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onToggleSquad,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hero.isInSquad) CyberSecondary else CyberGreen
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (hero.isInSquad) "REMOVE TO RESERVES" else "DEPLOY TO SQUAD",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CLOSE", color = CyberSubtext, fontSize = 11.sp)
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(CyberPrimary.copy(alpha = 0.2f))
                        .border(1.dp, CyberPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = heroClass.name.first().toString(),
                        color = CyberPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = hero.name,
                        color = CyberOnSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${heroClass.title} • LVL ${hero.level}",
                        color = CyberSubtext,
                        fontSize = 10.sp
                    )
                }
            }
        },
        text = {
            Column {
                Text(
                    text = heroClass.description,
                    color = CyberSubtext,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(text = "BASE STATS & GEAR BONUSES", fontSize = 10.sp, color = CyberPrimary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CyberSurfaceVariant, RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    StatRow("Health Points (HP)", "${heroClass.baseHp + (hero.level - 1) * 20}", "+${(armor?.hpBonus ?: 0) + (core?.hpBonus ?: 0)}", CyberGreen)
                    StatRow("Attack Power (ATK)", "${heroClass.baseAtk + (hero.level - 1) * 4}", "+${(weapon?.attackBonus ?: 0) + (core?.attackBonus ?: 0)}", CyberSecondary)
                    StatRow("Defense (DEF)", "${heroClass.baseDef + (hero.level - 1) * 2}", "+${armor?.defenseBonus ?: 0}", CyberPrimary)
                    StatRow("Range", "${heroClass.baseRange} tiles", "-", CyberYellow)
                    StatRow("Mobility", "${heroClass.baseMobility} tiles", "-", CyberPurple)
                }
            }
        },
        containerColor = CyberSurface,
        shape = RoundedCornerShape(14.dp)
    )
}

@Composable
private fun StatRow(label: String, base: String, bonus: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 11.sp, color = CyberOnSurface)
        Row {
            Text(text = base, fontSize = 11.sp, color = color, fontWeight = FontWeight.Bold)
            if (bonus != "-") {
                Text(text = " ($bonus)", fontSize = 11.sp, color = CyberGreen)
            }
        }
    }
}

@Composable
private fun MissionSelectDialog(
    unlockedMissionId: Int,
    selectedMissionId: Int,
    onSelectMission: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = CyberSubtext, fontSize = 11.sp)
            }
        },
        title = {
            Text(
                text = "SELECT CAMPAIGN MISSION",
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = CyberPrimary
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 320.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(CampaignData.missions) { mission ->
                    val isUnlocked = mission.id <= unlockedMissionId
                    val isSelected = mission.id == selectedMissionId

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = isUnlocked) {
                                onSelectMission(mission.id)
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) CyberSurfaceVariant else CyberSurface
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) CyberPrimary else if (isUnlocked) CyberBorder else Color.DarkGray
                        )
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "CH ${mission.chapter} • ${mission.title}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isUnlocked) CyberOnSurface else Color.Gray
                                )
                                if (!isUnlocked) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Text(
                                text = "REC PWR: ${mission.recommendedPower} PWR",
                                fontSize = 10.sp,
                                color = CyberYellow
                            )
                        }
                    }
                }
            }
        },
        containerColor = CyberSurface,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.testTag("mission_select_dialog")
    )
}
