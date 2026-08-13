package com.example.data.repository

import com.example.data.db.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class GameRepository(private val db: AppDatabase) {

    val playerProfile: Flow<PlayerProfileEntity?> = db.playerDao().getPlayerProfile()
    val allHeroes: Flow<List<HeroEntity>> = db.heroDao().getAllHeroes()
    val allGear: Flow<List<GearEntity>> = db.gearDao().getAllGear()

    suspend fun initializeDefaultDataIfNeeded() {
        val profile = playerProfile.firstOrNull()
        if (profile == null) {
            db.playerDao().insertOrUpdateProfile(
                PlayerProfileEntity(
                    playerName = "Commander Oi",
                    credits = 1500,
                    tacticalData = 600,
                    unlockedMissionId = 1,
                    totalVictories = 0,
                    badgeRank = "Sovereign Street Oi"
                )
            )

            // Seed 5 Black Ops III Specialist Operatives
            val defaultHeroes = listOf(
                HeroEntity(
                    id = "hero_vanguard",
                    name = "Ruin (Donnie Walsh)",
                    heroClass = HeroClass.VANGUARD,
                    level = 1,
                    isInSquad = true,
                    equippedWeaponId = "gear_sword_1",
                    equippedArmorId = "gear_vest_1"
                ),
                HeroEntity(
                    id = "hero_sniper",
                    name = "Outrider (Alessandra)",
                    heroClass = HeroClass.SNIPER,
                    level = 1,
                    isInSquad = true,
                    equippedWeaponId = "gear_rifle_1"
                ),
                HeroEntity(
                    id = "hero_cipher",
                    name = "Prophet (David Wilkes)",
                    heroClass = HeroClass.CIPHER,
                    level = 1,
                    isInSquad = true,
                    equippedCoreId = "gear_chip_1"
                ),
                HeroEntity(
                    id = "hero_medic",
                    name = "Battery (Erin Baker)",
                    heroClass = HeroClass.MEDIC,
                    level = 1,
                    isInSquad = false
                ),
                HeroEntity(
                    id = "hero_samurai",
                    name = "Seraph (He Zhen-Zhen)",
                    heroClass = HeroClass.SAMURAI,
                    level = 1,
                    isInSquad = false
                )
            )
            db.heroDao().insertHeroes(defaultHeroes)

            // Seed Initial Starter Gear
            val starterGear = listOf(
                GearEntity(
                    id = "gear_sword_1",
                    name = "Gravity Spikes Shockwave",
                    type = GearType.WEAPON,
                    rarity = Rarity.COMMON,
                    attackBonus = 14,
                    isEquipped = true,
                    assignedHeroId = "hero_vanguard"
                ),
                GearEntity(
                    id = "gear_vest_1",
                    name = "DNI Kinetic Armor",
                    type = GearType.ARMOR,
                    rarity = Rarity.COMMON,
                    defenseBonus = 10,
                    hpBonus = 45,
                    isEquipped = true,
                    assignedHeroId = "hero_vanguard"
                ),
                GearEntity(
                    id = "gear_rifle_1",
                    name = "Sparrow Explosive Bow",
                    type = GearType.WEAPON,
                    rarity = Rarity.RARE,
                    attackBonus = 25,
                    critBonus = 12,
                    isEquipped = true,
                    assignedHeroId = "hero_sniper"
                ),
                GearEntity(
                    id = "gear_chip_1",
                    name = "Tempest Arc Charge Core",
                    type = GearType.CIPHER_CHIP,
                    rarity = Rarity.RARE,
                    attackBonus = 12,
                    hpBonus = 30,
                    isEquipped = true,
                    assignedHeroId = "hero_cipher"
                ),
                GearEntity(
                    id = "gear_core_free",
                    name = "Glitch DNI Time Recall",
                    type = GearType.NANITE_CORE,
                    rarity = Rarity.RARE,
                    hpBonus = 65,
                    defenseBonus = 8,
                    isEquipped = false
                )
            )
            db.gearDao().insertGear(starterGear)
        }
    }

    suspend fun toggleHeroSquadStatus(heroId: String, currentStatus: Boolean) {
        db.heroDao().updateSquadStatus(heroId, !currentStatus)
    }

    suspend fun equipItem(heroId: String, gear: GearEntity) {
        // Unequip currently equipped gear in that slot for this hero
        when (gear.type) {
            GearType.WEAPON -> db.heroDao().equipWeapon(heroId, gear.id)
            GearType.ARMOR -> db.heroDao().equipArmor(heroId, gear.id)
            GearType.NANITE_CORE, GearType.CIPHER_CHIP -> db.heroDao().equipCore(heroId, gear.id)
        }
        db.gearDao().updateGearEquipStatus(gear.id, true, heroId)
    }

    suspend fun unequipItem(gearId: String, heroId: String, gearType: GearType) {
        when (gearType) {
            GearType.WEAPON -> db.heroDao().equipWeapon(heroId, null)
            GearType.ARMOR -> db.heroDao().equipArmor(heroId, null)
            GearType.NANITE_CORE, GearType.CIPHER_CHIP -> db.heroDao().equipCore(heroId, null)
        }
        db.gearDao().updateGearEquipStatus(gearId, false, null)
    }

    suspend fun craftNewGear(creditsCost: Int = 300, dataCost: Int = 150): GearEntity? {
        val profile = playerProfile.firstOrNull() ?: return null
        if (profile.credits < creditsCost || profile.tacticalData < dataCost) return null

        // Deduct resources
        db.playerDao().addRewards(-creditsCost, -dataCost)

        val gearTypes = GearType.values()
        val rarities = listOf(Rarity.COMMON, Rarity.RARE, Rarity.EPIC, Rarity.LEGENDARY)
        val selectedType = gearTypes.random()
        val rarityRoll = (1..100).random()
        val selectedRarity = when {
            rarityRoll > 92 -> Rarity.LEGENDARY
            rarityRoll > 70 -> Rarity.EPIC
            rarityRoll > 35 -> Rarity.RARE
            else -> Rarity.COMMON
        }

        val prefixes = listOf("Black Ops", "Mastercraft", "DNI", "Blackjack", "Corvus", "Cyber", "Pack-A-Punch")
        val weaponNames = listOf("Kuda SMG", "Man-O-War Rifle", "Scythe Minigun", "SVG-100 Sniper", "Purifier Flamethrower", "War Machine Launcher")
        val armorNames = listOf("Kinetic Armor Rig", "Heat Wave Shield", "DNI Exosuit Plate", "Combat Focus Weave")
        val coreNames = listOf("Glitch DNI Module", "Psychosis Hologram Core", "H.I.V.E. Pod Generator", "Vision Pulse Scanner")

        val name = when (selectedType) {
            GearType.WEAPON -> "${prefixes.random()} ${weaponNames.random()}"
            GearType.ARMOR -> "${prefixes.random()} ${armorNames.random()}"
            GearType.NANITE_CORE, GearType.CIPHER_CHIP -> "${prefixes.random()} ${coreNames.random()}"
        }

        val multiplier = selectedRarity.multiplier
        val newGear = GearEntity(
            id = "gear_${System.currentTimeMillis()}",
            name = name,
            type = selectedType,
            rarity = selectedRarity,
            attackBonus = if (selectedType == GearType.WEAPON) (15 * multiplier).toInt() else (5 * multiplier).toInt(),
            defenseBonus = if (selectedType == GearType.ARMOR) (12 * multiplier).toInt() else 0,
            hpBonus = (30 * multiplier).toInt(),
            critBonus = if (selectedType == GearType.WEAPON) (8 * multiplier).toInt() else 0,
            isEquipped = false
        )

        db.gearDao().insertSingleGear(newGear)
        return newGear
    }

    suspend fun addArcadeRewards(creditsEarned: Int, dataEarned: Int) {
        if (creditsEarned > 0 || dataEarned > 0) {
            db.playerDao().addRewards(creditsEarned, dataEarned)
        }
    }

    suspend fun purchaseCreditBundle(creditsAmount: Int, dataAmount: Int) {
        db.playerDao().addRewards(creditsAmount, dataAmount)
    }

    suspend fun purchaseDirectGear(gear: GearEntity) {
        db.gearDao().insertSingleGear(gear)
    }

    suspend fun purchaseVipPass(newBadgeRank: String, bonusCredits: Int, bonusData: Int) {
        db.playerDao().addRewards(bonusCredits, bonusData)
        val profile = playerProfile.firstOrNull() ?: return
        db.playerDao().insertOrUpdateProfile(profile.copy(badgeRank = newBadgeRank))
    }

    suspend fun processVictoryRewards(mission: CampaignMission, expGain: Int = 60) {
        db.playerDao().addRewards(mission.rewardCredits, mission.rewardData)
        db.playerDao().incrementVictories()
        db.playerDao().unlockMission(mission.id + 1)

        // Award reward gear
        val rewardGear = GearEntity(
            id = "reward_gear_${mission.id}_${System.currentTimeMillis()}",
            name = mission.rewardGearName,
            type = if (mission.id % 2 == 0) GearType.ARMOR else GearType.WEAPON,
            rarity = mission.rewardGearRarity,
            attackBonus = (20 * mission.rewardGearRarity.multiplier).toInt(),
            defenseBonus = (15 * mission.rewardGearRarity.multiplier).toInt(),
            hpBonus = (50 * mission.rewardGearRarity.multiplier).toInt(),
            critBonus = (10 * mission.rewardGearRarity.multiplier).toInt()
        )
        db.gearDao().insertSingleGear(rewardGear)

        // Level up participating heroes
        val heroes = allHeroes.firstOrNull() ?: emptyList()
        heroes.filter { it.isInSquad }.forEach { hero ->
            val newExp = hero.currentExp + expGain
            if (newExp >= hero.maxExp) {
                db.heroDao().updateHeroLevel(
                    heroId = hero.id,
                    level = hero.level + 1,
                    exp = newExp - hero.maxExp,
                    maxExp = (hero.maxExp * 1.4f).toInt()
                )
            } else {
                db.heroDao().updateHeroLevel(
                    heroId = hero.id,
                    level = hero.level,
                    exp = newExp,
                    maxExp = hero.maxExp
                )
            }
        }
    }
}
