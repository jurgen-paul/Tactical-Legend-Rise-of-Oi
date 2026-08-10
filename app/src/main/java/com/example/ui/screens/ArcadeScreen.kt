package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.PlayerProfileEntity
import com.example.ui.theme.*
import com.example.util.SoundManager
import kotlinx.coroutines.delay
import kotlin.random.Random

enum class ArcadeGameState {
    MENU, PLAYING, GAME_OVER
}

enum class NodeType {
    ROGUE_DRONE, EMP_POWERUP, SHIELD_BOOST, OVERDRIVE_CORE
}

data class ArcadeNode(
    val id: Long,
    val xRatio: Float, // 0.1f to 0.9f
    val yRatio: Float, // 0.1f to 0.85f
    val type: NodeType,
    val hp: Int = 1,
    val radiusDp: Float = 28f
)

data class ArcadeScoreRecord(
    val score: Int,
    val combo: Int,
    val creditsEarned: Int,
    val date: String
)

@Composable
fun ArcadeScreen(
    profile: PlayerProfileEntity?,
    onClaimRewards: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var gameState by remember { mutableStateOf(ArcadeGameState.MENU) }
    var currentScore by remember { mutableStateOf(0) }
    var highScore by remember { mutableStateOf(12400) }
    var combo by remember { mutableStateOf(0) }
    var maxCombo by remember { mutableStateOf(0) }
    var firewallHp by remember { mutableStateOf(100f) }
    var nodesDestroyed by remember { mutableStateOf(0) }
    var timeLeftSeconds by remember { mutableStateOf(45) }
    var difficultyMultiplier by remember { mutableStateOf(1.0f) }

    var activeNodes by remember { mutableStateOf<List<ArcadeNode>>(emptyList()) }
    var highScoresList by remember {
        mutableStateOf(
            listOf(
                ArcadeScoreRecord(12400, 24, 450, "Top Operative"),
                ArcadeScoreRecord(8900, 18, 300, "Jax Vanguard"),
                ArcadeScoreRecord(6200, 12, 200, "Cipher Zero")
            )
        )
    }

    var lastLaserPulse by remember { mutableStateOf<Pair<Offset, Offset>?>(null) }

    // Arcade Game Timer & Spawners
    LaunchedEffect(gameState) {
        if (gameState == ArcadeGameState.PLAYING) {
            currentScore = 0
            combo = 0
            maxCombo = 0
            firewallHp = 100f
            nodesDestroyed = 0
            timeLeftSeconds = 45
            activeNodes = emptyList()

            var nodeCounter = 0L

            while (gameState == ArcadeGameState.PLAYING && timeLeftSeconds > 0 && firewallHp > 0f) {
                delay(1000L)
                timeLeftSeconds -= 1

                // Randomly spawn nodes
                if (activeNodes.size < 6) {
                    val roll = Random.nextFloat()
                    val nodeType = when {
                        roll > 0.88f -> NodeType.EMP_POWERUP
                        roll > 0.78f -> NodeType.SHIELD_BOOST
                        roll > 0.68f -> NodeType.OVERDRIVE_CORE
                        else -> NodeType.ROGUE_DRONE
                    }
                    nodeCounter++
                    val newNode = ArcadeNode(
                        id = nodeCounter,
                        xRatio = Random.nextFloat() * 0.75f + 0.12f,
                        yRatio = Random.nextFloat() * 0.6f + 0.15f,
                        type = nodeType
                    )
                    activeNodes = activeNodes + newNode
                }
            }

            if (timeLeftSeconds <= 0 || firewallHp <= 0f) {
                gameState = ArcadeGameState.GAME_OVER
                if (currentScore > highScore) {
                    highScore = currentScore
                }
                val earnedCredits = (currentScore * 0.05f).toInt()
                val earnedData = (currentScore * 0.02f).toInt()
                highScoresList = (listOf(
                    ArcadeScoreRecord(currentScore, maxCombo, earnedCredits, "Commander Oi")
                ) + highScoresList).take(5)
            }
        }
    }

    // Node tick animation move down
    LaunchedEffect(gameState, activeNodes) {
        if (gameState == ArcadeGameState.PLAYING) {
            delay(400L)
            activeNodes = activeNodes.mapNotNull { node ->
                val newY = node.yRatio + 0.035f * difficultyMultiplier
                if (newY >= 0.85f && node.type == NodeType.ROGUE_DRONE) {
                    // Reached firewall, take damage!
                    firewallHp = (firewallHp - 12f).coerceAtLeast(0f)
                    combo = 0
                    null
                } else if (newY >= 0.85f) {
                    null // Despawn powerup
                } else {
                    node.copy(yRatio = newY)
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground)
            .testTag("arcade_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Arcade Header Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.5.dp, CyberPrimary)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CyberPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SportsEsports,
                                contentDescription = null,
                                tint = CyberPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "CYBER ARCADE SIMULATOR",
                                color = CyberPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "HACK STRIKE • HIGH SCORE REWARDS",
                                color = CyberSubtext,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "HIGH: $highScore",
                            color = CyberTertiary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "CR: ${profile?.credits ?: 0}",
                            color = CyberGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (gameState) {
                ArcadeGameState.MENU -> {
                    ArcadeMenuTab(
                        highScore = highScore,
                        highScoresList = highScoresList,
                        onStartGame = { diff ->
                            difficultyMultiplier = diff
                            gameState = ArcadeGameState.PLAYING
                        }
                    )
                }

                ArcadeGameState.PLAYING -> {
                    // HUD Bar
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("SCORE", color = CyberSubtext, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text("$currentScore", color = CyberPrimary, fontSize = 20.sp, fontWeight = FontWeight.Black)
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("COMBO", color = CyberSubtext, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text("${combo}x", color = CyberTertiary, fontSize = 18.sp, fontWeight = FontWeight.Black)
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("TIME LEFT", color = CyberSubtext, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text("${timeLeftSeconds}s", color = if (timeLeftSeconds <= 10) CyberSecondary else CyberOnSurface, fontSize = 18.sp, fontWeight = FontWeight.Black)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("FIREWALL HP", color = CyberSubtext, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = CyberGreen,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("${firewallHp.toInt()}%", color = CyberGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Interactive Arcade Matrix Canvas
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .border(2.dp, CyberPrimary, RoundedCornerShape(16.dp))
                            .testTag("arcade_game_field"),
                        colors = CardDefaults.cardColors(containerColor = CyberSurface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                            val wPx = constraints.maxWidth.toFloat()
                            val hPx = constraints.maxHeight.toFloat()

                            // Grid background Canvas
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                // Draw Scanlines
                                val stepY = 24.dp.toPx()
                                var currentY = 0f
                                while (currentY < size.height) {
                                    drawLine(
                                        color = CyberBorder.copy(alpha = 0.3f),
                                        start = Offset(0f, currentY),
                                        end = Offset(size.width, currentY),
                                        strokeWidth = 1f
                                    )
                                    currentY += stepY
                                }

                                val stepX = 40.dp.toPx()
                                var currentX = 0f
                                while (currentX < size.width) {
                                    drawLine(
                                        color = CyberBorder.copy(alpha = 0.3f),
                                        start = Offset(currentX, 0f),
                                        end = Offset(currentX, size.height),
                                        strokeWidth = 1f
                                    )
                                    currentX += stepX
                                }

                                // Firewall baseline
                                val baselineY = size.height * 0.85f
                                drawLine(
                                    color = CyberSecondary,
                                    start = Offset(0f, baselineY),
                                    end = Offset(size.width, baselineY),
                                    strokeWidth = 4f
                                )
                            }

                            // Active Nodes Overlay
                            activeNodes.forEach { node ->
                                val posX = wPx * node.xRatio
                                val posY = hPx * node.yRatio

                                Box(
                                    modifier = Modifier
                                        .offset(
                                            x = (node.xRatio * (maxWidth.value - 60)).dp,
                                            y = (node.yRatio * (maxHeight.value - 60)).dp
                                        )
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (node.type) {
                                                NodeType.ROGUE_DRONE -> CyberSecondary.copy(alpha = 0.25f)
                                                NodeType.EMP_POWERUP -> CyberPrimary.copy(alpha = 0.25f)
                                                NodeType.SHIELD_BOOST -> CyberGreen.copy(alpha = 0.25f)
                                                NodeType.OVERDRIVE_CORE -> CyberTertiary.copy(alpha = 0.25f)
                                            }
                                        )
                                        .border(
                                            width = 2.dp,
                                            color = when (node.type) {
                                                NodeType.ROGUE_DRONE -> CyberSecondary
                                                NodeType.EMP_POWERUP -> CyberPrimary
                                                NodeType.SHIELD_BOOST -> CyberGreen
                                                NodeType.OVERDRIVE_CORE -> CyberTertiary
                                            },
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            // Handle Node Click
                                            activeNodes = activeNodes.filter { it.id != node.id }
                                            when (node.type) {
                                                NodeType.ROGUE_DRONE -> {
                                                    SoundManager.playAttackSound()
                                                    combo++
                                                    if (combo > maxCombo) maxCombo = combo
                                                    val pts = (100 * (1 + combo * 0.2f)).toInt()
                                                    currentScore += pts
                                                    nodesDestroyed++
                                                }

                                                NodeType.EMP_POWERUP -> {
                                                    SoundManager.playAbilitySound()
                                                    // Clear all rogue nodes
                                                    val cleared =
                                                        activeNodes.count { it.type == NodeType.ROGUE_DRONE }
                                                    currentScore += cleared * 150
                                                    nodesDestroyed += cleared
                                                    activeNodes =
                                                        activeNodes.filter { it.type != NodeType.ROGUE_DRONE }
                                                    combo += 2
                                                }

                                                NodeType.SHIELD_BOOST -> {
                                                    SoundManager.playShieldHealSound()
                                                    firewallHp =
                                                        (firewallHp + 20f).coerceAtMost(100f)
                                                    currentScore += 50
                                                }

                                                NodeType.OVERDRIVE_CORE -> {
                                                    SoundManager.playVictorySound()
                                                    currentScore += 500
                                                    combo += 3
                                                }
                                            }
                                        }
                                        .testTag("arcade_node_${node.id}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (node.type) {
                                            NodeType.ROGUE_DRONE -> Icons.Default.SmartToy
                                            NodeType.EMP_POWERUP -> Icons.Default.FlashOn
                                            NodeType.SHIELD_BOOST -> Icons.Default.Shield
                                            NodeType.OVERDRIVE_CORE -> Icons.Default.Star
                                        },
                                        contentDescription = null,
                                        tint = when (node.type) {
                                            NodeType.ROGUE_DRONE -> CyberSecondary
                                            NodeType.EMP_POWERUP -> CyberPrimary
                                            NodeType.SHIELD_BOOST -> CyberGreen
                                            NodeType.OVERDRIVE_CORE -> CyberTertiary
                                        },
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                ArcadeGameState.GAME_OVER -> {
                    val creditsEarned = (currentScore * 0.05f).toInt()
                    val dataEarned = (currentScore * 0.02f).toInt()

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(2.dp, CyberPrimary, RoundedCornerShape(16.dp))
                            .testTag("arcade_game_over_card"),
                        colors = CardDefaults.cardColors(containerColor = CyberSurface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.MilitaryTech,
                                contentDescription = null,
                                tint = CyberTertiary,
                                modifier = Modifier.size(56.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "ARCADE SESSION COMPLETED",
                                color = CyberPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black
                            )

                            Text(
                                text = "Matrix hack sync metrics uploaded to squad profile.",
                                color = CyberSubtext,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )

                            Divider(color = CyberBorder, modifier = Modifier.padding(vertical = 16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                ScoreStatBox("FINAL SCORE", "$currentScore", CyberPrimary)
                                ScoreStatBox("MAX COMBO", "${maxCombo}x", CyberTertiary)
                                ScoreStatBox("NODES CLEARED", "$nodesDestroyed", CyberGreen)
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = CyberSurfaceVariant,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "EARNED SQUAD REWARDS",
                                        color = CyberTertiary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = CyberTertiary, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("+$creditsEarned CREDITS", color = CyberOnSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Memory, contentDescription = null, tint = CyberPrimary, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("+$dataEarned TACTICAL DATA", color = CyberOnSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    onClaimRewards(creditsEarned, dataEarned)
                                    gameState = ArcadeGameState.MENU
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("arcade_claim_rewards_button")
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("CLAIM REWARDS & RETURN TO MENU", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScoreStatBox(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = color, fontSize = 18.sp, fontWeight = FontWeight.Black)
        Text(text = label, color = CyberSubtext, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ArcadeMenuTab(
    highScore: Int,
    highScoresList: List<ArcadeScoreRecord>,
    onStartGame: (Float) -> Unit
) {
    var selectedDifficulty by remember { mutableStateOf(1.0f) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, CyberBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "SIMULATOR OBJECTIVE",
                        color = CyberPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Tap incoming Rogue Drones before they breach your Firewall line. Collect EMP pulses and Shield Overdrives to maximize combo scores and earn Cyber Credits!",
                        color = CyberSubtext,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "SELECT SIMULATION INTENSITY:",
                        color = CyberSubtext,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedDifficulty == 1.0f,
                            onClick = { selectedDifficulty = 1.0f },
                            label = { Text("STANDARD (1.0x)", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.testTag("arcade_diff_standard")
                        )

                        FilterChip(
                            selected = selectedDifficulty == 1.4f,
                            onClick = { selectedDifficulty = 1.4f },
                            label = { Text("OVERDRIVE (1.4x)", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.testTag("arcade_diff_overdrive")
                        )

                        FilterChip(
                            selected = selectedDifficulty == 1.8f,
                            onClick = { selectedDifficulty = 1.8f },
                            label = { Text("FRENZY (1.8x)", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.testTag("arcade_diff_frenzy")
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = { onStartGame(selectedDifficulty) },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("start_arcade_game_button")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("START ARCADE SESSION", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, CyberBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "LEADERBOARD & HIGH SCORES",
                        color = CyberTertiary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    highScoresList.forEachIndexed { index, record ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "#${index + 1}",
                                    color = if (index == 0) CyberTertiary else CyberSubtext,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(record.date, color = CyberOnSurface, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("Max Combo: ${record.combo}x", color = CyberSubtext, fontSize = 10.sp)
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${record.score} PTS", color = CyberPrimary, fontWeight = FontWeight.Black, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = CyberSurfaceVariant,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "+${record.creditsEarned} CR",
                                        color = CyberGreen,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        if (index < highScoresList.size - 1) {
                            Divider(color = CyberBorder, modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }
            }
        }
    }
}
