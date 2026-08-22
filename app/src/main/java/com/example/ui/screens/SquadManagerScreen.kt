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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.GearEntity
import com.example.data.db.HeroEntity
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.util.SoundManager
import kotlinx.coroutines.launch

enum class SquadFilter(val label: String) {
    ALL("ALL SPECIALISTS"),
    DEPLOYED("ASSIGNED SQUAD"),
    RESERVES("RESERVES"),
    VANGUARD("VANGUARD"),
    CIPHER("CIPHER"),
    SNIPER("SNIPER"),
    MEDIC("MEDIC"),
    SAMURAI("SAMURAI")
}

/**
 * 'The Oi' Squad Members & Campaign Assignment Screen
 * Allows viewing all Black Ops III Specialists (Ruin, Prophet, Outrider, Battery, Seraph, Nomad, Spectre, Reaper, Firebreak)
 * and assigning/deploying them to the active Campaign mission.
 */
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
    var selectedMissionId by remember {
        mutableIntStateOf(minOf(unlockedMissionId, CampaignData.missions.maxOf { it.id }))
    }
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
            SquadFilter.CIPHER -> heroes.filter { it.heroClass == HeroClass.CIPHER }
            SquadFilter.SNIPER -> heroes.filter { it.heroClass == HeroClass.SNIPER }
            SquadFilter.MEDIC -> heroes.filter { it.heroClass == HeroClass.MEDIC }
            SquadFilter.SAMURAI -> heroes.filter { it.heroClass == HeroClass.SAMURAI }
        }
    }

    // Active synergies calculation
    val activeSynergies = remember(deployedHeroes) {
        val classes = deployedHeroes.map { it.heroClass }.toSet()
        val list = mutableListOf<String>()
        if (classes.contains(HeroClass.VANGUARD) && classes.contains(HeroClass.CIPHER)) {
            list.add("EMP Breacher (Vanguard + Cipher)")
        }
        if (classes.contains(HeroClass.SNIPER) && classes.contains(HeroClass.SAMURAI)) {
            list.add("Shadow Recon (Sniper + Samurai)")
        }
        if (classes.contains(HeroClass.MEDIC) && classes.contains(HeroClass.VANGUARD)) {
            list.add("Immortal Bulwark (Medic + Vanguard)")
        }
        if (classes.size >= 4) {
            list.add("Combined Arms Mastery (4 Unique Classes)")
        }
        list
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = CyberBackground,
        modifier = modifier.testTag("squad_manager_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    // Header Card
                    OiSquadHeaderHud(
                        totalDeployed = totalDeployed,
                        totalSpecialists = heroes.size,
                        squadPower = squadPower,
                        activeSynergiesCount = activeSynergies.size
                    )
                }

                // Active Campaign Mission Assignment Banner
                item {
                    CampaignAssignmentBanner(
                        mission = selectedMission,
                        squadPower = squadPower,
                        deployedCount = totalDeployed,
                        activeSynergies = activeSynergies,
                        onChangeMission = {
                            SoundManager.playClickSound()
                            showMissionSelectDialog = true
                        },
                        onDeploySquad = {
                            if (totalDeployed == 0) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Deploy at least 1 'The Oi' specialist to assign into the campaign!")
                                }
                            } else {
                                SoundManager.playVictorySound()
                                onLaunchMission(selectedMission.id)
                            }
                        }
                    )
                }

                // Quick Strike Team Formations
                item {
                    SquadPresetFormationsRow(
                        onSelectPreset = { presetHeroIds ->
                            SoundManager.playShieldHealSound()
                            heroes.forEach { hero ->
                                val shouldBeInSquad = hero.id in presetHeroIds
                                if (hero.isInSquad != shouldBeInSquad) {
                                    onToggleSquad(hero.id, hero.isInSquad)
                                }
                            }
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Applied Strike Team Formation!")
                            }
                        }
                    )
                }

                // Assigned Squad Quick Strip
                if (deployedHeroes.isNotEmpty()) {
                    item {
                        AssignedSquadStrip(
                            deployedHeroes = deployedHeroes,
                            gearList = gearList,
                            onInspect = { hero ->
                                SoundManager.playClickSound()
                                selectedHeroForInspect = hero
                            },
                            onUnassign = { hero ->
                                if (totalDeployed <= 1) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Minimum 1 operative required in campaign squad!")
                                    }
                                } else {
                                    SoundManager.playClickSound()
                                    onToggleSquad(hero.id, true)
                                }
                            }
                        )
                    }
                }

                // Filter Row
                item {
                    OiSquadFilterRow(
                        selectedFilter = selectedFilter,
                        totalHeroes = heroes.size,
                        deployedCount = totalDeployed,
                        reservesCount = heroes.size - totalDeployed,
                        onSelectFilter = { filter ->
                            SoundManager.playClickSound()
                            selectedFilter = filter
                        }
                    )
                }

                // Specialists List
                if (filteredHeroes.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
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
                                    text = "NO SPECIALISTS IN THIS CATEGORY",
                                    color = CyberSubtext,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else {
                    items(filteredHeroes, key = { it.id }) { hero ->
                        OiSpecialistCard(
                            hero = hero,
                            gearList = gearList,
                            onToggleSquad = {
                                if (hero.isInSquad && totalDeployed <= 1) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Minimum 1 operative required in campaign squad!")
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

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Bottom Fixed Launch Action Bar
            Surface(
                color = CyberSurface,
                border = BorderStroke(1.dp, CyberPrimary.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "ASSIGNED TO:",
                                color = CyberSubtext,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "M0${selectedMission.id}: ${selectedMission.title}",
                                color = CyberPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = "${totalDeployed} Active Operatives • PWR $squadPower vs ${selectedMission.recommendedPower}",
                            color = if (squadPower >= selectedMission.recommendedPower) CyberGreen else CyberYellow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = {
                            if (totalDeployed == 0) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Deploy at least 1 specialist before launching!")
                                }
                            } else {
                                SoundManager.playVictorySound()
                                onLaunchMission(selectedMission.id)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (totalDeployed > 0) CyberSecondary else CyberSurfaceVariant
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .height(44.dp)
                            .testTag("launch_assigned_campaign_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SportsEsports,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "DISPATCH SQUAD",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Operative Dossier / Lore Inspection Dialog
        selectedHeroForInspect?.let { hero ->
            OiSpecialistDossierDialog(
                hero = hero,
                gearList = gearList,
                totalDeployed = totalDeployed,
                onDismiss = { selectedHeroForInspect = null },
                onToggleSquad = {
                    if (hero.isInSquad && totalDeployed <= 1) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Minimum 1 operative required in campaign squad!")
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

        // Campaign Mission Selection Modal Dialog
        if (showMissionSelectDialog) {
            OiCampaignSelectDialog(
                unlockedMissionId = unlockedMissionId,
                selectedMissionId = selectedMissionId,
                squadPower = squadPower,
                onSelectMission = { missionId ->
                    selectedMissionId = missionId
                    showMissionSelectDialog = false
                    SoundManager.playClickSound()
                },
                onDismiss = { showMissionSelectDialog = false }
            )
        }
    }
}

/**
 * Alias Composable for OiSquadAssignmentScreen
 */
@Composable
fun OiSquadAssignmentScreen(
    heroes: List<HeroEntity>,
    gearList: List<GearEntity>,
    unlockedMissionId: Int = 1,
    onToggleSquad: (String, Boolean) -> Unit,
    onLaunchMission: (Int) -> Unit = {},
    onNavigateToArmory: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    SquadManagerScreen(
        heroes = heroes,
        gearList = gearList,
        unlockedMissionId = unlockedMissionId,
        onToggleSquad = onToggleSquad,
        onLaunchMission = onLaunchMission,
        onNavigateToArmory = onNavigateToArmory,
        modifier = modifier
    )
}

/**
 * Header HUD displaying the Oi Squad status, power rating, and synergies
 */
@Composable
private fun OiSquadHeaderHud(
    totalDeployed: Int,
    totalSpecialists: Int,
    squadPower: Int,
    activeSynergiesCount: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CyberPrimary.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .testTag("oi_squad_header_hud"),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        shape = RoundedCornerShape(16.dp)
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
                            .background(
                                Brush.linearGradient(
                                    listOf(CyberPrimary.copy(alpha = 0.3f), CyberSecondary.copy(alpha = 0.3f))
                                )
                            )
                            .border(1.dp, CyberPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = null,
                            tint = CyberPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "THE OI SQUAD",
                                color = CyberPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = CyberSecondary.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "BLACK OPS III",
                                    color = CyberSecondary,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Specialist Operatives & Campaign Roster",
                            color = CyberSubtext,
                            fontSize = 11.sp
                        )
                    }
                }

                Surface(
                    color = if (totalDeployed in 1..4) CyberGreen.copy(alpha = 0.15f) else CyberRed.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(
                        1.dp,
                        if (totalDeployed in 1..4) CyberGreen.copy(alpha = 0.5f) else CyberRed.copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        text = "$totalDeployed / $totalSpecialists DEPLOYED",
                        color = if (totalDeployed in 1..4) CyberGreen else CyberRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Stats Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SquadStatBadge(
                    label = "COMBAT POWER",
                    value = "$squadPower",
                    icon = Icons.Default.Bolt,
                    tint = CyberTertiary,
                    modifier = Modifier.weight(1f)
                )
                SquadStatBadge(
                    label = "ACTIVE SYNERGIES",
                    value = "$activeSynergiesCount BUFFS",
                    icon = Icons.Default.AutoAwesome,
                    tint = CyberPrimary,
                    modifier = Modifier.weight(1f)
                )
                SquadStatBadge(
                    label = "ROSTER STATUS",
                    value = "$totalSpecialists SPECIALISTS",
                    icon = Icons.Default.VerifiedUser,
                    tint = CyberGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SquadStatBadge(
    label: String,
    value: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = CyberSurfaceVariant,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = label,
                    color = CyberSubtext,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = value,
                    color = CyberOnSurface,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

/**
 * Campaign Assignment and Readiness Banner
 */
@Composable
private fun CampaignAssignmentBanner(
    mission: CampaignMission,
    squadPower: Int,
    deployedCount: Int,
    activeSynergies: List<String>,
    onChangeMission: () -> Unit,
    onDeploySquad: () -> Unit
) {
    val isPowerSufficient = squadPower >= mission.recommendedPower
    val powerRatio = (squadPower.toFloat() / mission.recommendedPower.toFloat()).coerceIn(0f, 2f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CyberTertiary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .testTag("campaign_assignment_banner"),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = null,
                        tint = CyberTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ASSIGNED CAMPAIGN MISSION",
                        color = CyberTertiary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }

                TextButton(
                    onClick = onChangeMission,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.testTag("change_campaign_mission_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = null,
                        tint = CyberPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "CHANGE MISSION",
                        color = CyberPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Mission Details Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Chapter ${mission.chapter}: ${mission.title}",
                        color = CyberOnSurface,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = mission.subtitle,
                        color = CyberSubtext,
                        fontSize = 12.sp
                    )
                }

                Surface(
                    color = if (mission.isBossRaid) CyberRed.copy(alpha = 0.2f) else CyberPrimary.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (mission.isBossRaid) "BOSS RAID" else "CAMPAIGN OP",
                        color = if (mission.isBossRaid) CyberRed else CyberPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Power Matching Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Squad Power: $squadPower / ${mission.recommendedPower} Req",
                    color = if (isPowerSufficient) CyberGreen else CyberYellow,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = if (powerRatio >= 1.25f) "OVERMATCH (OPTIMAL)"
                    else if (powerRatio >= 1.0f) "FAVORABLE COMBAT"
                    else if (powerRatio >= 0.75f) "CONTESTED THREAT"
                    else "EXTREME DANGER",
                    color = if (powerRatio >= 1.0f) CyberGreen else CyberRed,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { (powerRatio / 1.5f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (isPowerSufficient) CyberGreen else CyberYellow,
                trackColor = CyberSurfaceVariant
            )

            // Active Synergies Chips
            if (activeSynergies.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "ACTIVE SQUAD SYNERGIES:",
                    color = CyberSubtext,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(activeSynergies) { synergy ->
                        Surface(
                            color = CyberPrimary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, CyberPrimary.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = CyberPrimary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = synergy,
                                    color = CyberPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Rewards Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = null,
                        tint = CyberYellow,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "+${mission.rewardCredits} CR",
                        color = CyberYellow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = null,
                        tint = CyberPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "+${mission.rewardData} DATA",
                        color = CyberPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onDeploySquad,
                    colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("launch_mission_quick_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "DEPLOY INTO BATTLE",
                        color = Color.Black,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

/**
 * Quick squad preset buttons
 */
@Composable
private fun SquadPresetFormationsRow(
    onSelectPreset: (List<String>) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "QUICK STRIKE FORMATIONS",
                color = CyberSubtext,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                PresetChip(
                    title = "Assault Team",
                    subtitle = "Ruin • Battery • Firebreak • Prophet",
                    icon = Icons.Default.FlashOn,
                    tint = CyberRed,
                    onClick = {
                        onSelectPreset(listOf("hero_vanguard", "hero_medic", "hero_firebreak", "hero_cipher"))
                    }
                )
            }
            item {
                PresetChip(
                    title = "Cyber Recon",
                    subtitle = "Prophet • Outrider • Nomad • Spectre",
                    icon = Icons.Default.TrackChanges,
                    tint = CyberPrimary,
                    onClick = {
                        onSelectPreset(listOf("hero_cipher", "hero_sniper", "hero_nomad", "hero_spectre"))
                    }
                )
            }
            item {
                PresetChip(
                    title = "Syndicate Blade",
                    subtitle = "Seraph • Spectre • Ruin • Reaper",
                    icon = Icons.Default.MilitaryTech,
                    tint = CyberSecondary,
                    onClick = {
                        onSelectPreset(listOf("hero_samurai", "hero_spectre", "hero_vanguard", "hero_reaper"))
                    }
                )
            }
            item {
                PresetChip(
                    title = "Heavy Mech Defense",
                    subtitle = "Reaper • Battery • Ruin • Firebreak",
                    icon = Icons.Default.SmartToy,
                    tint = CyberTertiary,
                    onClick = {
                        onSelectPreset(listOf("hero_reaper", "hero_medic", "hero_vanguard", "hero_firebreak"))
                    }
                )
            }
            item {
                PresetChip(
                    title = "All 9 Specialists",
                    subtitle = "Deploy Full Division",
                    icon = Icons.Default.Groups,
                    tint = CyberGreen,
                    onClick = {
                        onSelectPreset(
                            listOf(
                                "hero_vanguard", "hero_cipher", "hero_sniper", "hero_medic",
                                "hero_samurai", "hero_nomad", "hero_spectre", "hero_reaper", "hero_firebreak"
                            )
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun PresetChip(
    title: String,
    subtitle: String,
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit
) {
    Surface(
        color = CyberSurface,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, tint.copy(alpha = 0.4f)),
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag("preset_formation_$title")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = title,
                    color = CyberOnSurface,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = subtitle,
                    color = CyberSubtext,
                    fontSize = 9.sp
                )
            }
        }
    }
}

/**
 * Strip of current assigned/deployed squad members
 */
@Composable
private fun AssignedSquadStrip(
    deployedHeroes: List<HeroEntity>,
    gearList: List<GearEntity>,
    onInspect: (HeroEntity) -> Unit,
    onUnassign: (HeroEntity) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ASSIGNED SQUAD OPERATIVES (${deployedHeroes.size})",
                color = CyberSubtext,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "TAP TO BENCH / INSPECT",
                color = CyberPrimary,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(deployedHeroes, key = { "strip_${it.id}" }) { hero ->
                val specialist = OiSpecialistRoster.getSpecialist(hero.id)
                val tint = specialist?.accentColor ?: CyberPrimary

                Surface(
                    color = CyberSurface,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, tint),
                    modifier = Modifier
                        .width(140.dp)
                        .clickable { onInspect(hero) }
                        .testTag("assigned_strip_item_${hero.id}")
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(tint.copy(alpha = 0.2f))
                                .border(1.dp, tint, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = specialist?.icon ?: Icons.Default.Person,
                                contentDescription = null,
                                tint = tint,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = specialist?.callsign ?: hero.name,
                            color = tint,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1
                        )

                        Text(
                            text = hero.heroClass.role,
                            color = CyberSubtext,
                            fontSize = 9.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Remove / Bench Button
                        Surface(
                            color = CyberRed.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, CyberRed.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onUnassign(hero) }
                        ) {
                            Text(
                                text = "BENCH",
                                color = CyberRed,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Filter row for squad categories
 */
@Composable
private fun OiSquadFilterRow(
    selectedFilter: SquadFilter,
    totalHeroes: Int,
    deployedCount: Int,
    reservesCount: Int,
    onSelectFilter: (SquadFilter) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(SquadFilter.values()) { filter ->
            val countLabel = when (filter) {
                SquadFilter.ALL -> " ($totalHeroes)"
                SquadFilter.DEPLOYED -> " ($deployedCount)"
                SquadFilter.RESERVES -> " ($reservesCount)"
                else -> ""
            }
            val isSelected = selectedFilter == filter

            FilterChip(
                selected = isSelected,
                onClick = { onSelectFilter(filter) },
                label = {
                    Text(
                        text = "${filter.label}$countLabel",
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                        fontSize = 11.sp
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CyberPrimary,
                    selectedLabelColor = Color.Black,
                    containerColor = CyberSurface,
                    labelColor = CyberSubtext
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = if (isSelected) CyberPrimary else CyberSurfaceVariant,
                    selectedBorderColor = CyberPrimary,
                    enabled = true,
                    selected = isSelected
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("squad_filter_${filter.name.lowercase()}")
            )
        }
    }
}

/**
 * Individual 'The Oi' Specialist Card
 */
@Composable
private fun OiSpecialistCard(
    hero: HeroEntity,
    gearList: List<GearEntity>,
    onToggleSquad: () -> Unit,
    onInspect: () -> Unit
) {
    val specialist = OiSpecialistRoster.getSpecialist(hero.id)
    val accentColor = specialist?.accentColor ?: CyberPrimary
    val callsign = specialist?.callsign ?: hero.name.substringBefore(" ").uppercase()
    val realName = specialist?.realName ?: hero.name
    val weapon = gearList.find { it.id == hero.equippedWeaponId }
    val armor = gearList.find { it.id == hero.equippedArmorId }
    val core = gearList.find { it.id == hero.equippedCoreId }

    val totalHp = hero.heroClass.baseHp + (hero.level - 1) * 20 + (armor?.hpBonus ?: 0) + (core?.hpBonus ?: 0)
    val totalAtk = hero.heroClass.baseAtk + (hero.level - 1) * 4 + (weapon?.attackBonus ?: 0) + (core?.attackBonus ?: 0)
    val totalDef = hero.heroClass.baseDef + (hero.level - 1) * 2 + (armor?.defenseBonus ?: 0)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.5.dp,
                if (hero.isInSquad) accentColor else CyberSurfaceVariant,
                RoundedCornerShape(16.dp)
            )
            .testTag("specialist_card_${hero.id}"),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Avatar, Call-sign, Real Name, Deployed Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.2f))
                            .border(2.dp, accentColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = specialist?.icon ?: Icons.Default.Person,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = callsign,
                                color = accentColor,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = accentColor.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = hero.heroClass.title,
                                    color = accentColor,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = realName,
                            color = CyberSubtext,
                            fontSize = 11.sp
                        )
                    }
                }

                // Level Badge
                Surface(
                    color = CyberSurfaceVariant,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "LVL ${hero.level}",
                        color = CyberPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Signature Weapon & DNI Ability row
            specialist?.let { spec ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = CyberSurfaceVariant,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.SportsEsports,
                                contentDescription = null,
                                tint = CyberSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Column {
                                Text("WEAPON", color = CyberSubtext, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                Text(spec.signatureWeapon, color = CyberOnSurface, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            }
                        }
                    }

                    Surface(
                        color = CyberSurfaceVariant,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = CyberTertiary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Column {
                                Text("DNI ABILITY", color = CyberSubtext, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                Text(spec.signatureAbility, color = CyberOnSurface, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quote snippet
            specialist?.let { spec ->
                Text(
                    text = spec.quote,
                    color = CyberSubtext.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    fontStyle = FontStyle.Italic,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Stat bars
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatPill(label = "HP", value = "$totalHp", tint = CyberGreen, modifier = Modifier.weight(1f))
                StatPill(label = "ATK", value = "$totalAtk", tint = CyberRed, modifier = Modifier.weight(1f))
                StatPill(label = "DEF", value = "$totalDef", tint = CyberPrimary, modifier = Modifier.weight(1f))
                StatPill(label = "RNG", value = "${hero.heroClass.baseRange}", tint = CyberYellow, modifier = Modifier.weight(1f))
                StatPill(label = "AP", value = "${hero.heroClass.baseAp}", tint = CyberTertiary, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Actions: Assign to Squad vs Reserves + Inspect
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onInspect,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, CyberPrimary.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberPrimary),
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("inspect_specialist_${hero.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "DOSSIER",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onToggleSquad,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hero.isInSquad) CyberGreen else CyberSecondary
                    ),
                    modifier = Modifier
                        .weight(1.5f)
                        .height(42.dp)
                        .testTag("toggle_squad_btn_${hero.id}")
                ) {
                    Icon(
                        imageVector = if (hero.isInSquad) Icons.Default.CheckCircle else Icons.Default.AddCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (hero.isInSquad) "ASSIGNED (DEPLOYED)" else "ASSIGN TO SQUAD",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun StatPill(
    label: String,
    value: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = CyberSurfaceVariant,
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 4.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                color = tint,
                fontSize = 8.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = value,
                color = CyberOnSurface,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Operative Dossier Lore Inspection Sheet
 */
@Composable
private fun OiSpecialistDossierDialog(
    hero: HeroEntity,
    gearList: List<GearEntity>,
    totalDeployed: Int,
    onDismiss: () -> Unit,
    onToggleSquad: () -> Unit,
    onNavigateToArmory: () -> Unit
) {
    val specialist = OiSpecialistRoster.getSpecialist(hero.id)
    val accentColor = specialist?.accentColor ?: CyberPrimary
    val callsign = specialist?.callsign ?: hero.name
    val realName = specialist?.realName ?: hero.name

    val weapon = gearList.find { it.id == hero.equippedWeaponId }
    val armor = gearList.find { it.id == hero.equippedArmorId }
    val core = gearList.find { it.id == hero.equippedCoreId }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        containerColor = CyberSurface,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.testTag("specialist_dossier_dialog"),
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                // Header with large badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(accentColor.copy(alpha = 0.2f))
                                .border(2.dp, accentColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = specialist?.icon ?: Icons.Default.Person,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "$callsign DOSSIER",
                                color = accentColor,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "$realName • ${hero.heroClass.role}",
                                color = CyberSubtext,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = CyberSubtext)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quote
                specialist?.let { spec ->
                    Surface(
                        color = CyberSurfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = spec.quote,
                            color = CyberOnSurface,
                            fontSize = 11.sp,
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Background Lore
                specialist?.let { spec ->
                    Text(
                        text = "OPERATIVE BACKGROUND",
                        color = CyberPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = spec.backgroundLore,
                        color = CyberOnSurface.copy(alpha = 0.85f),
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Signature Weapon & Ability details
                    Text(
                        text = "SIGNATURE WEAPON: ${spec.signatureWeapon}",
                        color = CyberSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = spec.weaponDescription,
                        color = CyberSubtext,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "DNI CYBER ABILITY: ${spec.signatureAbility}",
                        color = CyberTertiary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = spec.abilityDescription,
                        color = CyberSubtext,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Equipped Gear Summary & Armory Shortcut
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LOADOUT STATUS",
                        color = CyberSubtext,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(
                        onClick = onNavigateToArmory,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("OPEN ARMORY →", color = CyberPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    GearSlotSummary("Weapon", weapon?.name ?: "Basic Kinetic", weapon != null, Modifier.weight(1f))
                    GearSlotSummary("Armor", armor?.name ?: "Standard Weave", armor != null, Modifier.weight(1f))
                    GearSlotSummary("DNI Core", core?.name ?: "None", core != null, Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Action Button in Dialog
                Button(
                    onClick = onToggleSquad,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hero.isInSquad) CyberGreen else CyberSecondary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Icon(
                        imageVector = if (hero.isInSquad) Icons.Default.CheckCircle else Icons.Default.AddCircle,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (hero.isInSquad) "ASSIGNED TO CAMPAIGN (TAP TO BENCH)" else "ASSIGN THIS OPERATIVE TO CAMPAIGN",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    )
}

@Composable
private fun GearSlotSummary(
    slot: String,
    gearName: String,
    isEquipped: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        color = CyberSurfaceVariant,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, if (isEquipped) CyberPrimary.copy(alpha = 0.3f) else CyberSurfaceVariant),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(6.dp)) {
            Text(slot, color = CyberSubtext, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            Text(
                gearName,
                color = if (isEquipped) CyberPrimary else CyberSubtext,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Campaign Mission Selection Dialog
 */
@Composable
private fun OiCampaignSelectDialog(
    unlockedMissionId: Int,
    selectedMissionId: Int,
    squadPower: Int,
    onSelectMission: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        containerColor = CyberSurface,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.testTag("campaign_mission_select_dialog"),
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
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
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SELECT CAMPAIGN MISSION",
                            color = CyberPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = CyberSubtext)
                    }
                }

                Text(
                    text = "Assign 'The Oi' squad to any unlocked Black Ops operation.",
                    color = CyberSubtext,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.heightIn(max = 380.dp)
                ) {
                    items(CampaignData.missions) { mission ->
                        val isUnlocked = mission.id <= unlockedMissionId
                        val isSelected = mission.id == selectedMissionId
                        val isPowerEnough = squadPower >= mission.recommendedPower

                        Surface(
                            color = if (isSelected) CyberPrimary.copy(alpha = 0.15f) else CyberSurfaceVariant,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                1.5.dp,
                                if (isSelected) CyberPrimary else if (isUnlocked) CyberSurfaceVariant else Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = isUnlocked) {
                                    onSelectMission(mission.id)
                                }
                                .testTag("mission_select_item_${mission.id}")
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "MISSION 0${mission.id}",
                                            color = if (isUnlocked) CyberPrimary else CyberSubtext,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        if (mission.isBossRaid) {
                                            Surface(
                                                color = CyberRed.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "BOSS RAID",
                                                    color = CyberRed,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Black,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    if (!isUnlocked) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Lock, contentDescription = null, tint = CyberRed, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("LOCKED", color = CyberRed, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    } else if (isSelected) {
                                        Surface(
                                            color = CyberGreen.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "ASSIGNED TARGET",
                                                color = CyberGreen,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = mission.title,
                                    color = if (isUnlocked) CyberOnSurface else CyberSubtext,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = mission.subtitle,
                                    color = CyberSubtext,
                                    fontSize = 10.sp
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Req Power: ${mission.recommendedPower}",
                                        color = if (isPowerEnough) CyberGreen else CyberYellow,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "+${mission.rewardCredits} CR • ${mission.rewardGearName}",
                                        color = CyberTertiary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}
