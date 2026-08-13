package com.example.game

import com.example.data.model.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

data class BattleUnit(
    val id: String,
    val name: String,
    val heroClass: HeroClass?,
    val team: UnitTeam,
    val maxHp: Int,
    var currentHp: Int,
    val attack: Int,
    val defense: Int,
    val range: Int,
    val mobility: Int,
    val maxAp: Int,
    var currentAp: Int,
    var position: Position,
    val isBoss: Boolean = false,
    var shieldAmount: Int = 0,
    var isStunned: Boolean = false,
    val abilities: List<TacticalAbility> = emptyList()
) {
    val isAlive: Boolean get() = currentHp > 0
}

enum class BattlePhase {
    DEPLOYMENT,
    PLAYER_TURN,
    ENEMY_TURN,
    VICTORY,
    DEFEAT
}

data class TacticalBattleState(
    val mission: CampaignMission,
    val width: Int,
    val height: Int,
    val terrainMap: Map<Position, TerrainType>,
    val units: List<BattleUnit>,
    val phase: BattlePhase = BattlePhase.PLAYER_TURN,
    val currentTurn: Int = 1,
    val selectedUnitId: String? = null,
    val activeAbility: TacticalAbility? = null,
    val logs: List<BattleLog> = emptyList(),
    val controlPointOwner: UnitTeam? = null,
    val controlPointTurnsHeld: Int = 0
)

