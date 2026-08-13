package com.example.data.model

data class CampaignMission(
    val id: Int,
    val chapter: Int,
    val title: String,
    val subtitle: String,
    val briefing: String,
    val gridWidth: Int,
    val gridHeight: Int,
    val recommendedPower: Int,
    val rewardCredits: Int,
    val rewardData: Int,
    val rewardGearName: String,
    val rewardGearRarity: Rarity,
    val victoryConditionText: String,
    val isBossRaid: Boolean = false,
    val initialEnemies: List<EnemySpawn>
)

data class EnemySpawn(
    val name: String,
    val hp: Int,
    val attack: Int,
    val defense: Int,
    val range: Int,
    val mobility: Int,
    val startX: Int,
    val startY: Int,
    val team: UnitTeam,
    val isBoss: Boolean = false
)

object CampaignData {
    val missions = listOf(
        CampaignMission(
            id = 1,
            chapter = 1,
            title = "Singapore DNI Facility",
            subtitle = "Clear CDP Combat Drones & RAPS",
            briefing = "Common Defense Pact (CDP) forces have breached the DNI research facility in Singapore. Command your Black Ops Specialist unit to neutralize enemy RAPS Drones and regain control.",
            gridWidth = 6,
            gridHeight = 6,
            recommendedPower = 120,
            rewardCredits = 350,
            rewardData = 120,
            rewardGearName = "Sheiva Marksman Rifle",
            rewardGearRarity = Rarity.RARE,
            victoryConditionText = "Eliminate all 3 CDP Combat Drones.",
            initialEnemies = listOf(
                EnemySpawn("CDP Vanguard Bot A", hp = 100, attack = 20, defense = 6, range = 2, mobility = 3, startX = 4, startY = 1, team = UnitTeam.ENEMY_CORPORATE),
                EnemySpawn("CDP Vanguard Bot B", hp = 100, attack = 20, defense = 6, range = 2, mobility = 3, startX = 5, startY = 2, team = UnitTeam.ENEMY_CORPORATE),
                EnemySpawn("R.A.P.S. Explosive Drone", hp = 130, attack = 28, defense = 8, range = 1, mobility = 4, startX = 4, startY = 4, team = UnitTeam.ENEMY_CORPORATE)
            )
        ),
        CampaignMission(
            id = 2,
            chapter = 1,
            title = "Nuketown 2065 Terminal",
            subtitle = "Capture & Hold DNI Cyber Terminal",
            briefing = "The simulation grid at Nuketown 2065 holds critical DNI intel at (3,3). Secure and hold the terminal before CDP automated turrets lock down the perimeter.",
            gridWidth = 6,
            gridHeight = 7,
            recommendedPower = 250,
            rewardCredits = 600,
            rewardData = 250,
            rewardGearName = "DNI Kinetic Armor Weave",
            rewardGearRarity = Rarity.EPIC,
            victoryConditionText = "Defeat all enemies or hold DNI Cyber Terminal.",
            initialEnemies = listOf(
                EnemySpawn("Cerberus Heavy Turret", hp = 170, attack = 32, defense = 12, range = 3, mobility = 1, startX = 5, startY = 1, team = UnitTeam.ENEMY_MECH),
                EnemySpawn("A.S.P. Riot Mech", hp = 220, attack = 28, defense = 18, range = 1, mobility = 2, startX = 4, startY = 3, team = UnitTeam.ENEMY_MECH),
                EnemySpawn("G.I. Unit Suppressor", hp = 140, attack = 26, defense = 10, range = 3, mobility = 3, startX = 5, startY = 5, team = UnitTeam.ENEMY_CORPORATE)
            )
        ),
        CampaignMission(
            id = 3,
            chapter = 2,
            title = "Zombies: Shadows of Evil",
            subtitle = "Morg City Undead Grid",
            briefing = "Element 115 outbreak in Morg City! Fight through hordes of Undead Walkers, Keeper Phantoms, and the multi-headed Margwa monster.",
            gridWidth = 7,
            gridHeight = 7,
            recommendedPower = 420,
            rewardCredits = 1000,
            rewardData = 450,
            rewardGearName = "Ray Gun Mark III",
            rewardGearRarity = Rarity.LEGENDARY,
            victoryConditionText = "Survive the Undead Wave and defeat Margwa!",
            initialEnemies = listOf(
                EnemySpawn("Shadows Undead Walker", hp = 160, attack = 38, defense = 8, range = 1, mobility = 3, startX = 6, startY = 1, team = UnitTeam.ENEMY_CORPORATE),
                EnemySpawn("Keeper Phantom", hp = 210, attack = 34, defense = 14, range = 2, mobility = 3, startX = 5, startY = 3, team = UnitTeam.ENEMY_MECH),
                EnemySpawn("Apothicon Shadow-Drone", hp = 140, attack = 42, defense = 8, range = 4, mobility = 4, startX = 6, startY = 5, team = UnitTeam.ENEMY_CORPORATE),
                EnemySpawn("Margwa Horror Head", hp = 280, attack = 46, defense = 16, range = 1, mobility = 2, startX = 4, startY = 6, team = UnitTeam.ENEMY_MECH)
            )
        ),
        CampaignMission(
            id = 4,
            chapter = 2,
            title = "Mindscape Boss Raid: CORVUS AI",
            subtitle = "The Frozen Forest Confrontation",
            briefing = "The rogue AI Corvus threatens to absorb all human DNI networks into the Mindscape. Deploy your elite Black Ops Specialist squad to destroy Corvus Core!",
            gridWidth = 7,
            gridHeight = 7,
            recommendedPower = 650,
            rewardCredits = 2500,
            rewardData = 1200,
            rewardGearName = "Mastercraft Annihilator",
            rewardGearRarity = Rarity.LEGENDARY,
            victoryConditionText = "Destroy Corvus Cyber Mindscape Titan!",
            isBossRaid = true,
            initialEnemies = listOf(
                EnemySpawn("CORVUS MINDSCAPE TITAN", hp = 700, attack = 55, defense = 24, range = 3, mobility = 2, startX = 5, startY = 3, team = UnitTeam.ENEMY_BOSS, isBoss = true),
                EnemySpawn("Corvus DNI Drone L", hp = 150, attack = 26, defense = 10, range = 2, mobility = 2, startX = 4, startY = 1, team = UnitTeam.ENEMY_MECH),
                EnemySpawn("Corvus DNI Drone R", hp = 150, attack = 26, defense = 10, range = 2, mobility = 2, startX = 4, startY = 5, team = UnitTeam.ENEMY_MECH)
            )
        )
    )
}
