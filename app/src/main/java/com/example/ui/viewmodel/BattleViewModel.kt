package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.GearEntity
import com.example.data.db.HeroEntity
import com.example.data.model.*
import com.example.data.repository.GameRepository
import com.example.game.BattlePhase
import com.example.game.BattleUnit
import com.example.game.TacticalBattleEngine
import com.example.game.TacticalBattleState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.example.util.SoundManager

class BattleViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GameRepository = GameRepository(AppDatabase.getDatabase(application))

    private var engine: TacticalBattleEngine? = null

    private val _battleState = MutableStateFlow<TacticalBattleState?>(null)
    val battleState: StateFlow<TacticalBattleState?> = _battleState.asStateFlow()

    fun startMission(missionId: Int, squadHeroEntities: List<HeroEntity>, allGear: List<GearEntity>) {
        val mission = CampaignData.missions.find { it.id == missionId } ?: CampaignData.missions.first()

        val squadBattleUnits = squadHeroEntities.filter { it.isInSquad }.map { hero ->
            val heroClass = hero.heroClass

            // Calculate equipment stats
            val equippedWeapon = allGear.find { it.id == hero.equippedWeaponId }
            val equippedArmor = allGear.find { it.id == hero.equippedArmorId }
            val equippedCore = allGear.find { it.id == hero.equippedCoreId }

            val bonusAtk = (equippedWeapon?.attackBonus ?: 0) + (equippedCore?.attackBonus ?: 0)
            val bonusDef = (equippedArmor?.defenseBonus ?: 0) + (equippedCore?.defenseBonus ?: 0)
            val bonusHp = (equippedArmor?.hpBonus ?: 0) + (equippedCore?.hpBonus ?: 0)

            val totalHp = heroClass.baseHp + (hero.level - 1) * 20 + bonusHp
            val totalAtk = heroClass.baseAtk + (hero.level - 1) * 4 + bonusAtk
            val totalDef = heroClass.baseDef + (hero.level - 1) * 2 + bonusDef

            // Hero active skills based on class
            val abilities = when (heroClass) {
                HeroClass.VANGUARD -> listOf(
                    TacticalAbility("vanguard_s1", "Aegis Kinetic Barrier", apCost = 2, range = 1, cooldownTurns = 2, type = AbilityType.SHIELD, powerAmount = 65, description = "Generates +65 kinetic shield barrier."),
                    TacticalAbility("vanguard_s2", "Plasma Strike", apCost = 3, range = 1, cooldownTurns = 2, type = AbilityType.ATTACK, powerAmount = 45, description = "Slashes with high-temperature plasma blade.")
                )
                HeroClass.SNIPER -> listOf(
                    TacticalAbility("sniper_s1", "Armor-Piercing Round", apCost = 3, range = 5, cooldownTurns = 2, type = AbilityType.ATTACK, powerAmount = 75, description = "Long-range high-velocity critical shot."),
                    TacticalAbility("sniper_s2", "Target Lock", apCost = 1, range = 5, cooldownTurns = 1, type = AbilityType.BUFF_ATK, powerAmount = 25, description = "Enhances target lock and critical chance.")
                )
                HeroClass.CIPHER -> listOf(
                    TacticalAbility("cipher_s1", "System Overload Stun", apCost = 3, range = 3, cooldownTurns = 2, type = AbilityType.STUN, powerAmount = 30, description = "Disrupts enemy circuits, stunning for 1 turn."),
                    TacticalAbility("cipher_s2", "EMP Blast", apCost = 3, range = 3, cooldownTurns = 3, type = AbilityType.AOE_ATTACK, powerAmount = 40, description = "Discharges broad EMP damaging all adjacent units.")
                )
                HeroClass.MEDIC -> listOf(
                    TacticalAbility("medic_s1", "Nanite Regen Pulse", apCost = 2, range = 2, cooldownTurns = 1, type = AbilityType.HEAL, powerAmount = 55, description = "Restores +55 HP to target ally."),
                    TacticalAbility("medic_s2", "Nanite Barrier", apCost = 2, range = 2, cooldownTurns = 2, type = AbilityType.SHIELD, powerAmount = 40, description = "Grants +40 shield barrier to squad mate.")
                )
                HeroClass.SAMURAI -> listOf(
                    TacticalAbility("samurai_s1", "Monomolecular Slash", apCost = 2, range = 1, cooldownTurns = 1, type = AbilityType.ATTACK, powerAmount = 60, description = "Razor-sharp blade flourish."),
                    TacticalAbility("samurai_s2", "Shadow Dash Strike", apCost = 3, range = 3, cooldownTurns = 2, type = AbilityType.AOE_ATTACK, powerAmount = 50, description = "Dashes across grid slicing enemies.")
                )
            }

            BattleUnit(
                id = hero.id,
                name = hero.name,
                heroClass = heroClass,
                team = UnitTeam.PLAYER_OI,
                maxHp = totalHp,
                currentHp = totalHp,
                attack = totalAtk,
                defense = totalDef,
                range = heroClass.baseRange,
                mobility = heroClass.baseMobility,
                maxAp = heroClass.baseAp,
                currentAp = heroClass.baseAp,
                position = Position(0, 0),
                abilities = abilities
            )
        }

        engine = TacticalBattleEngine(mission, squadBattleUnits)
        _battleState.value = engine?.state
    }

    fun selectUnit(unitId: String?) {
        if (unitId != null) SoundManager.playClickSound()
        engine?.selectUnit(unitId)
        _battleState.value = engine?.state
    }

    fun selectAbility(ability: TacticalAbility?) {
        if (ability != null) SoundManager.playClickSound()
        engine?.selectAbility(ability)
        _battleState.value = engine?.state
    }

    fun getValidMovePositions(unit: BattleUnit): List<Position> {
        return engine?.getValidMovePositions(unit) ?: emptyList()
    }

    fun moveUnit(unitId: String, targetPos: Position) {
        SoundManager.playMoveSound()
        engine?.moveUnit(unitId, targetPos)
        _battleState.value = engine?.state
        checkBattleEndState()
    }

    fun performAttack(attackerId: String, targetId: String) {
        SoundManager.playAttackSound()
        engine?.performBasicAttack(attackerId, targetId)
        _battleState.value = engine?.state
        checkBattleEndState()
    }

    fun executeAbility(attackerId: String, ability: TacticalAbility, targetPos: Position) {
        when (ability.type) {
            AbilityType.HEAL, AbilityType.SHIELD -> SoundManager.playShieldHealSound()
            AbilityType.ATTACK, AbilityType.AOE_ATTACK -> SoundManager.playAttackSound()
            else -> SoundManager.playAbilitySound()
        }
        engine?.executeAbility(attackerId, ability, targetPos)
        _battleState.value = engine?.state
        checkBattleEndState()
    }

    fun endTurn() {
        SoundManager.playClickSound()
        viewModelScope.launch {
            engine?.endPlayerTurn()
            _battleState.value = engine?.state
            checkBattleEndState()
        }
    }

    private fun checkBattleEndState() {
        val currentState = engine?.state ?: return
        if (currentState.phase == BattlePhase.VICTORY) {
            SoundManager.playVictorySound()
        } else if (currentState.phase == BattlePhase.DEFEAT) {
            SoundManager.playDefeatSound()
        }
    }

    fun claimRewards(onComplete: () -> Unit) {
        val currentMission = engine?.mission ?: return
        viewModelScope.launch {
            repository.processVictoryRewards(currentMission)
            onComplete()
        }
    }
}
