"""
AEGIS FPS ENGINE - Desktop Renderer
This file is ONLY rendering + input. All game logic lives in engine.py
(shared with the Android/Chaquopy build).
Run: python3 renderer_pygame.py
"""
import math
import pygame
from settings import (
    WIDTH, HEIGHT, HALF_HEIGHT, FPS,
    NUM_RAYS, SCALE, SCREEN_DIST,
    FLOOR_COLOR, CEIL_COLOR, BLACK, WHITE,
    RED, GREEN, DARKGRAY,
    PLAYER_ROT_SPEED, MAX_DEPTH,
)
from engine import GameEngine

def wall_shade(depth):
    # closer walls brighter, far walls darker, clamped
    brightness = max(30, 255 - int(depth * 12))
    return (brightness, brightness, brightness)

def draw_frame(screen, game: GameEngine):
    screen.fill(CEIL_COLOR)
    pygame.draw.rect(screen, FLOOR_COLOR, (0, HALF_HEIGHT, WIDTH, HALF_HEIGHT))
    depths = game.raycaster.cast(game.player)
    for i, depth in enumerate(depths):
        depth = min(depth, MAX_DEPTH)
        wall_height = min(int(SCREEN_DIST / (depth + 0.0001)), HEIGHT * 3)
        color = wall_shade(depth)
        x = i * SCALE
        pygame.draw.rect(
            screen, color,
            (x, HALF_HEIGHT - wall_height // 2, SCALE + 1, wall_height)
        )
    # crude enemy sprites: draw as colored circles sized by distance
    for enemy in game.enemies:
        if not enemy.alive:
            continue
        dx = enemy.x - game.player.x
        dy = enemy.y - game.player.y
        dist = math.hypot(dx, dy)
        angle_to = math.atan2(dy, dx) - game.player.angle
        angle_to = (angle_to + math.pi) % (2 * math.pi) - math.pi
        if abs(angle_to) < 0.6 and dist > 0.2:  # roughly in view
            screen_x = int(WIDTH / 2 + math.tan(angle_to) * SCREEN_DIST)
            size = max(4, int(300 / dist))
            pygame.draw.circle(screen, RED, (screen_x, HALF_HEIGHT), size)

    # HUD
    font = pygame.font.SysFont("arial", 22)
    hud = f"HP: {int(game.player.health)}   Score: {game.score}   " \
          f"Enemies left: {sum(1 for e in game.enemies if e.alive)}"
    screen.blit(font.render(hud, True, WHITE), (10, HEIGHT - 30))
    if not game.player.alive:
        over = font.render("YOU DIED — press ESC to quit", True, RED)
        screen.blit(over, (WIDTH // 2 - 150, HALF_HEIGHT))
    # simple weapon indicator (crosshair)
    pygame.draw.line(screen, WHITE, (WIDTH // 2 - 10, HALF_HEIGHT), (WIDTH // 2 + 10, HALF_HEIGHT), 2)
    pygame.draw.line(screen, WHITE, (WIDTH // 2, HALF_HEIGHT - 10), (WIDTH // 2, HALF_HEIGHT + 10), 2)

def main():
    pygame.init()
    screen = pygame.display.set_mode((WIDTH, HEIGHT))
    pygame.display.set_caption("AEGIS FPS ENGINE — prototype")
    clock = pygame.time.Clock()
    pygame.mouse.set_visible(False)
    pygame.event.set_grab(True)
    game = GameEngine()
    running = True
    while running:
        dt = clock.tick(FPS) / 1000.0
        forward = strafe = 0
        d_angle = 0.0
        firing = False
        for event in pygame.event.get():
            if event.type == pygame.QUIT:
                running = False
            if event.type == pygame.KEYDOWN and event.key == pygame.K_ESCAPE:
                running = False
            if event.type == pygame.MOUSEBUTTONDOWN and event.button == 1:
                firing = True
            if event.type == pygame.MOUSEMOTION:
                d_angle += event.rel[0] * 0.002
        keys = pygame.key.get_pressed()
        if keys[pygame.K_w]:
            forward += 1
        if keys[pygame.K_s]:
            forward -= 1
        if keys[pygame.K_d]:
            strafe += 1
        if keys[pygame.K_a]:
            strafe -= 1
        if keys[pygame.K_LEFT]:
            d_angle -= PLAYER_ROT_SPEED * dt
        if keys[pygame.K_RIGHT]:
            d_angle += PLAYER_ROT_SPEED * dt
        if keys[pygame.K_SPACE]:
            firing = True
        if game.player.alive:
            game.update(forward, strafe, d_angle, firing, dt)
        draw_frame(screen, game)
        pygame.display.flip()
    pygame.quit()

if __name__ == "__main__":
    main()
