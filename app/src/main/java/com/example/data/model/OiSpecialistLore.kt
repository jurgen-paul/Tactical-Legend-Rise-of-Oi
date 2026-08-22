package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.*

data class OiSpecialistInfo(
    val id: String,
    val callsign: String,
    val realName: String,
    val heroClass: HeroClass,
    val roleTag: String,
    val signatureWeapon: String,
    val signatureAbility: String,
    val quote: String,
    val backgroundLore: String,
    val accentColor: Color,
    val icon: ImageVector,
    val tacticalSpecialty: String,
    val weaponDescription: String,
    val abilityDescription: String,
    val synergyPartners: List<String>
)

object OiSpecialistRoster {
    val specialists = listOf(
        OiSpecialistInfo(
            id = "hero_vanguard",
            callsign = "RUIN",
            realName = "Donnie Walsh",
            heroClass = HeroClass.VANGUARD,
            roleTag = "Frontline Breacher",
            signatureWeapon = "Gravity Spikes",
            signatureAbility = "Overdrive Cyber Sprint",
            quote = "\"You have a nice day now. First in, last out, no excuses.\"",
            backgroundLore = "Raised in a rough military family, Donnie Walsh replaced both his arms with heavy cybernetic augmentations to deliver unstoppable kinetic shockwaves at point-blank range.",
            accentColor = CyberRed,
            icon = Icons.Default.FlashOn,
            tacticalSpecialty = "High HP Tank & Melee Area Stun",
            weaponDescription = "Twin kinetic pile-drivers that slam into the ground, triggering a massive lethal shockwave.",
            abilityDescription = "Temporarily boosts movement speed, armor hardness, and kinetic dash capability.",
            synergyPartners = listOf("Prophet", "Battery")
        ),
        OiSpecialistInfo(
            id = "hero_cipher",
            callsign = "PROPHET",
            realName = "David Wilkes",
            heroClass = HeroClass.CIPHER,
            roleTag = "Cyber Disruptor & Hacker",
            signatureWeapon = "Tempest Arc Rifle",
            signatureAbility = "Glitch DNI Time Warp",
            quote = "\"With these upgrades, you never stood a chance. Technology is evolution.\"",
            backgroundLore = "British cyber-warfare operative who continually modifies his body with state-of-the-art DNI augments, turning himself into a living tactical quantum processor.",
            accentColor = CyberPrimary,
            icon = Icons.Default.Bolt,
            tacticalSpecialty = "EMP Shock, Circuit Stun & Grid Disruption",
            weaponDescription = "Fires a concentrated bolt of high-voltage lightning that arcs between grouped enemy targets.",
            abilityDescription = "Rolls back his spatial position and quantum phase by several seconds to evade lethal incoming attacks.",
            synergyPartners = listOf("Ruin", "Outrider")
        ),
        OiSpecialistInfo(
            id = "hero_sniper",
            callsign = "OUTRIDER",
            realName = "Alessandra Castillo",
            heroClass = HeroClass.SNIPER,
            roleTag = "Precision Recon Marksman",
            signatureWeapon = "Sparrow Explosive Bow",
            signatureAbility = "Vision Pulse Telemetry",
            quote = "\"Target acquired. One shot, one clean kill. Nothing escapes my sights.\"",
            backgroundLore = "Hailing from the favelas of Rio de Janeiro, Alessandra mastered bowmanship and high-altitude recon before joining elite Brazilian special operations forces.",
            accentColor = CyberYellow,
            icon = Icons.Default.TrackChanges,
            tacticalSpecialty = "Long-Range Armor Piercing & Thermal Wallscan",
            weaponDescription = "Compound bow armed with armor-penetrating arrows that detonate shortly after impact.",
            abilityDescription = "Sends out a sonar frequency pulse revealing hidden enemy positions through cover and terrain.",
            synergyPartners = listOf("Spectre", "Nomad")
        ),
        OiSpecialistInfo(
            id = "hero_medic",
            callsign = "BATTERY",
            realName = "Erin Baker",
            heroClass = HeroClass.MEDIC,
            roleTag = "Heavy Support & Demolitions",
            signatureWeapon = "War Machine 40mm",
            signatureAbility = "Kinetic Armor Matrix",
            quote = "\"Fire in the hole! Kinetic armor holding! Let's shake the ground!\"",
            backgroundLore = "From a multi-generational military lineage, Erin Baker is an uncompromising heavy demolitionist with an advanced reactive mesh barrier suited for combat healing and triage.",
            accentColor = CyberGreen,
            icon = Icons.Default.Shield,
            tacticalSpecialty = "Nanite Squad Healing & Defensive Barrier",
            weaponDescription = "Semi-automatic 40mm rotary grenade launcher that fires bouncing explosive micro-canisters.",
            abilityDescription = "Activates a reactive nanite mesh that deflects incoming ballistic and kinetic damage.",
            synergyPartners = listOf("Ruin", "Firebreak")
        ),
        OiSpecialistInfo(
            id = "hero_samurai",
            callsign = "SERAPH",
            realName = "He Zhen-Zhen",
            heroClass = HeroClass.SAMURAI,
            roleTag = "Syndicate Enforcer",
            signatureWeapon = "Annihilator Revolver",
            signatureAbility = "Combat Focus Multiplier",
            quote = "\"The 54 Immortals crush all weakness. Discipline over disorder.\"",
            backgroundLore = "Adopted daughter of a high-ranking 54 Immortals syndicate leader in Singapore, Zhen-Zhen was trained from childhood with surgical blade mastery and lethal cyber pistol accuracy.",
            accentColor = CyberSecondary,
            icon = Icons.Default.MilitaryTech,
            tacticalSpecialty = "High Critical Damage & Critical AP Burst",
            weaponDescription = "Heavy high-caliber revolver firing depleted-uranium rounds that pierce through multiple enemies.",
            abilityDescription = "Channels supreme focus, doubling action point efficiency and tactical score accumulation.",
            synergyPartners = listOf("Spectre", "Reaper")
        ),
        OiSpecialistInfo(
            id = "hero_nomad",
            callsign = "NOMAD",
            realName = "Tavo Rojas",
            heroClass = HeroClass.CIPHER,
            roleTag = "Wilderness Survivalist & Trapper",
            signatureWeapon = "H.I.V.E. Nanobot Pods",
            signatureAbility = "Rejack Revival Nanites",
            quote = "\"The jungle provides. Watch your step, or the swarm will find you.\"",
            backgroundLore = "Sole survivor of the Rapid Deployment Force in the Colombian jungle, Tavo Rojas utilizes autonomous nanobot micro-drones to create deadly perimeter traps.",
            accentColor = Color(0xFF4CAF50),
            icon = Icons.Default.BugReport,
            tacticalSpecialty = "Perimeter Minefields & Self-Revival Protocols",
            weaponDescription = "Deployable nanobot pods that release a carnivorous swarm when enemy units step in proximity.",
            abilityDescription = "Injects a concentrated dose of regenerative nanoparticles, allowing self-revival upon receiving fatal damage.",
            synergyPartners = listOf("Outrider", "Prophet")
        ),
        OiSpecialistInfo(
            id = "hero_spectre",
            callsign = "SPECTRE",
            realName = "Classified (Ghost)",
            heroClass = HeroClass.SAMURAI,
            roleTag = "Stealth Phantom Assassin",
            signatureWeapon = "Ripper Monoblades",
            signatureAbility = "Active Camo Cloak",
            quote = "\"Clean cut. They won't even know what struck them.\"",
            backgroundLore = "Virtually nothing is known of Spectre's true identity, past allegiance, or citizenship. A ghost mercenary whose cybernetic stealth suit renders them invisible until the blades strike.",
            accentColor = Color(0xFF9C27B0),
            icon = Icons.Default.VisibilityOff,
            tacticalSpecialty = "Invisibility, Flanking & High Single-Target Execution",
            weaponDescription = "Twin twin-edged monomolecular plasma blades that lock on and dismember hostile infantry in seconds.",
            abilityDescription = "Bends light and optical sensors around the chassis, providing true battlefield invisibility.",
            synergyPartners = listOf("Seraph", "Outrider")
        ),
        OiSpecialistInfo(
            id = "hero_reaper",
            callsign = "REAPER",
            realName = "EWR-115 Combat Synth",
            heroClass = HeroClass.VANGUARD,
            roleTag = "Autonomous War Machine",
            signatureWeapon = "Scythe Arm Gatling",
            signatureAbility = "Psychosis Decoy Holograms",
            quote = "\"Combat parameters engaged. Eliminating organic and synthetic hostiles.\"",
            backgroundLore = "The sole functional prototype of the Experimental War Robot series, saved from decommissioning and retrofitted with bleeding-edge tactical combat AI algorithms.",
            accentColor = Color(0xFFFF9800),
            icon = Icons.Default.SmartToy,
            tacticalSpecialty = "Heavy Armor, High-Rate Suppression & Decoy Diversion",
            weaponDescription = "Transformable right arm that morphs into a six-barrel mini-gun with astronomical firing rate.",
            abilityDescription = "Projects 3 autonomous holographic clones that draw enemy fire and confuse hostile targeting systems.",
            synergyPartners = listOf("Ruin", "Seraph")
        ),
        OiSpecialistInfo(
            id = "hero_firebreak",
            callsign = "FIREBREAK",
            realName = "Krystof Hejek",
            heroClass = HeroClass.VANGUARD,
            roleTag = "Pyrotechnic Breacher",
            signatureWeapon = "Purifier Flamethrower",
            signatureAbility = "Heat Wave EMP Pulse",
            quote = "\"Burn it all down. Clear the room. Nothing survives the inferno.\"",
            backgroundLore = "Hardened in the industrial fires of Prague, Krystof is a fearlessly aggressive breacher who specializes in extreme thermal warfare, incinerating enemy bunkers and deflecting projectiles.",
            accentColor = Color(0xFFFF5722),
            icon = Icons.Default.LocalFireDepartment,
            tacticalSpecialty = "Wide Area Thermal Denial & Shield Breaker",
            weaponDescription = "High-pressure flamethrower firing hyper-reactive napalm that sticks to and melts armor alloys.",
            abilityDescription = "Emits a massive thermal energy blast that disables enemy electronics, evaporates shields, and stuns.",
            synergyPartners = listOf("Battery", "Ruin")
        )
    )

    fun getSpecialist(id: String): OiSpecialistInfo? {
        return specialists.find { it.id == id }
    }

    fun getSpecialistByName(name: String): OiSpecialistInfo? {
        val lower = name.lowercase()
        return specialists.find {
            lower.contains(it.callsign.lowercase()) ||
            lower.contains(it.realName.lowercase()) ||
            it.id == name
        }
    }
}
