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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.game.BattlePhase
import com.example.game.BattleUnit
import com.example.game.TacticalBattleState
import com.example.ui.theme.*
import com.example.ui.viewmodel.BattleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TacticalBattleScreen(
    viewModel: BattleViewModel,
    onNavigateBack: () -> Unit
) {
    val battleState by viewModel.battleState.collectAsState()
    var showLogsDrawer by remember { mutableStateOf(false) }

    val state = battleState ?: return

    val selectedUnit = state.units.find { it.id == state.selectedUnitId }
    val isPlayerTurn = state.phase == BattlePhase.PLAYER_TURN

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = state.mission.title.uppercase(),
                            color = CyberPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "TURN ${state.currentTurn} • ${if (isPlayerTurn) "SQUAD TURN" else "HOSTILE TURN"}",
                            color = if (isPlayerTurn) CyberGreen else CyberSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("battle_back_button")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = CyberOnSurface)
                    }
                },
                actions = {
                    IconButton(onClick = { showLogsDrawer = !showLogsDrawer }) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = "Logs", tint = CyberTertiary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CyberSurface)
            )
        },
        containerColor = CyberBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Tactical Status Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CyberSurfaceVariant)
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val playerAliveCount = state.units.count { it.isAlive && it.team == UnitTeam.PLAYER_OI }
                    val enemyAliveCount = state.units.count { it.isAlive && it.team != UnitTeam.PLAYER_OI }

                    Text(
                        text = "OI SQUAD: $playerAliveCount ACTIVE",
                        color = CyberPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )

                    Surface(
                        color = if (state.controlPointOwner == UnitTeam.PLAYER_OI) CyberGreen.copy(alpha = 0.2f) else CyberSurface,
                        border = BorderStroke(1.dp, CyberTertiary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "CP TERMINAL: ${state.controlPointTurnsHeld}/2 TURNS",
                            color = CyberTertiary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = "HOSTILES: $enemyAliveCount ALIVE",
                        color = CyberSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                // TURN INDICATOR BANNER (The Oi Squad vs Enemy AI)
                TurnIndicatorHeader(
                    phase = state.phase,
                    currentTurn = state.currentTurn
                )

                // Center 2D Grid Battlefield
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    TacticalGridBoard(
                        state = state,
                        viewModel = viewModel,
                        selectedUnit = selectedUnit
                    )
                }

                // Bottom Active Unit HUD & Action Bar
                if (selectedUnit != null && selectedUnit.isAlive && selectedUnit.team == UnitTeam.PLAYER_OI) {
                    ActiveUnitActionBar(
                        unit = selectedUnit,
                        activeAbility = state.activeAbility,
                        isPlayerTurn = isPlayerTurn,
                        onSelectAbility = { viewModel.selectAbility(it) },
                        onEndTurn = { viewModel.endTurn() }
                    )
                } else {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CyberSurface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isPlayerTurn) "SELECT A SQUAD UNIT ON THE GRID" else "HOSTILE UNITS EXECUTING TURN...",
                                color = CyberSubtext,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )

                            if (isPlayerTurn) {
                                Button(
                                    onClick = { viewModel.endTurn() },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberSecondary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("end_turn_button")
                                ) {
                                    Text("END TURN", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Victory or Defeat Overlay Modal
            if (state.phase == BattlePhase.VICTORY || state.phase == BattlePhase.DEFEAT) {
                BattleOutcomeModal(
                    isVictory = state.phase == BattlePhase.VICTORY,
                    mission = state.mission,
                    onClaimRewards = {
                        if (state.phase == BattlePhase.VICTORY) {
                            viewModel.claimRewards { onNavigateBack() }
                        } else {
                            onNavigateBack()
                        }
                    }
                )
            }

            // Battle Log Drawer Sheet
            if (showLogsDrawer) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .align(Alignment.BottomCenter),
                    color = CyberSurface,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    tonalElevation = 8.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("TACTICAL LOG AUDIT", color = CyberPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            IconButton(onClick = { showLogsDrawer = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = CyberOnSurface)
                            }
                        }

                        Divider(color = CyberBorder, modifier = Modifier.padding(vertical = 8.dp))

                        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(state.logs) { log ->
                                LogRow(log = log)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TacticalGridBoard(
    state: TacticalBattleState,
    viewModel: BattleViewModel,
    selectedUnit: BattleUnit?
) {
    val validMoves = if (selectedUnit != null && selectedUnit.team == UnitTeam.PLAYER_OI) {
        viewModel.getValidMovePositions(selectedUnit)
    } else emptyList()

    val width = state.width
    val height = state.height

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // Top X-Axis Coordinates Header
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 2.dp)
        ) {
            // Corner spacer for Y-axis column
            Box(modifier = Modifier.size(20.dp))

            for (x in 0 until width) {
                Box(
                    modifier = Modifier.size(width = 46.dp, height = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "X$x",
                        color = CyberPrimary.copy(alpha = 0.8f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        for (y in 0 until height) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Y-Axis Coordinate Label
                Box(
                    modifier = Modifier.size(width = 20.dp, height = 46.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Y$y",
                        color = CyberSecondary.copy(alpha = 0.8f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                for (x in 0 until width) {
                    val pos = Position(x, y)
                    val terrain = state.terrainMap[pos] ?: TerrainType.PLAIN
                    val unitOnTile = state.units.find { it.isAlive && it.position == pos }
                    val isValidMove = pos in validMoves
                    val isSelectedUnitTile = selectedUnit?.position == pos
                    val apCost = if (isValidMove && selectedUnit != null) {
                        viewModel.getMoveApCost(selectedUnit.position, pos)
                    } else null

                    GridTileView(
                        pos = pos,
                        terrain = terrain,
                        unit = unitOnTile,
                        isSelected = isSelectedUnitTile,
                        isValidMove = isValidMove,
                        moveApCost = apCost,
                        onClick = {
                            if (unitOnTile != null) {
                                if (selectedUnit != null && selectedUnit.team == UnitTeam.PLAYER_OI && unitOnTile.team != UnitTeam.PLAYER_OI) {
                                    val ability = state.activeAbility
                                    if (ability != null) {
                                        viewModel.executeAbility(selectedUnit.id, ability, pos)
                                    } else {
                                        viewModel.performAttack(selectedUnit.id, unitOnTile.id)
                                    }
                                } else {
                                    viewModel.selectUnit(unitOnTile.id)
                                }
                            } else if (isValidMove && selectedUnit != null) {
                                viewModel.moveUnit(selectedUnit.id, pos)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun GridTileView(
    pos: Position,
    terrain: TerrainType,
    unit: BattleUnit?,
    isSelected: Boolean,
    isValidMove: Boolean,
    moveApCost: Int?,
    onClick: () -> Unit
) {
    val tileBg = when {
        isSelected -> CyberPrimary.copy(alpha = 0.35f)
        isValidMove -> CyberGreen.copy(alpha = 0.25f)
        terrain == TerrainType.CONTROL_POINT -> CyberTertiary.copy(alpha = 0.25f)
        terrain == TerrainType.COVER -> Color(0xFF1E293B)
        terrain == TerrainType.ELEVATED -> Color(0xFF334155)
        terrain == TerrainType.HAZARD -> Color(0xFF450A0A)
        else -> CyberSurface
    }

    val tileBorder = when {
        isSelected -> CyberPrimary
        isValidMove -> CyberGreen
        terrain == TerrainType.CONTROL_POINT -> CyberTertiary
        unit?.team == UnitTeam.PLAYER_OI -> CyberPrimary
        unit?.team != null -> CyberSecondary
        else -> CyberBorder
    }

    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(tileBg)
            .border(if (isSelected || isValidMove) 1.5.dp else 1.dp, tileBorder, RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .testTag("tile_${pos.x}_${pos.y}"),
        contentAlignment = Alignment.Center
    ) {
        // Small 2D Coordinate Badge in top-left
        Text(
            text = "${pos.x},${pos.y}",
            color = CyberSubtext.copy(alpha = 0.4f),
            fontSize = 7.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(2.dp)
        )

        // Terrain background indicator
        if (terrain == TerrainType.CONTROL_POINT) {
            Icon(
                imageVector = Icons.Default.Terminal,
                contentDescription = "Terminal",
                tint = CyberTertiary.copy(alpha = 0.5f),
                modifier = Modifier.size(22.dp)
            )
        } else if (terrain == TerrainType.COVER) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = "Cover",
                tint = Color.Gray.copy(alpha = 0.3f),
                modifier = Modifier.size(18.dp)
            )
        }

        // Movement AP Cost Badge on Valid Targets
        if (isValidMove && moveApCost != null && unit == null) {
            Surface(
                color = CyberGreen,
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(2.dp)
            ) {
                Text(
                    text = "-${moveApCost}AP",
                    color = Color.Black,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                )
            }
        }

        // Unit Token Rendering
        if (unit != null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Class Icon / Token Badge
                Surface(
                    shape = CircleShape,
                    color = if (unit.team == UnitTeam.PLAYER_OI) CyberPrimary else CyberSecondary,
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = when (unit.heroClass) {
                                HeroClass.VANGUARD -> "V"
                                HeroClass.SNIPER -> "S"
                                HeroClass.CIPHER -> "C"
                                HeroClass.MEDIC -> "M"
                                HeroClass.SAMURAI -> "B"
                                null -> if (unit.isBoss) "★" else "E"
                            },
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // HP Bar
                val hpRatio = unit.currentHp.toFloat() / unit.maxHp.toFloat()
                LinearProgressIndicator(
                    progress = { hpRatio },
                    modifier = Modifier
                        .width(36.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = if (unit.team == UnitTeam.PLAYER_OI) CyberGreen else CyberSecondary,
                    trackColor = Color.Black
                )
            }
        }
    }
}

@Composable
fun ActiveUnitActionBar(
    unit: BattleUnit,
    activeAbility: TacticalAbility?,
    isPlayerTurn: Boolean,
    onSelectAbility: (TacticalAbility?) -> Unit,
    onEndTurn: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Unit Stat Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = unit.name, color = CyberOnSurface, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(
                        text = "${unit.heroClass?.role ?: "Tactical Unit"} • POS (${unit.position.x}, ${unit.position.y}) • MOB ${unit.mobility}",
                        color = CyberSubtext,
                        fontSize = 11.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = CyberSurfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "AP ${unit.currentAp}/${unit.maxAp}",
                            color = CyberPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    if (unit.shieldAmount > 0) {
                        Surface(
                            color = CyberTertiary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "SHIELD +${unit.shieldAmount}",
                                color = CyberTertiary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons Row (Basic Attack, Active Skills)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Basic Attack Button
                Button(
                    onClick = { onSelectAbility(null) },
                    enabled = isPlayerTurn && unit.currentAp >= 2,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeAbility == null) CyberPrimary else CyberSurfaceVariant
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("basic_attack_button"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("ATTACK (2 AP)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                // Active Skill Buttons
                unit.abilities.forEach { ability ->
                    val isSelected = activeAbility?.id == ability.id
                    Button(
                        onClick = {
                            if (isSelected) onSelectAbility(null) else onSelectAbility(ability)
                        },
                        enabled = isPlayerTurn && unit.currentAp >= ability.apCost,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) CyberSecondary else CyberSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("${ability.name.take(8)} (${ability.apCost} AP)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = onEndTurn,
                    enabled = isPlayerTurn,
                    colors = ButtonDefaults.buttonColors(containerColor = CyberSecondary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("unit_end_turn_button")
                ) {
                    Text("END", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun BattleOutcomeModal(
    isVictory: Boolean,
    mission: CampaignMission,
    onClaimRewards: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberOverlay),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .width(320.dp)
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(2.dp, if (isVictory) CyberGreen else CyberSecondary)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = if (isVictory) Icons.Default.EmojiEvents else Icons.Default.Cancel,
                    contentDescription = null,
                    tint = if (isVictory) CyberGreen else CyberSecondary,
                    modifier = Modifier.size(56.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (isVictory) "VICTORY ACHIEVED!" else "SQUAD NEUTRALIZED",
                    color = if (isVictory) CyberGreen else CyberSecondary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = if (isVictory) "The Oi squad successfully cleared ${mission.title}!" else "Retreat and upgrade squad gear to retry.",
                    color = CyberSubtext,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                if (isVictory) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("REWARDS EARNED:", color = CyberTertiary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text("• +${mission.rewardCredits} Cyber Credits", color = CyberOnSurface, fontSize = 12.sp)
                            Text("• +${mission.rewardData} Tactical Data", color = CyberOnSurface, fontSize = 12.sp)
                            Text("• Gear: ${mission.rewardGearName} [${mission.rewardGearRarity.label}]", color = CyberPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onClaimRewards,
                    colors = ButtonDefaults.buttonColors(containerColor = if (isVictory) CyberPrimary else CyberSecondary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("claim_rewards_button"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isVictory) "CLAIM REWARDS & RETURN" else "RETURN TO WAR ROOM",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun TurnIndicatorHeader(
    phase: BattlePhase,
    currentTurn: Int
) {
    val isPlayerTurn = phase == BattlePhase.PLAYER_TURN
    val bannerBg = if (isPlayerTurn) CyberPrimary.copy(alpha = 0.15f) else CyberSecondary.copy(alpha = 0.20f)
    val borderColor = if (isPlayerTurn) CyberPrimary else CyberSecondary
    val mainColor = if (isPlayerTurn) CyberPrimary else CyberSecondary
    val icon = if (isPlayerTurn) Icons.Default.Shield else Icons.Default.Warning
    val titleText = if (isPlayerTurn) "THE OI SQUAD TURN" else "ENEMY AI TURN"
    val subtitleText = if (isPlayerTurn) "COMMAND ACTIVE OPERATIVES" else "HOSTILE ENFORCE EXECUTION"

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag("turn_indicator_header"),
        color = bannerBg,
        border = BorderStroke(1.5.dp, borderColor),
        shape = RoundedCornerShape(8.dp)
    ) {
        AnimatedContent(
            targetState = isPlayerTurn,
            transitionSpec = {
                fadeIn() + slideInVertically { height -> -height } togetherWith fadeOut() + slideOutVertically { height -> height }
            },
            label = "TurnTransition"
        ) { targetIsPlayer ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = mainColor,
                        shape = CircleShape,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = "Turn Icon",
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = titleText,
                                color = mainColor,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                letterSpacing = 0.5.sp
                            )
                            Surface(
                                color = mainColor.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = if (targetIsPlayer) "ACTIVE" else "EXECUTING...",
                                    color = mainColor,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = subtitleText,
                            color = CyberOnSurface.copy(alpha = 0.8f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Surface(
                    color = CyberSurface,
                    border = BorderStroke(1.dp, mainColor.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "TURN $currentTurn",
                        color = mainColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun LogRow(log: BattleLog) {
    val color = when (log.logType) {
        LogType.CRITICAL -> CyberSecondary
        LogType.SKILL -> CyberPrimary
        LogType.HEAL -> CyberGreen
        LogType.DEFEAT -> CyberSecondary
        LogType.SYSTEM -> CyberTertiary
        else -> CyberSubtext
    }

    Row(modifier = Modifier.fillMaxWidth()) {
        Text(text = "[T${log.turn}] ", color = CyberSubtext, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Text(text = log.message, color = color, fontSize = 11.sp)
    }
}
