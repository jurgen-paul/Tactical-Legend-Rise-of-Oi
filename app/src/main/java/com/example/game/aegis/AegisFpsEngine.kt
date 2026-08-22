package com.example.game.aegis

import kotlin.math.*

/**
 * AEGIS FPS ENGINE - Kotlin Core Engine
 * Exact 1-to-1 port of engine.py and settings.py for the 3D Raycasting FPS Engine.
 */
object AegisSettings {
    const val WIDTH = 1024
    const val HEIGHT = 640
    const val HALF_WIDTH = WIDTH / 2
    const val HALF_HEIGHT = HEIGHT / 2
    const val FPS = 60

    val FOV = Math.PI / 3.0 // 60-degree field of view
    val HALF_FOV = FOV / 2.0
    const val NUM_RAYS = 256
    val DELTA_ANGLE = FOV / NUM_RAYS
    const val MAX_DEPTH = 20.0

    const val PLAYER_SPEED = 4.0
    const val PLAYER_ROT_SPEED = 2.5
    const val PLAYER_MAX_HEALTH = 100.0

    val GAME_MAP = listOf(
        "1111111111111111",
        "1000000000000001",
        "1011110111101101",
        "1010000100001001",
        "1010111101111001",
        "1000100000001001",
        "1111101111101001",
        "1000000000000001",
        "1011111011111101",
        "1000000000000001",
        "1111111111111111"
    )

    const val WEAPON_DAMAGE = 34.0
    const val WEAPON_FIRE_RATE = 0.35 // seconds
    const val WEAPON_RANGE = MAX_DEPTH

    const val ENEMY_SPEED = 0.02
    const val ENEMY_HEALTH = 100.0
    const val ENEMY_ATTACK_DAMAGE = 10.0
    const val ENEMY_ATTACK_DIST = 1.2
}

class AegisPlayer(
    var x: Double = 1.5,
    var y: Double = 1.5,
    var angle: Double = 0.0,
    var health: Double = AegisSettings.PLAYER_MAX_HEALTH,
    var alive: Boolean = true
) {
    fun move(mapGrid: List<String>, forward: Double, strafe: Double, dt: Double) {
        if (!alive) return
        val dx = cos(angle) * forward - sin(angle) * strafe
        val dy = sin(angle) * forward + cos(angle) * strafe
        val speed = AegisSettings.PLAYER_SPEED * dt
        val newX = x + dx * speed
        val newY = y + dy * speed

        if (!AegisFpsEngine.isWall(mapGrid, newX, y)) {
            x = newX
        }
        if (!AegisFpsEngine.isWall(mapGrid, x, newY)) {
            y = newY
        }
    }

    fun rotate(dAngle: Double) {
        if (!alive) return
        angle = (angle + dAngle).mod(2 * Math.PI)
    }

    fun takeDamage(amount: Double) {
        health = max(0.0, health - amount)
        if (health <= 0.0) {
            alive = false
        }
    }
}

class AegisEnemy(
    var x: Double,
    var y: Double,
    var health: Double = AegisSettings.ENEMY_HEALTH,
    var alive: Boolean = true
) {
    fun update(mapGrid: List<String>, player: AegisPlayer, dt: Double): String {
        if (!alive || !player.alive) return "idle"
        val dist = hypot(player.x - x, player.y - y)
        if (dist < AegisSettings.ENEMY_ATTACK_DIST) {
            return "attack"
        }

        val step = AegisSettings.ENEMY_SPEED * dt * 60.0
        val dx = (player.x - x) / max(dist, 0.0001) * step
        val dy = (player.y - y) / max(dist, 0.0001) * step

        if (!AegisFpsEngine.isWall(mapGrid, x + dx, y)) {
            x += dx
        }
        if (!AegisFpsEngine.isWall(mapGrid, x, y + dy)) {
            y += dy
        }
        return "chase"
    }

    fun takeDamage(amount: Double) {
        health = max(0.0, health - amount)
        if (health <= 0.0) {
            alive = false
        }
    }
}

