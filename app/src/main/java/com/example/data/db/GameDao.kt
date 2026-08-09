package com.example.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {
    @Query("SELECT * FROM player_profile WHERE id = 1")
    fun getPlayerProfile(): Flow<PlayerProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: PlayerProfileEntity)

    @Query("UPDATE player_profile SET credits = credits + :addedCredits, tacticalData = tacticalData + :addedData WHERE id = 1")
    suspend fun addRewards(addedCredits: Int, addedData: Int)

    @Query("UPDATE player_profile SET unlockedMissionId = :missionId WHERE id = 1 AND unlockedMissionId < :missionId")
    suspend fun unlockMission(missionId: Int)

    @Query("UPDATE player_profile SET totalVictories = totalVictories + 1 WHERE id = 1")
    suspend fun incrementVictories()
}

@Dao
interface HeroDao {
    @Query("SELECT * FROM heroes")
    fun getAllHeroes(): Flow<List<HeroEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHeroes(heroes: List<HeroEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHero(hero: HeroEntity)

    @Query("UPDATE heroes SET isInSquad = :inSquad WHERE id = :heroId")
    suspend fun updateSquadStatus(heroId: String, inSquad: Boolean)

    @Query("UPDATE heroes SET level = :level, currentExp = :exp, maxExp = :maxExp WHERE id = :heroId")
    suspend fun updateHeroLevel(heroId: String, level: Int, exp: Int, maxExp: Int)

    @Query("UPDATE heroes SET equippedWeaponId = :weaponId WHERE id = :heroId")
    suspend fun equipWeapon(heroId: String, weaponId: String?)

    @Query("UPDATE heroes SET equippedArmorId = :armorId WHERE id = :heroId")
    suspend fun equipArmor(heroId: String, armorId: String?)

    @Query("UPDATE heroes SET equippedCoreId = :coreId WHERE id = :heroId")
    suspend fun equipCore(heroId: String, coreId: String?)
}

@Dao
interface GearDao {
    @Query("SELECT * FROM gear_items")
    fun getAllGear(): Flow<List<GearEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGear(gearList: List<GearEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSingleGear(gear: GearEntity)

    @Query("DELETE FROM gear_items WHERE id = :gearId")
    suspend fun deleteGear(gearId: String)

    @Query("UPDATE gear_items SET isEquipped = :isEquipped, assignedHeroId = :heroId WHERE id = :gearId")
    suspend fun updateGearEquipStatus(gearId: String, isEquipped: Boolean, heroId: String?)
}
