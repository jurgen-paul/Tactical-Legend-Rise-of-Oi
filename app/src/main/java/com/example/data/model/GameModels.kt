package com.example.data.model

enum class HeroClass(
    val title: String,
    val role: String,
    val description: String,
    val baseHp: Int,
    val baseAtk: Int,
    val baseDef: Int,
    val baseRange: Int,
    val baseMobility: Int,
    val baseAp: Int
) {
    VANGUARD(
        title = "Vanguard Enforcer",
        role = "Frontline Tank",
        description = "Heavy armored 'Oi' enforcer equipped with kinetic shield and taunt matrix.",
        baseHp = 220,
        baseAtk = 28,
        baseDef = 18,
        baseRange = 1,
        baseMobility = 3,
        baseAp = 4
    ),
    SNIPER(
        title = "Neon Marksman",
        role = "Long-Range Marksman",
        description = "Precision sniper capable of armor-piercing critical strikes from distance.",
        baseHp = 120,
        baseAtk = 48,
        baseDef = 8,
        baseRange = 4,
        baseMobility = 3,
        baseAp = 4
    ),
    CIPHER(
        title = "Cipher Hacker",
        role = "Tactical Support / Debuff",
        description = "Electronic warfare specialist who disrupts enemy systems, drains energy, and stuns mechs.",
        baseHp = 140,
        baseAtk = 32,
        baseDef = 10,
        baseRange = 3,
        baseMobility = 4,
        baseAp = 5
    ),
    MEDIC(
        title = "Nanite Medic",
        role = "Combat Healer / Buffer",
        description = "Field surgeon with nanite dispersion spray and defensive energy barriers.",
        baseHp = 150,
        baseAtk = 22,
        baseDef = 12,
        baseRange = 2,
        baseMobility = 4,
        baseAp = 5
    ),
    SAMURAI(
        title = "Cyber Blade",
        role = "High Mobility Assassin",
        description = "Agile melee phantom utilizing monomolecular blades and shadow dashes.",
        baseHp = 160,
        baseAtk = 42,
        baseDef = 12,
        baseRange = 1,
        baseMobility = 5,
        baseAp = 5
    )
}

enum class UnitTeam {
    PLAYER_OI,
    ENEMY_CORPORATE,
    ENEMY_MECH,
    ENEMY_BOSS
}

enum class AbilityType {
    ATTACK,
    HEAL,
    SHIELD,
    STUN,
    AOE_ATTACK,
    BUFF_ATK
}

data class TacticalAbility(
    val id: String,
    val name: String,
    val apCost: Int,
    val range: Int,
    val cooldownTurns: Int,
    val type: AbilityType,
    val powerAmount: Int,
    val description: String
)

enum class TerrainType(
    val title: String,
    val coverReductionPercent: Int,
    val damageBonusPercent: Int,
    val movementCost: Int
) {
    PLAIN("Open Field", 0, 0, 1),
    COVER("Barrier Cover", 30, 0, 1),
    ELEVATED("Elevated Ridge", 10, 20, 2),
    HAZARD("Acid/Energy Leak", 0, -10, 2),
    CONTROL_POINT("Cyber Terminal", 15, 0, 1)
}

enum class Rarity(val label: String, val multiplier: Float) {
    COMMON("Common", 1.0f),
    RARE("Rare", 1.25f),
    EPIC("Epic", 1.55f),
    LEGENDARY("Legendary", 2.0f)
}

enum class GearType(val label: String) {
    WEAPON("Weapon System"),
    ARMOR("Tactical Weave"),
    NANITE_CORE("Nanite Core"),
    CIPHER_CHIP("Cipher Chip")
}

data class Position(val x: Int, val y: Int) {
    fun distanceTo(other: Position): Int = kotlin.math.abs(x - other.x) + kotlin.math.abs(y - other.y)
}

enum class LogType {
    INFO,
    ATTACK,
    CRITICAL,
    SKILL,
    HEAL,
    DEFEAT,
    SYSTEM
}

data class BattleLog(
    val id: String = java.util.UUID.randomUUID().toString(),
    val turn: Int,
    val message: String,
    val logType: LogType = LogType.INFO
)
