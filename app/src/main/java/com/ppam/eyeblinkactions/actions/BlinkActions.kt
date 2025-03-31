package com.ppam.eyeblinkactions.actions

import android.content.Intent


import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import com.ppam.eyeblinkactions.R
import android.widget.Toast
import android.Manifest
import android.os.Looper
import android.util.Log
import android.os.Handler


private var ringtone: MediaPlayer? = null
private var isRingtonePlaying = false

private var mediaPlayer: MediaPlayer? = null

/*
fun handleBlinkAction(context: Context, blinkCount: Int) {
    when (blinkCount) {
        2 -> {
            if (isRingtonePlaying) {
                Log.d("BLINK_ACTION", "Stopping ringtone")
                showBlinkAlert(context, blinkCount, "Stop Playing Bell")
                stopRingtone()
            } else {
                Log.d("BLINK_ACTION", "Playing ringtone")
                showBlinkAlert(context, blinkCount, "Playing Bell")
                playRingtone(context)
            }
        }
//        3 -> {
//            showBlinkAlert(context, blinkCount, "Making a Phone Call")
//            makePhoneCall(context, "8121040308")
//        }
    }
}
*/

fun handleBlinkAction(context: Context, blinkCounter: Int) {
    if (blinkCounter == 2) {
        if (isRingtonePlaying) {
            showBlinkAlert(context, blinkCounter, "Stop Playing Bell")
            stopRingtone()
        } else {
            showBlinkAlert(context, blinkCounter, "Playing Bell")
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

private fun stopRingtone() {
    ringtone?.let {
        if (it.isPlaying) {
            it.stop()
            it.release()
        }
    }
    ringtone = null
    isRingtonePlaying = false // Ensure flag is updated
    Log.d("Ringtone", "Stopped ringtone")
}

private fun makePhoneCall(context: Context, phoneNumber: String) {
    val intent = Intent(Intent.ACTION_CALL).apply {
        data = Uri.parse("tel:$phoneNumber")
    }
    context.startActivity(intent)
}
