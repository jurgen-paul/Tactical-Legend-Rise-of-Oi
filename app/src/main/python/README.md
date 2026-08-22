# AEGIS FPS ENGINE

A first-person raycasting shooter with one shared Python game core used by both a desktop build (`pygame`) and an Android build (`Chaquopy` / Native Jetpack Compose Canvas).

## Architecture

- **`app/src/main/python/settings.py`**: Constants (map, weapon stats, player stats, raycasting constants).
- **`app/src/main/python/engine.py`**: Pure Python game logic (player movement, hostile AI, hitscan weapon ballistics, raycasting without rendering dependencies).
- **`app/src/main/python/renderer_pygame.py`**: Desktop-only renderer using Pygame for input and rendering.
- **`app/src/main/java/com/example/game/aegis/AegisGameView.kt`**: Android SurfaceView / Canvas raycast rendering bridge.
- **`app/src/main/java/com/example/game/aegis/AegisFpsEngine.kt`**: Native 60FPS Kotlin port of the core simulation and raycasting math.
- **`app/src/main/java/com/example/ui/screens/AegisFpsScreen.kt`**: Full interactive 3D Raycasting FPS game screen in Jetpack Compose with dual touch controls, D-Pad movement, swipe look, primary fire, crosshair tracking, minimap radar, and combat rewards!

## Desktop Execution

```bash
pip install pygame
python3 app/src/main/python/renderer_pygame.py
```

- **Controls**: WASD to move, mouse / arrow keys to look, left-click or SPACE to fire, ESC to quit.

## Android Execution

- Launchable directly from the **War Room** and **Arcade** menus via the **Aegis 3D FPS Simulator** mode.
- Includes on-screen touch D-Pad, rotation swipe pad, quick rotation controls, fire trigger, real-time minimap radar, enemy health bars, audio feedback, and score rewards (+350 Cryptokeys, +150 DNI Data).
