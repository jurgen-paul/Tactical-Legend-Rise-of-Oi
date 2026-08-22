"""
AEGIS FPS ENGINE - Core (pure Python, no rendering deps)
This module has ZERO dependency on pygame or any UI toolkit.
It is the exact file Chaquopy imports on Android — Kotlin calls into
these classes each frame and draws the results with Canvas/SurfaceView.
The desktop build (renderer_pygame.py) imports this same module.
"""
import math
from settings import (
    GAME_MAP, PLAYER_SPEED, PLAYER_ROT_SPEED, PLAYER_MAX_HEALTH,
    NUM_RAYS, DELTA_ANGLE, HALF_FOV, MAX_DEPTH, SCREEN_DIST, TILE_SIZE,
    WEAPON_DAMAGE, WEAPON_FIRE_RATE, WEAPON_RANGE,
    ENEMY_SPEED, ENEMY_HEALTH, ENEMY_ATTACK_DAMAGE, ENEMY_ATTACK_DIST,
)

def is_wall(map_grid, x, y):
    row = int(y)
    col = int(x)
    if row < 0 or row >= len(map_grid) or col < 0 or col >= len(map_grid[0]):
        return True
    return map_grid[row][col] == "1"

class Player:
    def __init__(self, x=1.5, y=1.5, angle=0.0):
        self.x = x
        self.y = y
        self.angle = angle
        self.health = PLAYER_MAX_HEALTH
        self.alive = True

    def move(self, map_grid, forward=0, strafe=0, dt=1.0):
        """forward/strafe in [-1, 0, 1]. dt = seconds since last frame."""
        dx = (math.cos(self.angle) * forward - math.sin(self.angle) * strafe)
        dy = (math.sin(self.angle) * forward + math.cos(self.angle) * strafe)
        speed = PLAYER_SPEED * dt
        new_x = self.x + dx * speed
        new_y = self.y + dy * speed
        # slide along walls: test each axis separately
        if not is_wall(map_grid, new_x, self.y):
            self.x = new_x
        if not is_wall(map_grid, self.x, new_y):
            self.y = new_y

    def rotate(self, d_angle):
        self.angle = (self.angle + d_angle) % (2 * math.pi)

    def take_damage(self, amount):
        self.health = max(0, self.health - amount)
        if self.health == 0:
            self.alive = False

class Enemy:
    def __init__(self, x, y, health=ENEMY_HEALTH):
        self.x = x
        self.y = y
        self.health = health
        self.alive = True

    def update(self, map_grid, player, dt=1.0):
        if not self.alive or not player.alive:
            return "idle"
        dist = math.hypot(player.x - self.x, player.y - self.y)
        if dist < ENEMY_ATTACK_DIST:
            return "attack"
        # move toward player, one axis at a time to allow sliding
        step = ENEMY_SPEED * dt * 60  # normalize roughly to dt
        dx = (player.x - self.x) / max(dist, 0.0001) * step
        dy = (player.y - self.y) / max(dist, 0.0001) * step
        if not is_wall(map_grid, self.x + dx, self.y):
            self.x += dx
        if not is_wall(map_grid, self.x, self.y + dy):
            self.y += dy
        return "chase"

    def take_damage(self, amount):
        self.health = max(0, self.health - amount)
        if self.health == 0:
            self.alive = False

class Weapon:
    def __init__(self, damage=WEAPON_DAMAGE, fire_rate=WEAPON_FIRE_RATE, range_=WEAPON_RANGE):
        self.damage = damage
        self.fire_rate = fire_rate
        self.range = range_
        self.cooldown = 0.0

    def update(self, dt):
        if self.cooldown > 0:
            self.cooldown -= dt

    def can_fire(self):
        return self.cooldown <= 0

    def fire(self, player, enemies, map_grid):
        """Hitscan straight down the player's view direction.
        Returns the enemy hit (or None) and clears cooldown."""
        if not self.can_fire():
            return None
        self.cooldown = self.fire_rate
        ray_x, ray_y = player.x, player.y
        step = 0.05
        for _ in range(int(self.range / step)):
            ray_x += math.cos(player.angle) * step
            ray_y += math.sin(player.angle) * step
            if is_wall(map_grid, ray_x, ray_y):
                return None
            for enemy in enemies:
                if not enemy.alive:
                    continue
                if math.hypot(enemy.x - ray_x, enemy.y - ray_y) < 0.35:
                    enemy.take_damage(self.damage)
                    return enemy
        return None

class Raycaster:
    """Computes wall distances for NUM_RAYS across the FOV.
    Returns a list of (perp_distance, side, map_x, map_y, ray_angle) —
    the renderer turns that into wall-slice heights/shading."""
    def __init__(self, map_grid):
        self.map_grid = map_grid

    def cast(self, player):
        results = []
        start_angle = player.angle - HALF_FOV
        for i in range(NUM_RAYS):
            angle = start_angle + i * DELTA_ANGLE
            depth = self._cast_single(player.x, player.y, angle)
            # fix fisheye distortion
            perp_depth = depth * math.cos(angle - player.angle)
            results.append(max(perp_depth, 0.0001))
        return results

    def _cast_single(self, x, y, angle):
        sin_a = math.sin(angle)
        cos_a = math.cos(angle)
        depth = 0.0
        step = 0.02
        while depth < MAX_DEPTH:
            depth += step
            test_x = x + cos_a * depth
            test_y = y + sin_a * depth
            if is_wall(self.map_grid, test_x, test_y):
                return depth
        return MAX_DEPTH

class GameEngine:
    """Top-level object Chaquopy (or the pygame renderer) talks to.
    Call update(...) once per frame, then read player/enemies/raycaster
    state to draw the frame on whichever platform you're on."""
    def __init__(self, map_grid=None):
        self.map = map_grid or GAME_MAP
        self.player = Player()
        self.weapon = Weapon()
        self.raycaster = Raycaster(self.map)
        self.enemies = [Enemy(8.5, 3.5), Enemy(12.5, 7.5), Enemy(4.5, 8.5)]
        self.score = 0

    def update(self, forward=0, strafe=0, d_angle=0.0, firing=False, dt=1.0/60):
        self.player.move(self.map, forward, strafe, dt)
        self.player.rotate(d_angle)
        self.weapon.update(dt)
        for enemy in self.enemies:
            state = enemy.update(self.map, self.player, dt)
            if state == "attack":
                self.player.take_damage(ENEMY_ATTACK_DAMAGE * dt)
        if firing:
            hit = self.weapon.fire(self.player, self.enemies, self.map)
            if hit is not None and not hit.alive:
                self.score += 100
        return self.get_state()

    def get_state(self):
        """Plain-dict snapshot — this is what you'd JSON-serialize or
        pass across the Chaquopy boundary to Kotlin each frame."""
        return {
            "player": {
                "x": self.player.x,
                "y": self.player.y,
                "angle": self.player.angle,
                "health": self.player.health,
                "alive": self.player.alive
            },
            "enemies": [
                {"x": e.x, "y": e.y, "health": e.health, "alive": e.alive}
                for e in self.enemies
            ],
            "score": self.score,
        }
