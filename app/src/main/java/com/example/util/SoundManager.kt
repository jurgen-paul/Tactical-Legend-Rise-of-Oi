package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.ToneGenerator
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * Tactical Cyberpunk Sound Manager Utility
 * Generates synthetic audio feedback for combat actions (Movement, Laser Attacks, Shielding, Hits, Victory/Defeat)
 * without requiring external audio asset files.
 */
object SoundManager {

    private const val TAG = "SoundManager"
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    var isSoundEnabled: Boolean = true

    private var toneGenerator: ToneGenerator? = try {
        ToneGenerator(AudioManager.STREAM_MUSIC, 80)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to initialize ToneGenerator", e)
        null
    }

    /**
     * Play gentle cyber step sound when moving a unit across the tactical grid.
     */
    fun playMoveSound() {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 40)
            } catch (e: Exception) {
                Log.e(TAG, "Error playing move sound", e)
            }
        }
    }

    /**
     * Play high-tech laser attack sound.
     */
    fun playAttackSound() {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                // High frequency burst tone
                toneGenerator?.startTone(ToneGenerator.TONE_DTMF_D, 120)
            } catch (e: Exception) {
                // Fallback tone play
                playSynthSweep(startFreq = 880f, endFreq = 220f, durationMs = 120)
            }
        }
    }

    /**
     * Play tactical ability execution sound.
     */
    fun playAbilitySound() {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_SUP_PIP, 100)
            } catch (e: Exception) {
                playSynthSweep(startFreq = 440f, endFreq = 880f, durationMs = 150)
            }
        }
    }

    /**
     * Play nanite regen or kinetic shield powerup sound.
     */
    fun playShieldHealSound() {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_DTMF_0, 80)
                kotlinx.coroutines.delay(60)
                toneGenerator?.startTone(ToneGenerator.TONE_DTMF_A, 100)
            } catch (e: Exception) {
                playSynthSweep(startFreq = 300f, endFreq = 600f, durationMs = 140)
            }
        }
    }

    /**
     * Play damage impact thud sound.
     */
    fun playHitSound() {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_LOW_L, 90)
            } catch (e: Exception) {
                playSynthSweep(startFreq = 200f, endFreq = 80f, durationMs = 100)
            }
        }
    }

    /**
     * Play victory fanfare audio chime.
     */
    fun playVictorySound() {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                val notes = listOf(
                    ToneGenerator.TONE_DTMF_1 to 100L,
                    ToneGenerator.TONE_DTMF_3 to 100L,
                    ToneGenerator.TONE_DTMF_5 to 100L,
                    ToneGenerator.TONE_DTMF_A to 250L
                )
                for ((tone, duration) in notes) {
                    toneGenerator?.startTone(tone, duration.toInt())
                    kotlinx.coroutines.delay(duration + 20)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error playing victory sound", e)
            }
        }
    }

    /**
     * Play defeat alarm sound.
     */
    fun playDefeatSound() {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                val notes = listOf(
                    ToneGenerator.TONE_DTMF_B to 150L,
                    ToneGenerator.TONE_DTMF_9 to 150L,
                    ToneGenerator.TONE_DTMF_7 to 300L
                )
                for ((tone, duration) in notes) {
                    toneGenerator?.startTone(tone, duration.toInt())
                    kotlinx.coroutines.delay(duration + 20)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error playing defeat sound", e)
            }
        }
    }

    /**
     * Play UI button click feedback.
     */
    fun playClickSound() {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 30)
            } catch (e: Exception) {
                Log.e(TAG, "Error playing click sound", e)
            }
        }
    }

    /**
     * Synthesize custom frequency tone sweep using AudioTrack.
     */
    private fun playSynthSweep(startFreq: Float, endFreq: Float, durationMs: Int) {
        try {
            val sampleRate = 22050
            val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
            val sample = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val progress = i.toFloat() / numSamples
                val currentFreq = startFreq + (endFreq - startFreq) * progress
                val time = i.toDouble() / sampleRate
                val angle = 2.0 * Math.PI * currentFreq * time
                // Envelope decay to prevent harsh clipping
                val envelope = 1.0 - progress
                val value = (sin(angle) * 32767 * envelope).toInt().coerceIn(-32768, 32767)
                sample[i] = value.toShort()
            }

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(sample.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(sample, 0, sample.size)
            audioTrack.play()
            scope.launch {
                kotlinx.coroutines.delay(durationMs.toLong() + 50)
                audioTrack.release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "AudioTrack synth failed", e)
        }
    }
}
