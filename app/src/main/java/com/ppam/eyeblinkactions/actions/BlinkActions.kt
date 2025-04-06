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

/*
fun handleBlinkAction(context: Context, blinkCounter: Int) {
    if (blinkCounter == 2) {
        if (isRingtonePlaying) {
            showBlinkAlert(context, blinkCounter, "Stop Playing Bell \uD83D\uDD14")
            stopRingtone()
        } else {
            showBlinkAlert(context, blinkCounter, " Playing Bell \uD83D\uDD14")
            playRingtone(context)
        }
    }
}

// Keep this function right below detectBlink()
private fun showBlinkAlert(context: Context, blinkCount: Int, message: String) {
    Log.d("BLINK_ALERT", "Showing toast: $blinkCount, $message") // Debug Log
    Handler(Looper.getMainLooper()).post {
        Toast.makeText(
            context,
            "Blinks detected: $blinkCount\n$message",
            Toast.LENGTH_LONG
        ).show()
    }
}


//private fun playRingtone(context: Context) {
//    Log.d("Ringtone", "Playing ringtone")
//
//    if (ringtone == null) {
//        ringtone = MediaPlayer.create(context, R.raw.templebells).apply {
//            isLooping = true
//            setOnCompletionListener {
//                stopRingtone() // Ensure ringtone stops properly
//            }
//            start()
//        }
//        Log.d("Ringtone", "Playing ringtone")
//        isRingtonePlaying = true
//    }
//}
//
//
// fun stopRingtone() {
//    ringtone?.apply {
//        if (isPlaying) {
//            stop()
//            release()  // Releases the MediaPlayer to free memory
//        }
//    }
//    ringtone = null  // Avoid holding unnecessary references
//    isRingtonePlaying = false
//}

private var audioManager: AudioManager? = null
private var focusRequest: AudioFocusRequest? = null

private fun playRingtone(context: Context) {
    Log.d("Ringtone", "Attempting to play ringtone")

    if (ringtone == null) {
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAcceptsDelayedFocusGain(false)
            .setOnAudioFocusChangeListener { focusChange ->
                Log.d("AudioFocus", "Focus changed: $focusChange")
            }
            .build()

        val result = audioManager?.requestAudioFocus(focusRequest!!)
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            ringtone = MediaPlayer.create(context, R.raw.templebells)?.apply {
                isLooping = true
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setOnCompletionListener {
                    stopRingtone()
                }
                start()
                isRingtonePlaying = true
                Log.d("Ringtone", "Ringtone started successfully")
            }
        } else {
            Log.w("Ringtone", "Audio focus not granted")
        }
    }
}

fun stopRingtone() {
    ringtone?.apply {
        if (isPlaying) {
            stop()
            release()
        }
    }
    ringtone = null
    isRingtonePlaying = false

    focusRequest?.let {
        audioManager?.abandonAudioFocusRequest(it)
    }
    audioManager = null
    focusRequest = null
}



private fun makePhoneCall(context: Context, phoneNumber: String) {
    val intent = Intent(Intent.ACTION_CALL).apply {
        data = "tel:$phoneNumber".toUri()
    }
    context.startActivity(intent)
}

private val handler = Handler(Looper.getMainLooper())

private fun releaseHandler() {
    handler.removeCallbacksAndMessages(null)
}

 */


object BlinkActionHandler {

    private var soundPool: SoundPool? = null
    private var bellSoundId: Int = 0
    private var isBellLoaded = false
    private var isBellPlaying = false

    private val handler = Handler(Looper.getMainLooper())

    fun handleBlinkAction(context: Context, blinkCounter: Int) {
        if (blinkCounter == 2) {
            if (!isBellLoaded) initSoundPool(context)

            if (isBellPlaying) {
                showBlinkAlert(context, blinkCounter, "Stop Playing Bell 🔔")
                stopBell()
            } else {
                showBlinkAlert(context, blinkCounter, "Playing Bell 🔔")
                playBell()
            }
        }
        // You can extend this for other blink actions like 3 blinks → call etc.
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

        bellSoundId = soundPool!!.load(context, R.raw.templebells, 1)
        soundPool?.setOnLoadCompleteListener { _, _, status ->
            isBellLoaded = status == 0
            Log.d("SoundPool", "Bell loaded: $isBellLoaded")
        }
    }

    private fun playBell() {
        if (isBellLoaded) {
            soundPool?.play(bellSoundId, 1f, 1f, 1, -1, 1f) // -1 → loop indefinitely
            isBellPlaying = true
            Log.d("SoundPool", "Bell playing")
        } else {
            Log.w("SoundPool", "Bell not loaded yet")
        }
    }

    private fun stopBell() {
        soundPool?.stop(bellSoundId)
        isBellPlaying = false
        Log.d("SoundPool", "Bell stopped")
    }

    private fun showBlinkAlert(context: Context, blinkCount: Int, message: String) {
        Log.d("BLINK_ALERT", "Showing toast: $blinkCount, $message")
        handler.post {
            Toast.makeText(
                context,
                "Blinks detected: $blinkCount\n$message",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        isBellLoaded = false
        isBellPlaying = false
        handler.removeCallbacksAndMessages(null)
    }
}