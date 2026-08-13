package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.GearType
import com.example.data.model.HeroClass
import com.example.data.model.Rarity

@Entity(tableName = "player_profile")
data class PlayerProfileEntity(
    @PrimaryKey val id: Int = 1,
    val playerName: String = "DNI Commander",
    val credits: Int = 1250,
    val tacticalData: Int = 450,
    val squadLevel: Int = 1,
    val totalVictories: Int = 0,
    val unlockedMissionId: Int = 1,
    val badgeRank: String = "Master Prestige Oistars Ops"
)

@Entity(tableName = "heroes")
data class HeroEntity(
    @PrimaryKey val id: String,
    val name: String,
    val heroClass: HeroClass,
    val level: Int = 1,
    val currentExp: Int = 0,
    val maxExp: Int = 100,
    val equippedWeaponId: String? = null,
    val equippedArmorId: String? = null,
    val equippedCoreId: String? = null,
    val isInSquad: Boolean = true
)

@Entity(tableName = "gear_items")
data class GearEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: GearType,
    val rarity: Rarity,
    val attackBonus: Int = 0,
    val defenseBonus: Int = 0,
    val hpBonus: Int = 0,
    val critBonus: Int = 0,
    val isEquipped: Boolean = false,
    val assignedHeroId: String? = null
)
