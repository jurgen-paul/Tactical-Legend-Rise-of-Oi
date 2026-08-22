package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.aegis.AegisFpsEngine
import com.example.game.aegis.AegisSettings
import com.example.ui.theme.*
import com.example.util.SoundManager
import kotlinx.coroutines.delay
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AegisFpsScreen(
    onNavigateBack: () -> Unit,
    onRewardEarned: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val engine = remember { AegisFpsEngine() }

    var forwardInput by remember { mutableStateOf(0) }
    var strafeInput by remember { mutableStateOf(0) }
    var deltaAngleInput by remember { mutableStateOf(0.0) }
    var firingInput by remember { mutableStateOf(false) }

    var playerHealth by remember { mutableStateOf(engine.player.health) }
    var playerAlive by remember { mutableStateOf(engine.player.alive) }
    var score by remember { mutableStateOf(engine.score) }
    var enemiesAliveCount by remember { mutableStateOf(engine.enemies.count { it.alive }) }
    var muzzleFlash by remember { mutableStateOf(false) }
    var hitFeedback by remember { mutableStateOf(false) }

    var gameCompleted by remember { mutableStateOf(false) }
    var isVictory by remember { mutableStateOf(false) }

    // Game Simulation Loop
    LaunchedEffect(Unit) {
        var lastTime = System.nanoTime()
        while (true) {
            val now = System.nanoTime()
            val dt = ((now - lastTime) / 1_000_000_000.0).coerceIn(0.005, 0.05)
            lastTime = now

            if (playerAlive && !gameCompleted) {
                engine.update(
                    forward = forwardInput,
                    strafe = strafeInput,
                    dAngle = deltaAngleInput,
                    firing = firingInput,
                    dt = dt
                )
                deltaAngleInput = 0.0

                playerHealth = engine.player.health
                playerAlive = engine.player.alive
                score = engine.score
                enemiesAliveCount = engine.enemies.count { it.alive }

                if (firingInput && engine.weapon.cooldown > 0.25) {
                    muzzleFlash = true
                } else {
                    muzzleFlash = false
                }

                if (System.currentTimeMillis() - engine.lastHitTime < 250) {
                    hitFeedback = true
                } else {
                    hitFeedback = false
                }

                if (!playerAlive) {
                    gameCompleted = true
                    isVictory = false
                } else if (enemiesAliveCount == 0) {
                    gameCompleted = true
                    isVictory = true
                    onRewardEarned(350, 150)
                }
            }

            delay(16) // ~60fps
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("aegis_fps_screen")
    ) {
        // 3D Raycasting Canvas View
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            deltaAngleInput += (dragAmount.x * 0.005).toDouble()
                        }
                    )
                }
        ) {
            val w = size.width
            val h = size.height
            val halfH = h / 2f

            // 1. Ceiling
            drawRect(
                color = Color(25, 25, 30),
                topLeft = Offset.Zero,
                size = Size(w, halfH)
            )

            // 2. Floor
            drawRect(
                color = Color(50, 50, 50),
                topLeft = Offset(0f, halfH),
                size = Size(w, halfH)
            )

            // 3. Wall Slices
            val depths = engine.raycaster.cast(engine.player)
            val numRays = depths.size.coerceAtLeast(1)
            val scale = w / numRays.toFloat()
            val screenDist = (w / 2.0) / tan(Math.PI / 6.0)

            for (i in depths.indices) {
                val depth = depths[i].coerceAtLeast(0.0001)
                val wallHeight = (screenDist / depth).toFloat().coerceAtMost(h * 3f)
                val brightness = (255 - (depth * 12).toInt()).coerceIn(30, 255)

                val wallColor = Color(brightness, brightness, brightness)
                val x = i * scale

                drawRect(
                    color = wallColor,
                    topLeft = Offset(x, halfH - wallHeight / 2f),
                    size = Size(scale + 1f, wallHeight)
                )
            }

            // 4. Enemy Sprites (3D Billboards)
            for (enemy in engine.enemies) {
                if (!enemy.alive) continue
                val dx = enemy.x - engine.player.x
                val dy = enemy.y - engine.player.y
                val dist = hypot(dx, dy)
                var angleTo = atan2(dy, dx) - engine.player.angle
                angleTo = (angleTo + Math.PI).mod(2 * Math.PI) - Math.PI

                if (abs(angleTo) < 0.6 && dist > 0.2) {
                    val screenX = (w / 2.0 + tan(angleTo) * screenDist).toFloat()
                    val spriteSize = max(10f, (300.0 / dist).toFloat())

                    // Red Hostile Orb
                    drawCircle(
                        color = Color(200, 30, 30),
                        radius = spriteSize,
                        center = Offset(screenX, halfH)
                    )

                    // Inner cyber core
                    drawCircle(
                        color = Color(255, 100, 50),
                        radius = spriteSize * 0.45f,
                        center = Offset(screenX, halfH)
                    )

                    // Enemy Mini Health Bar
                    val healthRatio = (enemy.health / 100f).toFloat().coerceIn(0f, 1f)
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(screenX - spriteSize, halfH - spriteSize - 16f),
                        size = Size(spriteSize * 2, 8f)
                    )
                    drawRect(
                        color = if (healthRatio > 0.4f) Color(30, 200, 60) else Color.Red,
                        topLeft = Offset(screenX - spriteSize, halfH - spriteSize - 16f),
                        size = Size(spriteSize * 2 * healthRatio, 8f)
                    )
                }
            }

            // 5. Crosshair
            val crosshairColor = if (hitFeedback) Color.Red else Color.White
            val crosshairLen = if (hitFeedback) 22f else 16f
            drawLine(
                color = crosshairColor,
                start = Offset(w / 2f - crosshairLen, halfH),
                end = Offset(w / 2f + crosshairLen, halfH),
                strokeWidth = if (hitFeedback) 4f else 2.5f
            )
            drawLine(
                color = crosshairColor,
                start = Offset(w / 2f, halfH - crosshairLen),
                end = Offset(w / 2f, halfH + crosshairLen),
                strokeWidth = if (hitFeedback) 4f else 2.5f
            )

            // 6. Muzzle Flash Effect
            if (muzzleFlash) {
                drawCircle(
                    color = Color(255, 230, 100, 180),
                    radius = 45f,
                    center = Offset(w / 2f, halfH + 60f)
                )
            }
        }

        // Top HUD Overlay
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(40.dp)
                    .background(CyberSurface.copy(alpha = 0.8f), CircleShape)
                    .border(1.dp, CyberPrimary, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = CyberPrimary
                )
            }

            // Health & Stats Bar
            Surface(
                color = CyberSurface.copy(alpha = 0.85f),
                border = BorderStroke(1.dp, CyberPrimary.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "HEALTH",
                            color = CyberSubtext,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${playerHealth.toInt()}/100",
                            color = if (playerHealth < 30) CyberSecondary else CyberGreen,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Column {
                        Text(
                            text = "SCORE",
                            color = CyberSubtext,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$score",
                            color = CyberPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Column {
                        Text(
                            text = "HOSTILES",
                            color = CyberSubtext,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$enemiesAliveCount LEFT",
                            color = if (enemiesAliveCount > 0) CyberSecondary else CyberGreen,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // Minimap Radar
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.75f))
                    .border(1.5.dp, CyberPrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(60.dp)) {
                    val mapScale = size.width / 16f
                    val map = engine.mapGrid

                    // Draw walls on minimap
                    for (row in map.indices) {
                        for (col in map[row].indices) {
                            if (map[row][col] == '1') {
                                drawRect(
                                    color = Color.DarkGray,
                                    topLeft = Offset(col * mapScale, row * mapScale),
                                    size = Size(mapScale, mapScale)
                                )
                            }
                        }
                    }

                    // Draw Player Dot & Line of Sight
                    val px = (engine.player.x * mapScale).toFloat()
                    val py = (engine.player.y * mapScale).toFloat()
                    drawCircle(color = Color.Cyan, radius = 3f, center = Offset(px, py))
                    val sightX = px + (cos(engine.player.angle) * 12.0).toFloat()
                    val sightY = py + (sin(engine.player.angle) * 12.0).toFloat()
                    drawLine(
                        color = Color.Cyan.copy(alpha = 0.7f),
                        start = Offset(px, py),
                        end = Offset(sightX, sightY),
                        strokeWidth = 1.5f
                    )

                    // Draw Enemies
                    for (enemy in engine.enemies) {
                        if (!enemy.alive) continue
                        val ex = (enemy.x * mapScale).toFloat()
                        val ey = (enemy.y * mapScale).toFloat()
                        drawCircle(color = Color.Red, radius = 3f, center = Offset(ex, ey))
                    }
                }
            }
        }

        // Bottom On-Screen Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            // Movement D-Pad (Forward, Backward, Strafe)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Forward Button
                Button(
                    onClick = {},
                    modifier = Modifier
                        .size(54.dp)
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    forwardInput = if (event.changes.any { it.pressed }) 1 else 0
                                }
                            }
                        },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberSurface.copy(alpha = 0.85f)),
                    border = BorderStroke(1.dp, CyberPrimary)
                ) {
                    Icon(imageVector = Icons.Default.KeyboardArrowUp, contentDescription = "Forward", tint = CyberPrimary)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Strafe Left
                    Button(
                        onClick = {},
                        modifier = Modifier
                            .size(54.dp)
                            .pointerInput(Unit) {
                                awaitPointerEventScope {
                                    while (true) {
                                        val event = awaitPointerEvent()
                                        strafeInput = if (event.changes.any { it.pressed }) -1 else 0
                                    }
                                }
                            },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberSurface.copy(alpha = 0.85f)),
                        border = BorderStroke(1.dp, CyberPrimary)
                    ) {
                        Icon(imageVector = Icons.Default.KeyboardArrowLeft, contentDescription = "Strafe Left", tint = CyberPrimary)
                    }

                    // Backward
                    Button(
                        onClick = {},
                        modifier = Modifier
                            .size(54.dp)
                            .pointerInput(Unit) {
                                awaitPointerEventScope {
                                    while (true) {
                                        val event = awaitPointerEvent()
                                        forwardInput = if (event.changes.any { it.pressed }) -1 else 0
                                    }
                                }
                            },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberSurface.copy(alpha = 0.85f)),
                        border = BorderStroke(1.dp, CyberPrimary)
                    ) {
                        Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = "Backward", tint = CyberPrimary)
                    }

                    // Strafe Right
                    Button(
                        onClick = {},
                        modifier = Modifier
                            .size(54.dp)
                            .pointerInput(Unit) {
                                awaitPointerEventScope {
                                    while (true) {
                                        val event = awaitPointerEvent()
                                        strafeInput = if (event.changes.any { it.pressed }) 1 else 0
                                    }
                                }
                            },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberSurface.copy(alpha = 0.85f)),
                        border = BorderStroke(1.dp, CyberPrimary)
                    ) {
                        Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = "Strafe Right", tint = CyberPrimary)
                    }
                }
            }

            // Quick Turn and Fire Controls
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Quick Turn Left / Right Row
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { deltaAngleInput -= 0.35 },
                        shape = CircleShape,
                        modifier = Modifier.size(46.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberSurface.copy(alpha = 0.8f)),
                        border = BorderStroke(1.dp, CyberSecondary)
                    ) {
                        Icon(imageVector = Icons.Default.RotateLeft, contentDescription = "Turn Left", tint = CyberSecondary)
                    }

                    Button(
                        onClick = { deltaAngleInput += 0.35 },
                        shape = CircleShape,
                        modifier = Modifier.size(46.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberSurface.copy(alpha = 0.8f)),
                        border = BorderStroke(1.dp, CyberSecondary)
                    ) {
                        Icon(imageVector = Icons.Default.RotateRight, contentDescription = "Turn Right", tint = CyberSecondary)
                    }
                }

                // Primary Fire Trigger Button
                Button(
                    onClick = {
                        SoundManager.playAttackSound()
                    },
                    modifier = Modifier
                        .size(80.dp)
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val isPressed = event.changes.any { it.pressed }
                                    firingInput = isPressed
                                    if (isPressed && engine.weapon.canFire()) {
                                        SoundManager.playAttackSound()
                                    }
                                }
                            }
                        }
                        .testTag("aegis_fire_button"),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (firingInput) CyberSecondary else CyberSecondary.copy(alpha = 0.8f)
                    ),
                    border = BorderStroke(2.dp, CyberTertiary)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Fire Weapon",
                        tint = Color.White,
                        modifier = Modifier.size(38.dp)
                    )
                }
            }
        }

        // Game Over / Victory Modal
        if (gameCompleted) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = CyberSurface,
                    border = BorderStroke(2.dp, if (isVictory) CyberGreen else CyberSecondary)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = if (isVictory) Icons.Default.EmojiEvents else Icons.Default.Dangerous,
                            contentDescription = null,
                            tint = if (isVictory) CyberGreen else CyberSecondary,
                            modifier = Modifier.size(56.dp)
                        )

                        Text(
                            text = if (isVictory) "SECTOR CLEARED!" else "MISSION FAILED - KIA",
                            color = if (isVictory) CyberGreen else CyberSecondary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )

                        Text(
                            text = if (isVictory)
                                "All hostile drones neutralized! Score: $score\n+350 Cryptokeys | +150 DNI Data"
                            else
                                "Operative down in the combat zone. Final Score: $score",
                            color = CyberOnSurface,
                            fontSize = 14.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = onNavigateBack,
                                modifier = Modifier.weight(1f),
                                border = BorderStroke(1.dp, CyberPrimary)
                            ) {
                                Text("Exit", color = CyberPrimary)
                            }

                            Button(
                                onClick = {
                                    engine.reset()
                                    playerHealth = engine.player.health
                                    playerAlive = engine.player.alive
                                    score = 0
                                    enemiesAliveCount = 3
                                    gameCompleted = false
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary)
                            ) {
                                Text("Restart", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
