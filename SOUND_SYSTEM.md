# 🔊 Tactical Cyberpunk Audio System & Sound Management Utility

The **Sound Management Utility** (`com.example.util.SoundManager`) provides real-time, zero-dependency synthetic audio feedback across **Tactical Legend: Rise of Oi**.

---

## 🎧 Architecture & Implementation

The audio engine utilizes native Android **`ToneGenerator`** and **`AudioTrack` PCM waveform synthesis** to output low-latency, retro-cyberpunk audio feedback for all combat actions, arcade interactions, and UI navigation without requiring external `.mp3` or `.wav` binary assets.

- **Package**: `com.example.util.SoundManager`
- **Concurrency**: Asynchronous execution powered by Kotlin Coroutines (`Dispatchers.Default` thread pool).
- **Audio Usage**: `AudioAttributes.USAGE_GAME` with `CONTENT_TYPE_SONIFICATION`.

---

## ⚔️ Combat Audio Triggers

| Action | Audio Effect | Technical Specification / Frequency |
| :--- | :--- | :--- |
| **Grid Movement** | `playMoveSound()` | Short 40ms high-pitch tactical step ping (`TONE_PROP_BEEP`) |
| **Basic Laser Attack** | `playAttackSound()` | 120ms high-frequency DTMF blast sweep (880 Hz → 220 Hz decay) |
| **Active Class Ability** | `playAbilitySound()` | 150ms ascending tone pulse (`TONE_SUP_PIP`) |
| **Shield / Nanite Heal** | `playShieldHealSound()` | Dual-chime ascending frequency interval (300 Hz → 600 Hz) |
| **Damage Impact / Hit** | `playHitSound()` | 90ms low-register impact thud (`TONE_CDMA_LOW_L`) |
| **Mission Victory** | `playVictorySound()` | Arpeggiated 4-note victory chord sequence |
| **Squad Defeat** | `playDefeatSound()` | Descending 3-note critical failure alarm |
| **UI Button Tap** | `playClickSound()` | Subtle 30ms acknowledgment click (`TONE_PROP_ACK`) |

---

## 💻 Code Usage Examples

### Playing Audio on Grid Actions
```kotlin
import com.example.util.SoundManager

// Unit Movement
fun moveUnit(unitId: String, targetPos: Position) {
    SoundManager.playMoveSound()
    engine.moveUnit(unitId, targetPos)
}

// Firing Laser Weapon
fun performAttack(attackerId: String, targetId: String) {
    SoundManager.playAttackSound()
    engine.performBasicAttack(attackerId, targetId)
}

// Deploying Nanite Shield or Healing
fun executeAbility(attackerId: String, ability: TacticalAbility, targetPos: Position) {
    if (ability.type == AbilityType.SHIELD || ability.type == AbilityType.HEAL) {
        SoundManager.playShieldHealSound()
    } else {
        SoundManager.playAbilitySound()
    }
    engine.executeAbility(attackerId, ability, targetPos)
}
```

### Toggle Mute / Sound Settings
```kotlin
// Disable all audio feedback
SoundManager.isSoundEnabled = false

// Enable audio feedback
SoundManager.isSoundEnabled = true
```

---

## 🕹️ Arcade Hack Simulator Audio Integration

In the **Cyber Arcade Simulator**, node interactions trigger real-time audio responses:
- **Rogue Drone Tap**: Fires `playAttackSound()`
- **EMP Blast Powerup**: Fires `playAbilitySound()`
- **Shield Boost Node**: Fires `playShieldHealSound()`
- **Overdrive Core Matrix**: Fires `playVictorySound()`

---

*Designed and implemented for Tactical Legend: Rise of Oi.*
