package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.GearEntity
import com.example.data.db.HeroEntity
import com.example.data.db.PlayerProfileEntity
import com.example.data.model.GearType
import com.example.data.repository.GameRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val repository: GameRepository

    val playerProfile: StateFlow<PlayerProfileEntity?>
    val heroes: StateFlow<List<HeroEntity>>
    val gearList: StateFlow<List<GearEntity>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = GameRepository(database)

        playerProfile = repository.playerProfile.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        heroes = repository.allHeroes.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        gearList = repository.allGear.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        viewModelScope.launch {
            repository.initializeDefaultDataIfNeeded()
        }
    }

    fun toggleSquadStatus(heroId: String, currentInSquad: Boolean) {
        viewModelScope.launch {
            repository.toggleHeroSquadStatus(heroId, currentInSquad)
        }
    }

    fun equipItem(heroId: String, gear: GearEntity) {
        viewModelScope.launch {
            repository.equipItem(heroId, gear)
        }
    }

    fun unequipItem(gearId: String, heroId: String, gearType: GearType) {
        viewModelScope.launch {
            repository.unequipItem(gearId, heroId, gearType)
        }
    }

    fun craftGear(onSuccess: (GearEntity) -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            val created = repository.craftNewGear()
            if (created != null) {
                onSuccess(created)
            } else {
                onError()
            }
        }
    }

    fun claimArcadeRewards(creditsEarned: Int, dataEarned: Int) {
        viewModelScope.launch {
            repository.addArcadeRewards(creditsEarned, dataEarned)
        }
    }
}
