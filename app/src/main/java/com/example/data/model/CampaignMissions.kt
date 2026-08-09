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
            title = "Sector 7 Alley Breach",
            subtitle = "Clear the Corporate Security Patrol",
            briefing = "Corporate security drones have cordoned off Sector 7. Command 'The Oi' squad to eliminate the security threat and reclaim the street.",
            gridWidth = 6,
            gridHeight = 6,
            recommendedPower = 120,
            rewardCredits = 300,
            rewardData = 100,
            rewardGearName = "Vanguard Plasma Edge",
            rewardGearRarity = Rarity.RARE,
            victoryConditionText = "Eliminate all 3 Corporate Drones.",
            initialEnemies = listOf(
                EnemySpawn("Corp Security Drone A", hp = 100, attack = 20, defense = 6, range = 2, mobility = 3, startX = 4, startY = 1, team = UnitTeam.ENEMY_CORPORATE),
                EnemySpawn("Corp Security Drone B", hp = 100, attack = 20, defense = 6, range = 2, mobility = 3, startX = 5, startY = 2, team = UnitTeam.ENEMY_CORPORATE),
                EnemySpawn("Street Enforcer Bot", hp = 140, attack = 26, defense = 10, range = 1, mobility = 2, startX = 4, startY = 4, team = UnitTeam.ENEMY_CORPORATE)
            )
        ),
        CampaignMission(
            id = 2,
            chapter = 1,
            title = "Neon Plaza Terminal",
            subtitle = "Capture & Hold Control Point",
            briefing = "The central cyber terminal at Neon Plaza feeds data to the syndicate. Secure the terminal at (3,3) or neutralize all enemy combatants.",
            gridWidth = 6,
            gridHeight = 7,
            recommendedPower = 250,
            rewardCredits = 550,
            rewardData = 220,
            rewardGearName = "Nanite Field Generator",
            rewardGearRarity = Rarity.EPIC,
            victoryConditionText = "Defeat all enemies or hold Cyber Terminal.",
            initialEnemies = listOf(
                EnemySpawn("Sentry Turret Mech", hp = 160, attack = 30, defense = 12, range = 3, mobility = 1, startX = 5, startY = 1, team = UnitTeam.ENEMY_MECH),
                EnemySpawn("Heavy Riot Cyborg", hp = 210, attack = 28, defense = 16, range = 1, mobility = 2, startX = 4, startY = 3, team = UnitTeam.ENEMY_MECH),
                EnemySpawn("Cipher Suppressor", hp = 130, attack = 24, defense = 8, range = 3, mobility = 3, startX = 5, startY = 5, team = UnitTeam.ENEMY_CORPORATE)
            )
        ),
        CampaignMission(
            id = 3,
            chapter = 2,
            title = "Subterranean Cyber Vault",
            subtitle = "Infiltrate the Syndicate Depot",
            briefing = "Heavy syndicate forces have fortified the underground grid. Watch out for elevated terrain platforms and toxic energy leaks.",
            gridWidth = 7,
            gridHeight = 7,
            recommendedPower = 420,
            rewardCredits = 900,
            rewardData = 400,
            rewardGearName = "Apex Monomolecular Blade",
            rewardGearRarity = Rarity.LEGENDARY,
            victoryConditionText = "Destroy all 4 Syndicate Vanguard units.",
            initialEnemies = listOf(
                EnemySpawn("Syndicate Assassin", hp = 180, attack = 44, defense = 10, range = 1, mobility = 4, startX = 6, startY = 1, team = UnitTeam.ENEMY_CORPORATE),
                EnemySpawn("Heavy Siege Mech", hp = 280, attack = 36, defense = 20, range = 2, mobility = 2, startX = 5, startY = 3, team = UnitTeam.ENEMY_MECH),
                EnemySpawn("Pulse Marksman Drone", hp = 150, attack = 40, defense = 8, range = 4, mobility = 3, startX = 6, startY = 5, team = UnitTeam.ENEMY_CORPORATE),
                EnemySpawn("Nanite Overlord Bot", hp = 220, attack = 28, defense = 14, range = 2, mobility = 3, startX = 4, startY = 6, team = UnitTeam.ENEMY_MECH)
            )
        ),
        CampaignMission(
            id = 4,
            chapter = 2,
            title = "Apex Boss Raid: XERXES-9",
            subtitle = "The Final Stand of the Oi",
            briefing = "Apex Overlord XERXES-9 commands the central spire. Unleash the full strength of 'The Oi' squad to defeat this mechanical titan!",
            gridWidth = 7,
            gridHeight = 7,
            recommendedPower = 650,
            rewardCredits = 2000,
            rewardData = 1000,
            rewardGearName = "Sovereign Oi Crest of Valor",
            rewardGearRarity = Rarity.LEGENDARY,
            victoryConditionText = "Destroy Boss XERXES-9!",
            isBossRaid = true,
            initialEnemies = listOf(
                EnemySpawn("XERXES-9 TITAN", hp = 650, attack = 52, defense = 22, range = 3, mobility = 2, startX = 5, startY = 3, team = UnitTeam.ENEMY_BOSS, isBoss = true),
                EnemySpawn("Titan Guard Drone L", hp = 140, attack = 24, defense = 10, range = 2, mobility = 2, startX = 4, startY = 1, team = UnitTeam.ENEMY_MECH),
                EnemySpawn("Titan Guard Drone R", hp = 140, attack = 24, defense = 10, range = 2, mobility = 2, startX = 4, startY = 5, team = UnitTeam.ENEMY_MECH)
            )
        )
    )
}
