package com.ppam.eyeblinkactions.service
import android.content.Context
import android.content.Context.POWER_SERVICE
import android.os.PowerManager
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat.getSystemService

private var wakeLock: PowerManager.WakeLock? = null
private val handler = Handler(Looper.getMainLooper())

//
///**
// * Acquires a Wake Lock to keep the CPU active in sleep mode.
// */
internal fun acquireWakeLock(context: Context) {
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "EyeBlink:WakeLock")

    wakeLock?.acquire(10 * 60 * 1000L /* 10 minutes */)  // ✅ Set a timeout
}

/**
 * Releases the Wake Lock when the activity is destroyed.
 */
internal fun releaseWakeLock() {
    wakeLock?.let {
        if (it.isHeld) {
            it.release()
        }
    }
}

fun releaseHandler() {
    handler.removeCallbacksAndMessages(null)
}