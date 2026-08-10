# 🎮 Tactical Legend: Rise of Oi — Complete Game & Systems Guide

Welcome to the definitive guide for **Tactical Legend: Rise of Oi**, a feature-rich turn-based tactical squad RPG set in a dystopian cyberpunk metropolis.

---

## 📑 Table of Contents
1. [Overview](#-overview)
2. [Squad Roster & Operative Classes](#-squad-roster--operative-classes)
3. [Grid Combat & Tactical Battle Engine](#-grid-combat--tactical-battle-engine)
4. [Cyber Cinema & Video Player Module](#-cyber-cinema--video-player-module)
5. [Cyber Arcade Hack Simulator](#-cyber-arcade-hack-simulator)
6. [Nanite Forge & Armory Loadouts](#-nanite-forge--armory-loadouts)
7. [Black Market Cyber Store & Payments](#-black-market-cyber-store--payments)
8. [Synthesized Audio Engine](#-synthesized-audio-engine)
9. [Database Architecture & Local Persistence](#-database-architecture--local-persistence)

---

## 🏙️ Overview

In **Tactical Legend: Rise of Oi**, you command "The Oi" elite operative squad across grid-based battlefields to reclaim Neo-Tokyo sub-sectors from rogue corporate AI factions and cybernetic warlords.

### Key Game Features
- **Turn-Based Tactical Combat**: Grid position strategy, line-of-sight checks, action points (AP), cover dynamics, and class abilities.
- **Cyber Media Cinema Video Player**: Integrated 4K stream video player for game trailers, raid previews, and tutorials with credit rewards.
- **Cyber Arcade Hack Simulator**: Matrix mini-game with real-time drone tapping, EMP powerups, and multiplier combos.
- **Nanite Forge Armory**: Craft weapons, kinetic armor, nanite cores, and cipher chips spanning Common to Legendary tiers.
- **Zero-Dependency Audio Engine**: Real-time synthesized audio feedback for steps, attacks, shields, hits, and victories.

---

## 👥 Squad Roster & Operative Classes

Command five specialized operative classes, each possessing unique base attributes and tactical field abilities:

| Operative | Class | Role | Base HP | Base ATK | Special Field Ability |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Jax "Aegis" Vane** | Vanguard | Frontline Tank | 220 | 28 | **Plasma Barrier**: Grants +40 Shielding & Taunts adjacent hostiles. |
| **Lyra "Viper" Swift** | Sniper | Long-Range Marksman | 120 | 52 | **Overcharge Shot**: Delivers 2.5x Critical Damage across 6 grid tiles. |
| **Zero "Ghost" Kai** | Cipher | Electronic Hacker | 140 | 38 | **EMP Pulse**: Disables enemy kinetic shields & stuns target for 1 turn. |
| **Dr. Elena Vance** | Medic | Field Combat Doctor | 150 | 22 | **Nanite Surge**: Restores 60 HP to adjacent squad members. |
| **Kael "Ronin" Blade** | Samurai | Melee Assassin | 175 | 45 | **Blade Dance**: Strikes up to 3 adjacent target hostiles. |

---

## ⚔️ Grid Combat & Tactical Battle Engine

### Combat Mechanics
- **Grid Positioning**: Battles occur on an 8x6 tactical grid with movement range limits based on class AP.
- **Target Selection**: Highlighted attack and ability range overlays show valid target tiles.
- **Turn Sequence**: Player Phase -> AI Hostile Phase. End turn when action points are spent.
- **Dynamic HUD**: Real-time turn indicator, active unit cards, HP/Shield gauges, and victory condition tracking.

---

## 🎬 Cyber Cinema & Video Player Module

The **Cyber Media Cinema** allows players to stream official game trailers, boss raid cutscenes, and tactical tutorials.

### Player Capabilities
- **Native VideoView Integration**: High-definition video streaming using native Android media controls.
- **Interactive Control HUD**: Play/Pause, Rewind 10s, Forward 10s, progress scrub bar, volume mute toggle, and full-screen expansion.
- **Watch & Earn Credits**: Claim **+50 Cyber Credits** per video watched to fund Nanite Forge crafting.

### Video Stream Catalog
1. *Rise of Oi - Official Cyber Gameplay Trailer* (01:30)
2. *Arasaka Spire Boss Raid Infiltration* (02:15)
3. *Nanite Forge & Cybernetic Weapon Synthesis* (01:45)
4. *Cyber Arcade Matrix Hack Simulator* (01:10)

---

## 👾 Cyber Arcade Hack Simulator

An interactive matrix mini-game accessible directly from the Main Hub.

### Game Rules
- **Objective**: Neutralize incoming Rogue Drones before they breach the Firewall line at the bottom of the grid.
- **Special Nodes**:
  - 💥 **EMP Powerup**: Clears all active rogue drones on field.
  - 🛡️ **Shield Boost**: Restores +20% Firewall integrity.
  - ⭐ **Overdrive Core**: Adds +500 PTS and boosts combo multiplier by +3x.
- **Difficulty Intensities**: Standard (1.0x), Overdrive (1.4x), and Frenzy (1.8x).
- **Squad Rewards**: High scores automatically convert to Cyber Credits and Tactical Data.

---

## 🔬 Nanite Forge & Armory Loadouts

Equip your operatives with military-grade cybernetics synthesized in the Nanite Forge:

- **Equipment Types**:
  - ⚔️ **Weapons**: Increases Attack (ATK) and Critical Hit Rate.
  - 🛡️ **Armor**: Increases Defense (DEF) and Max HP.
  - ⚡ **Nanite Cores**: Hybrid bonuses to HP, Defense, and Skill Cooldowns.
  - 💾 **Cipher Chips**: Boosts Critical Damage and Hacking power.
- **Rarity Modifier System**:
  - **Common**: 1.0x Stat Scale
  - **Rare**: 1.3x Stat Scale
  - **Epic**: 1.7x Stat Scale
  - **Legendary**: 2.3x Stat Scale

---

## 🛒 Black Market Cyber Store & Payments

The **Black Market Store** provides direct in-app purchases for game items, currency vault packs, legendary gear, and VIP rank passes:

### Store Catalog
- 💳 **Credit Vaults**:
  - *Cyber Credit Stash*: +1,200 Credits & +300 Data ($0.99)
  - *Black Market Matrix Vault*: +4,500 Credits & +1,200 Data ($2.99)
  - *Cyber Overlord Mega Vault*: +15,000 Credits & +4,500 Data ($9.99)
- ⚔️ **Direct Legendary Weapons & Gear**:
  - *Plasma Singularity Scythe* (Legendary Weapon, +65 ATK, +25% Crit) — $2.99
  - *Aegis Dreadnought Armor* (Legendary Kinetic Exosuit, +60 DEF, +220 HP) — $3.99
  - *Quantum Cipher Core v5* (Epic Cipher Chip, +40 ATK, +120 HP, +15% Crit) — $1.99
- 👑 **VIP Cyber Passes**:
  - *Sovereign VIP Cyber Pass*: Unlocks 'Sovereign Cyber Overlord' title, +6,000 Credits, +2,500 Data, & permanent XP boosts — $4.99

### Payment Gateway
- Supports simulated **Google Play In-App Billing**, **Cyber-Crypto Wallet**, and **Oi Sovereign Black Card**.
- Direct state modification in Room database upon order authorization.

---

## 🔊 Synthesized Audio Engine

The application features a built-in `SoundManager` (`com.example.util.SoundManager`) generating real-time PCM waveforms and tone pulses:

- **Grid Move**: 40ms step ping (`TONE_PROP_BEEP`)
- **Laser Attack**: 120ms frequency sweep (880 Hz -> 220 Hz)
- **Nanite Shield/Heal**: Dual-frequency ascending chord (300 Hz -> 600 Hz)
- **Damage Hit**: 90ms low impact thud (`TONE_CDMA_LOW_L`)
- **Victory Chime**: 4-note ascending fanfare
- **Defeat Alarm**: 3-note critical failure tone

---

## 💾 Database Architecture & Local Persistence

Powered by **Room Persistence Library** (`AppDatabase`):

- **`PlayerProfileEntity`**: Tracks Cyber Credits, Tactical Data, Player Level, XP, Victories, and Defeats.
- **`HeroEntity`**: Stores operative level, XP, current HP, base stats, and equipped item IDs.
- **`EquipmentEntity`**: Stores item inventory, rarity, stat bonuses, and equip status.
- **`CampaignMissionEntity`**: Tracks mission progress, sector status, and victory flags.

---

*Tactical Legend: Rise of Oi — Built with Kotlin, Jetpack Compose, Material Design 3, and Room.*