class TacticalBattleEngine(
    val mission: CampaignMission,
    squadHeroes: List<BattleUnit>
) {
    private val width = mission.gridWidth
    private val height = mission.gridHeight

    // Generate terrain grid with strategic covers, elevation, and control points
    private val terrainMap: MutableMap<Position, TerrainType> = mutableMapOf()
    private val units: MutableList<BattleUnit> = mutableListOf()
    private val logs: MutableList<BattleLog> = mutableListOf()

    lateinit var state: TacticalBattleState

    init {
        // Build Terrain Map
        for (x in 0 until width) {
            for (y in 0 until height) {
                val pos = Position(x, y)
                when {
                    x == 3 && y == 3 -> terrainMap[pos] = TerrainType.CONTROL_POINT
                    (x == 1 && y == 2) || (x == 4 && y == 4) || (x == 2 && y == 5) -> terrainMap[pos] = TerrainType.COVER
                    (x == 2 && y == 1) || (x == 5 && y == 3) -> terrainMap[pos] = TerrainType.ELEVATED
                    (x == 0 && y == 3) || (x == 5 && y == 0) -> terrainMap[pos] = TerrainType.HAZARD
                    else -> terrainMap[pos] = TerrainType.PLAIN
                }
            }
        }

        // Place Player Heroes on Starting Positions (Column 0 & 1)
        squadHeroes.forEachIndexed { index, hero ->
            val startPos = Position(0, index.coerceAtMost(height - 1))
            hero.position = startPos
            hero.currentAp = hero.maxAp
            units.add(hero)
        }

        // Place Initial Mission Enemies
        mission.initialEnemies.forEachIndexed { index, enemySpawn ->
            val abilities = if (enemySpawn.isBoss) {
                listOf(
                    TacticalAbility("boss_skill_1", "Missile Barrage", apCost = 3, range = 3, cooldownTurns = 2, type = AbilityType.AOE_ATTACK, powerAmount = 45, description = "Rains AOE fire on all adjacent tiles."),
                    TacticalAbility("boss_skill_2", "Titan Shield", apCost = 2, range = 0, cooldownTurns = 3, type = AbilityType.SHIELD, powerAmount = 100, description = "Gains heavy kinetic barrier.")
                )
            } else {
                listOf(
                    TacticalAbility("enemy_skill_1", "Overcharge Pulse", apCost = 2, range = enemySpawn.range, cooldownTurns = 2, type = AbilityType.ATTACK, powerAmount = 30, description = "Fires charged energy pulse.")
                )
            }

            units.add(
                BattleUnit(
                    id = "enemy_${index}_${enemySpawn.name.hashCode()}",
                    name = enemySpawn.name,
                    heroClass = null,
                    team = enemySpawn.team,
                    maxHp = enemySpawn.hp,
                    currentHp = enemySpawn.hp,
                    attack = enemySpawn.attack,
                    defense = enemySpawn.defense,
                    range = enemySpawn.range,
                    mobility = enemySpawn.mobility,
                    maxAp = 3,
                    currentAp = 3,
                    position = Position(enemySpawn.startX, enemySpawn.startY),
                    isBoss = enemySpawn.isBoss,
                    abilities = abilities
                )
            )
        }

        state = TacticalBattleState(
            mission = mission,
            width = width,
            height = height,
            terrainMap = terrainMap,
            units = units.toList(),
            phase = BattlePhase.PLAYER_TURN,
            currentTurn = 1,
            selectedUnitId = units.firstOrNull { it.team == UnitTeam.PLAYER_OI }?.id,
            logs = logs.toList()
        )

        addLog("Battle Commenced: ${mission.title}! Deployment complete.", LogType.SYSTEM)
    }

    private fun addLog(message: String, type: LogType = LogType.INFO) {
        val currentTurn = if (::state.isInitialized) state.currentTurn else 1
        logs.add(0, BattleLog(turn = currentTurn, message = message, logType = type))
    }

    fun selectUnit(unitId: String?) {
        val unit = units.find { it.id == unitId }
        if (unit != null && unit.isAlive) {
            state = state.copy(selectedUnitId = unitId, activeAbility = null)
        }
    }

    fun selectAbility(ability: TacticalAbility?) {
        state = state.copy(activeAbility = ability)
    }

    fun getValidMovePositions(unit: BattleUnit): List<Position> {
        if (unit.currentAp <= 0 || !unit.isAlive) return emptyList()
        val valid = mutableListOf<Position>()
        val maxDist = min(unit.mobility, unit.currentAp * 2)

        for (x in 0 until width) {
            for (y in 0 until height) {
                val targetPos = Position(x, y)
                val dist = unit.position.distanceTo(targetPos)
                val occupied = units.any { it.isAlive && it.position == targetPos }
                if (dist in 1..maxDist && !occupied) {
                    valid.add(targetPos)
                }
            }
        }
        return valid
    }

    fun getMoveApCost(from: Position, to: Position): Int {
        val dist = from.distanceTo(to)
        return max(1, (dist + 1) / 2)
    }

    fun moveUnit(unitId: String, targetPos: Position) {
        val unit = units.find { it.id == unitId } ?: return
        if (!unit.isAlive || unit.currentAp <= 0) return

        val dist = unit.position.distanceTo(targetPos)
        val apCost = max(1, (dist + 1) / 2)
        if (unit.currentAp < apCost) return

        val terrain = terrainMap[targetPos] ?: TerrainType.PLAIN
        unit.position = targetPos
        unit.currentAp -= apCost

        addLog("${unit.name} repositioned to (${targetPos.x}, ${targetPos.y}) [${terrain.title}].", LogType.INFO)

        // Check hazard
        if (terrain == TerrainType.HAZARD) {
            val dmg = 15
            unit.currentHp = max(0, unit.currentHp - dmg)
            addLog("${unit.name} stepped in Toxic Leak! Suffered $dmg hazard damage.", LogType.CRITICAL)
        }

        checkVictoryOrDefeatConditions()
        updateState()
    }

    fun performBasicAttack(attackerId: String, targetId: String) {
        val attacker = units.find { it.id == attackerId } ?: return
        val target = units.find { it.id == targetId } ?: return

        if (!attacker.isAlive || !target.isAlive || attacker.currentAp < 2) return

        val dist = attacker.position.distanceTo(target.position)
        if (dist > attacker.range) return

        attacker.currentAp -= 2

        // Terrain Modifiers
        val attackerTerrain = terrainMap[attacker.position] ?: TerrainType.PLAIN
        val targetTerrain = terrainMap[target.position] ?: TerrainType.PLAIN

        val elevationBonus = if (attackerTerrain == TerrainType.ELEVATED) 0.20f else 0.0f
        val coverReduction = if (targetTerrain == TerrainType.COVER) 0.30f else 0.0f

        val baseAtk = attacker.attack * (1.0f + elevationBonus)
        val effectiveDef = target.defense * (1.0f - coverReduction)

        val isCrit = (1..100).random() <= 20
        val critMult = if (isCrit) 1.75f else 1.0f

        var damage = max(8, ((baseAtk - effectiveDef) * critMult).toInt())

        // Shield Absorption
        if (target.shieldAmount > 0) {
            if (target.shieldAmount >= damage) {
                target.shieldAmount -= damage
                addLog("${attacker.name}'s attack absorbed by ${target.name}'s Kinetic Shield!", LogType.ATTACK)
                damage = 0
            } else {
                damage -= target.shieldAmount
                target.shieldAmount = 0
            }
        }

        if (damage > 0) {
            target.currentHp = max(0, target.currentHp - damage)
            val logType = if (isCrit) LogType.CRITICAL else LogType.ATTACK
            val critLabel = if (isCrit) " CRITICAL HIT!" else ""
            addLog("${attacker.name} attacked ${target.name} for $damage damage!$critLabel", logType)

            if (target.currentHp <= 0) {
                addLog("${target.name} was DEFEATED!", LogType.DEFEAT)
            }
        }

        checkVictoryOrDefeatConditions()
        updateState()
    }

    fun executeAbility(attackerId: String, ability: TacticalAbility, targetPos: Position) {
        val attacker = units.find { it.id == attackerId } ?: return
        if (!attacker.isAlive || attacker.currentAp < ability.apCost) return

        attacker.currentAp -= ability.apCost

        when (ability.type) {
            AbilityType.ATTACK, AbilityType.AOE_ATTACK -> {
                val targets = if (ability.type == AbilityType.AOE_ATTACK) {
                    units.filter { it.isAlive && it.position.distanceTo(targetPos) <= 1 && it.team != attacker.team }
                } else {
                    units.filter { it.isAlive && it.position == targetPos && it.team != attacker.team }
                }

                targets.forEach { target ->
                    val damage = max(15, ability.powerAmount + attacker.attack / 2 - target.defense / 2)
                    target.currentHp = max(0, target.currentHp - damage)
                    addLog("${attacker.name} used ${ability.name} on ${target.name} for $damage damage!", LogType.SKILL)
                    if (target.currentHp <= 0) {
                        addLog("${target.name} was ELIMINATED!", LogType.DEFEAT)
                    }
                }
            }
            AbilityType.HEAL -> {
                val targets = units.filter { it.isAlive && it.position.distanceTo(targetPos) <= 1 && it.team == attacker.team }
                targets.forEach { target ->
                    val healAmt = ability.powerAmount
                    target.currentHp = min(target.maxHp, target.currentHp + healAmt)
                    addLog("${attacker.name} cast ${ability.name} healing ${target.name} for +$healAmt HP!", LogType.HEAL)
                }
            }
            AbilityType.SHIELD -> {
                attacker.shieldAmount += ability.powerAmount
                addLog("${attacker.name} activated ${ability.name}, gaining +${ability.powerAmount} Kinetic Shield!", LogType.SKILL)
            }
            AbilityType.STUN -> {
                val target = units.find { it.isAlive && it.position == targetPos }
                if (target != null) {
                    target.isStunned = true
                    val damage = 20
                    target.currentHp = max(0, target.currentHp - damage)
                    addLog("${attacker.name} discharged ${ability.name} on ${target.name}! Stunned for 1 turn.", LogType.SKILL)
                }
            }
            AbilityType.BUFF_ATK -> {
                attacker.shieldAmount += 30
                addLog("${attacker.name} engaged ${ability.name}, boosting tactical readiness!", LogType.SKILL)
            }
        }

        checkVictoryOrDefeatConditions()
        updateState()
    }

    fun endPlayerTurn() {
        if (state.phase != BattlePhase.PLAYER_TURN) return

        addLog("--- Player Turn Completed ---", LogType.SYSTEM)

        // Process Control Point
        val cpPos = Position(3, 3)
        val occupant = units.find { it.isAlive && it.position == cpPos }
        var cpOwner = state.controlPointOwner
        var cpTurns = state.controlPointTurnsHeld

        if (occupant != null) {
            if (cpOwner == occupant.team) {
                cpTurns += 1
            } else {
                cpOwner = occupant.team
                cpTurns = 1
            }
            addLog("Cyber Terminal held by ${occupant.name} [${cpOwner.name}] ($cpTurns/2 Turns)!", LogType.SYSTEM)
        }

        state = state.copy(
            phase = BattlePhase.ENEMY_TURN,
            controlPointOwner = cpOwner,
            controlPointTurnsHeld = cpTurns
        )

        executeEnemyAiTurn()
    }

    private fun executeEnemyAiTurn() {
        val enemyUnits = units.filter { it.isAlive && it.team != UnitTeam.PLAYER_OI }
        val playerUnits = units.filter { it.isAlive && it.team == UnitTeam.PLAYER_OI }

        if (enemyUnits.isEmpty() || playerUnits.isEmpty()) {
            checkVictoryOrDefeatConditions()
            return
        }

        enemyUnits.forEach { enemy ->
            if (enemy.isStunned) {
                enemy.isStunned = false
                addLog("${enemy.name} is stunned and skipped turn!", LogType.SYSTEM)
                return@forEach
            }

            enemy.currentAp = enemy.maxAp

            // AI Decision logic: find target
            val nearestHero = playerUnits.minByOrNull { enemy.position.distanceTo(it.position) }
            if (nearestHero != null) {
                val dist = enemy.position.distanceTo(nearestHero.position)

                // Try attack if in range
                if (dist <= enemy.range && enemy.currentAp >= 2) {
                    performBasicAttack(enemy.id, nearestHero.id)
                } else if (enemy.currentAp > 0) {
                    // Move closer to hero
                    val dx = (nearestHero.position.x - enemy.position.x).coerceIn(-1, 1)
                    val dy = (nearestHero.position.y - enemy.position.y).coerceIn(-1, 1)
                    val newPos = Position(
                        (enemy.position.x + dx).coerceIn(0, width - 1),
                        (enemy.position.y + dy).coerceIn(0, height - 1)
                    )
                    if (!units.any { it.isAlive && it.position == newPos }) {
                        enemy.position = newPos
                        enemy.currentAp -= 1
                        addLog("${enemy.name} moved towards ${nearestHero.name}.", LogType.INFO)

                        // Try attack again if now in range
                        if (enemy.position.distanceTo(nearestHero.position) <= enemy.range && enemy.currentAp >= 2) {
                            performBasicAttack(enemy.id, nearestHero.id)
                        }
                    }
                }
            }
        }

        // Reset player AP for new turn
        units.filter { it.isAlive && it.team == UnitTeam.PLAYER_OI }.forEach {
            it.currentAp = it.maxAp
        }

        val newTurn = state.currentTurn + 1
        addLog("=== TURN $newTurn: Player Squad Ready ===", LogType.SYSTEM)

        state = state.copy(
            phase = BattlePhase.PLAYER_TURN,
            currentTurn = newTurn,
            selectedUnitId = units.firstOrNull { it.isAlive && it.team == UnitTeam.PLAYER_OI }?.id
        )

        checkVictoryOrDefeatConditions()
        updateState()
    }

    private fun checkVictoryOrDefeatConditions() {
        val aliveHeroes = units.count { it.isAlive && it.team == UnitTeam.PLAYER_OI }
        val aliveEnemies = units.count { it.isAlive && it.team != UnitTeam.PLAYER_OI }

        if (aliveHeroes == 0) {
            addLog("DEFEAT! All 'Oi' squad heroes were neutralized.", LogType.DEFEAT)
            state = state.copy(phase = BattlePhase.DEFEAT)
            return
        }

        if (aliveEnemies == 0) {
            addLog("VICTORY! All hostile corporate units eliminated!", LogType.SYSTEM)
            state = state.copy(phase = BattlePhase.VICTORY)
            return
        }

        // Control Point victory check
        if (state.controlPointOwner == UnitTeam.PLAYER_OI && state.controlPointTurnsHeld >= 2) {
            addLog("VICTORY! Cyber Terminal Secured by The Oi Squad!", LogType.SYSTEM)
            state = state.copy(phase = BattlePhase.VICTORY)
            return
        }
    }

    private fun updateState() {
        state = state.copy(
            units = units.toList(),
            logs = logs.toList()
        )
    }
}
