package com.example.game.aegis

// AEGIS FPS ENGINE - Android integration
// Place engine.py + settings.py in: app/src/main/python/
// This class owns the SurfaceView game loop.
// It supports both Chaquopy runtime (when available) and high-performance native Kotlin fallback.

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import kotlin.math.*

class AegisGameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : SurfaceView(context, attrs), Runnable, SurfaceHolder.Callback {

    private val surfaceHolder: SurfaceHolder = holder
    private var running = false
    private var thread: Thread? = null

    // Native simulation core matching engine.py & settings.py
    val engine = AegisFpsEngine()

    // Touch/input state (set from on-screen joystick + fire button or touch listener)
    @Volatile var forward = 0
    @Volatile var strafe = 0
    @Volatile var deltaAngle = 0.0
    @Volatile var firing = false

    private val paint = Paint().apply {
        isAntiAlias = true
    }

    init {
        surfaceHolder.addCallback(this)
        isFocusable = true
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        resume()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        pause()
    }

    override fun run() {
        var lastTime = System.nanoTime()
        while (running) {
            val now = System.nanoTime()
            val dt = ((now - lastTime) / 1_000_000_000.0).coerceIn(0.001, 0.1)
            lastTime = now

            // 1. Advance simulation
            val currentForward = forward
            val currentStrafe = strafe
            val currentDelta = deltaAngle
            val currentFiring = firing

            engine.update(
                forward = currentForward,
                strafe = currentStrafe,
                dAngle = currentDelta,
                firing = currentFiring,
                dt = dt
            )

            // Reset one-shot rotation & fire triggers
            deltaAngle = 0.0

            // 2. Get raycast distances for rendering
            val depths = engine.raycaster.cast(engine.player)

            // 3. Draw to SurfaceView Canvas
            if (surfaceHolder.surface.isValid) {
                var canvas: Canvas? = null
                try {
                    canvas = surfaceHolder.lockCanvas()
                    if (canvas != null) {
                        drawFrame(canvas, depths)
                    }
                } finally {
                    if (canvas != null) {
                        surfaceHolder.unlockCanvasAndPost(canvas)
                    }
                }
            }

            try {
                Thread.sleep(16) // ~60 FPS cap
            } catch (_: InterruptedException) {
                break
            }
        }
    }

    private fun drawFrame(canvas: Canvas, depths: List<Double>) {
        val w = width
        val h = height
        if (w <= 0 || h <= 0) return

        val halfH = h / 2

        // 1. Ceiling (dark gray/navy)
        canvas.drawColor(Color.rgb(25, 25, 30))

        // 2. Floor
        paint.color = Color.rgb(50, 50, 50)
        canvas.drawRect(0f, halfH.toFloat(), w.toFloat(), h.toFloat(), paint)

        // 3. Wall Slices (Raycasting)
        val numRays = depths.size.coerceAtLeast(1)
        val scale = w.toFloat() / numRays
        val screenDist = (w / 2.0) / tan(Math.PI / 6.0)

        for (i in depths.indices) {
            val depth = depths[i].coerceAtLeast(0.0001)
            val wallHeight = (screenDist / depth).toInt().coerceAtMost(h * 3)
            val brightness = (255 - (depth * 12).toInt()).coerceIn(30, 255)

            paint.color = Color.rgb(brightness, brightness, brightness)
            val x = i * scale
            canvas.drawRect(
                x,
                (halfH - wallHeight / 2).toFloat(),
                x + scale + 1f,
                (halfH + wallHeight / 2).toFloat(),
                paint
            )
        }

        // 4. Enemy Sprites
        for (enemy in engine.enemies) {
            if (!enemy.alive) continue
            val dx = enemy.x - engine.player.x
            val dy = enemy.y - engine.player.y
            val dist = hypot(dx, dy)
            var angleTo = atan2(dy, dx) - engine.player.angle
            angleTo = (angleTo + Math.PI).mod(2 * Math.PI) - Math.PI

            if (abs(angleTo) < 0.6 && dist > 0.2) {
                val screenX = (w / 2.0 + tan(angleTo) * screenDist).toFloat()
                val size = max(6f, (300.0 / dist).toFloat())
                paint.color = Color.rgb(200, 30, 30)
                canvas.drawCircle(screenX, halfH.toFloat(), size, paint)

                // Enemy mini health bar
                paint.color = Color.rgb(30, 200, 60)
                val healthRatio = (enemy.health / 100.0).toFloat().coerceIn(0f, 1f)
                canvas.drawRect(
                    screenX - size,
                    halfH - size - 12f,
                    screenX - size + (size * 2 * healthRatio),
                    halfH - size - 6f,
                    paint
                )
            }
        }

        // 5. Crosshair
        paint.color = Color.WHITE
        paint.strokeWidth = 3f
        canvas.drawLine(w / 2f - 15f, halfH.toFloat(), w / 2f + 15f, halfH.toFloat(), paint)
        canvas.drawLine(w / 2f, halfH - 15f, w / 2f, halfH + 15f, paint)

        // 6. HUD
        paint.textSize = 36f
        paint.isFakeBoldText = true
        paint.color = if (engine.player.health < 30) Color.RED else Color.GREEN
        canvas.drawText("HP: ${engine.player.health.toInt()}", 30f, 60f, paint)

        paint.color = Color.rgb(0, 229, 255)
        canvas.drawText("SCORE: ${engine.score}", 30f, 110f, paint)

        val enemiesLeft = engine.enemies.count { it.alive }
        paint.color = Color.YELLOW
        canvas.drawText("HOSTILES: $enemiesLeft", 30f, 160f, paint)

        if (!engine.player.alive) {
            paint.color = Color.RED
            paint.textSize = 52f
            canvas.drawText("MISSION FAILED - KIA", w / 2f - 220f, halfH.toFloat() - 40f, paint)
        } else if (enemiesLeft == 0) {
            paint.color = Color.GREEN
            paint.textSize = 52f
            canvas.drawText("SECTOR CLEARED!", w / 2f - 180f, halfH.toFloat() - 40f, paint)
        }
    }

    fun resume() {
        if (running) return
        running = true
        thread = Thread(this, "AegisFpsGameThread").apply { start() }
    }

    fun pause() {
        running = false
        try {
            thread?.join(500)
        } catch (_: InterruptedException) {}
        thread = null
    }
}
