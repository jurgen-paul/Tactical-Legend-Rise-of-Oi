"""
AEGIS FPS ENGINE - Settings
Core constants shared by every module. This file is also the piece
you port first when moving the engine to a new platform (Android/Godot),
since map format, player stats, and weapon data all live here.
"""
import math

# --- Display ---
WIDTH, HEIGHT = 1024, 640
HALF_WIDTH, HALF_HEIGHT = WIDTH // 2, HEIGHT // 2
FPS = 60

# --- Raycasting ---
FOV = math.pi / 3           # 60 degree field of view
HALF_FOV = FOV / 2
NUM_RAYS = WIDTH // 2        # ray resolution (lower = faster, blockier)
DELTA_ANGLE = FOV / NUM_RAYS
MAX_DEPTH = 20
SCREEN_DIST = HALF_WIDTH / math.tan(HALF_FOV)
SCALE = WIDTH // NUM_RAYS

# --- Player ---
PLAYER_SPEED = 4.0
PLAYER_ROT_SPEED = 2.5
PLAYER_MAX_HEALTH = 100
PLAYER_SIZE_SCALE = 60

# --- Map ---
TILE_SIZE = 1  # world units per grid cell

# 1 = wall, 0 = floor. Any layout works as long as it's rectangular-ish
# (unlisted cells default to wall, so the border is always sealed).
GAME_MAP = [
    "1111111111111111",
    "1000000000000001",
    "1011110111101101",
    "1010000100001001",
    "1010111101111001",
    "1000100000001001",
    "1111101111101001",
    "1000000000000001",
    "1011111011111101",
    "1000000000000001",
    "1111111111111111",
]

# --- Weapon ---
WEAPON_DAMAGE = 34
WEAPON_FIRE_RATE = 0.35   # seconds between shots
WEAPON_RANGE = MAX_DEPTH

# --- Enemy ---
ENEMY_SPEED = 0.02
ENEMY_HEALTH = 100
ENEMY_ATTACK_DAMAGE = 10
ENEMY_ATTACK_DIST = 1.2

# --- Colors ---
BLACK = (0, 0, 0)
WHITE = (255, 255, 255)
DARKGRAY = (40, 40, 40)
FLOOR_COLOR = (50, 50, 50)
CEIL_COLOR = (25, 25, 30)
RED = (200, 30, 30)
GREEN = (30, 200, 60)