class AegisWeapon(
    val damage: Double = AegisSettings.WEAPON_DAMAGE,
    val fireRate: Double = AegisSettings.WEAPON_FIRE_RATE,
    val range: Double = AegisSettings.WEAPON_RANGE
) {
    var cooldown: Double = 0.0

    fun update(dt: Double) {
        if (cooldown > 0.0) {
            cooldown -= dt
        }
    }

    fun canFire(): Boolean = cooldown <= 0.0

    fun fire(player: AegisPlayer, enemies: List<AegisEnemy>, mapGrid: List<String>): AegisEnemy? {
        if (!canFire()) return null
        cooldown = fireRate
        var rayX = player.x
        var rayY = player.y
        val step = 0.05
        val maxSteps = (range / step).toInt()

        val cosA = cos(player.angle) * step
        val sinA = sin(player.angle) * step

        for (i in 0 until maxSteps) {
            rayX += cosA
            rayY += sinA

            if (AegisFpsEngine.isWall(mapGrid, rayX, rayY)) {
                return null
            }

            for (enemy in enemies) {
                if (!enemy.alive) continue
                if (hypot(enemy.x - rayX, enemy.y - rayY) < 0.40) {
                    enemy.takeDamage(damage)
                    return enemy
                }
            }
        }
        return null
    }
}

class AegisRaycaster(private val mapGrid: List<String>) {
    fun cast(player: AegisPlayer): List<Double> {
        val results = ArrayList<Double>(AegisSettings.NUM_RAYS)
        val startAngle = player.angle - AegisSettings.HALF_FOV

        for (i in 0 until AegisSettings.NUM_RAYS) {
            val angle = startAngle + i * AegisSettings.DELTA_ANGLE
            val depth = castSingle(player.x, player.y, angle)
            val perpDepth = depth * cos(angle - player.angle)
            results.add(max(perpDepth, 0.0001))
        }
        return results
    }

    private fun castSingle(x: Double, y: Double, angle: Double): Double {
        val sinA = sin(angle)
        val cosA = cos(angle)
        var depth = 0.0
        val step = 0.02
        while (depth < AegisSettings.MAX_DEPTH) {
            depth += step
            val testX = x + cosA * depth
            val testY = y + sinA * depth
            if (AegisFpsEngine.isWall(mapGrid, testX, testY)) {
                return depth
            }
        }
        return AegisSettings.MAX_DEPTH
    }
}

class AegisFpsEngine(val mapGrid: List<String> = AegisSettings.GAME_MAP) {
    var player = AegisPlayer()
    var weapon = AegisWeapon()
    val raycaster = AegisRaycaster(mapGrid)
    var enemies = mutableListOf(
        AegisEnemy(8.5, 3.5),
        AegisEnemy(12.5, 7.5),
        AegisEnemy(4.5, 8.5)
    )
    var score: Int = 0
    var lastHitTime: Long = 0L

    fun reset() {
        player = AegisPlayer()
        weapon = AegisWeapon()
        enemies = mutableListOf(
            AegisEnemy(8.5, 3.5),
            AegisEnemy(12.5, 7.5),
            AegisEnemy(4.5, 8.5)
        )
        score = 0
    }

    fun update(
        forward: Int = 0,
        strafe: Int = 0,
        dAngle: Double = 0.0,
        firing: Boolean = false,
        dt: Double = 1.0 / 60.0
    ): AegisStateSnapshot {
        player.move(mapGrid, forward.toDouble(), strafe.toDouble(), dt)
        player.rotate(dAngle)
        weapon.update(dt)

        for (enemy in enemies) {
            val state = enemy.update(mapGrid, player, dt)
            if (state == "attack") {
                player.takeDamage(AegisSettings.ENEMY_ATTACK_DAMAGE * dt)
            }
        }

        if (firing) {
            val hit = weapon.fire(player, enemies, mapGrid)
            if (hit != null) {
                lastHitTime = System.currentTimeMillis()
                if (!hit.alive) {
                    score += 100
                }
            }
        }

        return getState()
    }

    fun getState(): AegisStateSnapshot {
        return AegisStateSnapshot(
            playerX = player.x,
            playerY = player.y,
            playerAngle = player.angle,
            playerHealth = player.health,
            playerAlive = player.alive,
            enemies = enemies.map {
                AegisEnemySnapshot(it.x, it.y, it.health, it.alive)
            },
            score = score
        )
    }

    companion object {
        fun isWall(mapGrid: List<String>, x: Double, y: Double): Boolean {
            val row = y.toInt()
            val col = x.toInt()
            if (row < 0 || row >= mapGrid.size || col < 0 || col >= mapGrid[0].length) {
                return true
            }
            return mapGrid[row][col] == '1'
        }
    }
}

data class AegisEnemySnapshot(
    val x: Double,
    val y: Double,
    val health: Double,
    val alive: Boolean
)

data class AegisStateSnapshot(
    val playerX: Double,
    val playerY: Double,
    val playerAngle: Double,
    val playerHealth: Double,
    val playerAlive: Boolean,
    val enemies: List<AegisEnemySnapshot>,
    val score: Int
)
