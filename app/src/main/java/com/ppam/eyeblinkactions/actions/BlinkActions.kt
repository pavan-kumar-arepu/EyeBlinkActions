package com.ppam.eyeblinkactions.actions

import android.content.Intent


import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import com.ppam.eyeblinkactions.R
import android.widget.Toast
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import android.os.Looper
import android.util.Log
import android.os.Handler
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri

private var ringtone: MediaPlayer? = null
private var isRingtonePlaying = false
private var mediaPlayer: MediaPlayer? = null



object BlinkActionHandler {

    private var soundPool: SoundPool? = null
    private var bellSoundId: Int = 0
    private var streamId: Int = 0
    private var isBellLoaded = false
    private var isBellPlaying = false

    private val handler = Handler(Looper.getMainLooper())

    fun handleBlinkAction(context: Context, blinkCounter: Int) {
        if (blinkCounter == 2) {
            if (!isBellLoaded) {
                initSoundPool(context)
                showBlinkAlert(context, blinkCounter, "Bell is loading... 🔄")
                return
            }

            if (isBellPlaying) {
                showBlinkAlert(context, blinkCounter, "Stopping Bell 🔕")
                stopBell()
            } else {
                showBlinkAlert(context, blinkCounter, "Playing Bell 🔔")
                playBell()
            }
        }
    }

    private fun initSoundPool(context: Context) {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(audioAttributes)
            .build()

        soundPool?.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0 && sampleId == bellSoundId) {
                isBellLoaded = true
                Log.d("SoundPool", "Bell sound loaded successfully.")
            } else {
                Log.e("SoundPool", "Failed to load bell sound.")
            }
        }

        bellSoundId = soundPool!!.load(context.applicationContext, R.raw.templebells, 1)
    }

    private fun playBell() {
        if (!isBellLoaded) {
            Log.w("SoundPool", "Attempted to play before loading.")
            return
        }
        streamId = soundPool?.play(bellSoundId, 1f, 1f, 1, -1, 1f) ?: 0
        isBellPlaying = true
        Log.d("SoundPool", "Bell started (streamId=$streamId)")
    }

    private fun stopBell() {
        if (streamId != 0) {
            soundPool?.stop(streamId)
            Log.d("SoundPool", "Bell stopped (streamId=$streamId)")
        } else {
            Log.w("SoundPool", "No active stream to stop.")
        }
        isBellPlaying = false
        streamId = 0
    }

    private fun showBlinkAlert(context: Context, blinkCount: Int, message: String) {
        Log.d("BLINK_ALERT", "Showing toast: $blinkCount, $message")
        handler.post {
            Toast.makeText(
                context.applicationContext,
                "Blinks detected: $blinkCount\n$message",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        isBellLoaded = false
        isBellPlaying = false
        streamId = 0
        handler.removeCallbacksAndMessages(null)
    }
}