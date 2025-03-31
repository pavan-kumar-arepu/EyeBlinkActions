package com.ppam.eyeblinkactions.service
import android.content.Context
import android.os.PowerManager
import android.os.Handler
import android.os.Looper

private var wakeLock: PowerManager.WakeLock? = null
private val handler = Handler(Looper.getMainLooper())

internal fun acquireWakeLock(context: Context) {
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "EyeBlink:WakeLock")

    wakeLock?.acquire(10 * 60 * 1000L /* 10 minutes */)  // ✅ Set a timeout
}

fun releaseWakeLock() {
    wakeLock?.let {
        if (it.isHeld) it.release()
    }
    wakeLock = null
}

fun releaseHandler() {
    handler.removeCallbacksAndMessages(null)
}