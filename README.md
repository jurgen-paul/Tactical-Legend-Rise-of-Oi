# Tactical Legend: Rise of Oi

An immersive, turn-based tactical squad RPG set in a dystopian cyberpunk universe. Command "The Oi" elite operative squad, navigate grid-based combat missions, forge high-tech cybernetic gear, and reclaim the metropolis from enemy factions.

---

## 🎮 Key Features

### ⚔️ Tactical Grid Combat Engine
- **Grid-Based Combat**: Position squad members on tactical battlefields, maneuver behind cover, and outflank hostile forces.
- **Action Point (AP) System**: Strategic resource management for movement, basic attacks, and class abilities.
- **Operative Classes**:
  - **Vanguard**: Frontline tank with heavy shield generators and high durability.
  - **Sniper**: Long-range precision specialist dealing devastating critical damage.
  - **Cipher**: Electronic warfare hacker capable of disabling enemy optics and disrupting shields.
  - **Medic**: Combat field doctor restoring squad integrity and cleansing status ailments.
  - **Samurai**: High-mobility melee assassin equipped with thermal blades.
- **Tactical Abilities**: Execute targeted class skills, AoE abilities, and field buffs.
- **Boss Battles**: Engage heavily armored enemy commanders with unique attack patterns and higher health pools.

### 🔬 Cyber Armory & Nanite Forge
- **Nanite Forge Matrix**: Synthesize new military-grade weapons, kinetic weave armors, nanite cores, and cipher chips using Cyber Credits and Tactical Data.
- **Rarity System**: Forge items spanning Common, Rare, Epic, and Legendary tiers with scaling stat modifiers.

### 🎒 Squad Inventory & Loadout Manager
- **Stat Modifier Tracking**: Inspect weapon damage, armor values, HP bonuses, and critical strike multipliers.
- **Real-Time Stat Deltas**: Compare stat changes before equipping gear to squad members.
- **Filter & Sort**: Categorize equipment by gear type (Weapon, Armor, Core) and deployment status (Equipped vs. Vault Storage).

### 🗺️ Campaign Missions & Progression
- **Chapter Storyline**: Fight through story missions across cyberpunk sectors.
- **Mission Rewards**: Earn Cyber Credits, Tactical Data, and XP upon completing missions.
- **Base Operations**: Monitor squad status, level up operatives, and manage resources from the War Room hub.

---

## 🛠️ Tech Stack & Architecture

- **Language**: 100% Kotlin
- **UI Framework**: Jetpack Compose with Material Design 3
- **Architecture**: MVVM (Model-View-ViewModel) + Clean Data Architecture
- **Local Database**: Room Persistence Library with KSP (Kotlin Symbol Processing)
- **Asynchronous Operations**: Kotlin Coroutines & `StateFlow`
- **Navigation**: Type-safe Compose Navigation
- **Design System**: Custom Warm Tactical Cyberpunk palette with accessible contrast and dynamic Material 3 components

---

## 📁 Project Structure

```text
com.example/
├── data/
│   ├── db/              # Room Entities, DAOs, and Database Configuration
│   ├── model/           # Core Game Domain Models, Hero Classes & Rarity
│   └── repository/      # Game Repository Layer for Local Data Management
├── ui/
│   ├── navigation/      # Screen Routes and Navigation Controller
│   ├── screens/         # Compose Screens (Battle, Inventory, Armory, Campaign, Main Menu)
│   └── theme/           # Tactical Cyberpunk Color Schemes, Typography, and M3 Shapes
└── MainActivity.kt      # Main Entry Point & ViewModel Binding
```

---

## 🚀 Building & Running

### Prerequisites
- Android Studio Ladybug or newer
- JDK 17+
- Android SDK 24+ (Android 7.0)

### Build Commands
To compile and test the applet locally:

```bash
# Build Debug APK
gradle :app:assembleDebug

# Run Unit Tests
gradle :app:testDebugUnitTest
```

---

## 📄 License
Created for Google AI Studio Build. All rights reserved.
