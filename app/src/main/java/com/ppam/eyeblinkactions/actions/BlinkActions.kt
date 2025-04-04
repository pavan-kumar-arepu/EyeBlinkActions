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


private fun playRingtone(context: Context) {
    Log.d("Ringtone", "Playing ringtone")

    if (ringtone == null) {
        ringtone = MediaPlayer.create(context, R.raw.templebells).apply {
            isLooping = true
            setOnCompletionListener {
                stopRingtone() // Ensure ringtone stops properly
            }
            start()
        }
        Log.d("Ringtone", "Playing ringtone")
        isRingtonePlaying = true
    }
}


 fun stopRingtone() {
    ringtone?.apply {
        if (isPlaying) {
            stop()
            release()  // Releases the MediaPlayer to free memory
        }
    }
    ringtone = null  // Avoid holding unnecessary references
    isRingtonePlaying = false
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