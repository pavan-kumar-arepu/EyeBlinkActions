package com.ppam.eyeblinkactions.service


import android.app.*
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.ppam.eyeblinkactions.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
//import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.PowerManager
import android.util.Log
import com.ppam.eyeblinkactions.actions.handleBlinkAction
import android.provider.Settings
import android.net.Uri
import androidx.annotation.RequiresApi
import androidx.core.app.ServiceCompat.startForeground
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.ContextCompat.startActivity
import com.ppam.eyeblinkactions.camera.CameraManager
import java.util.concurrent.TimeUnit
import androidx.lifecycle.LifecycleService
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.ppam.eyeblinkactions.camera.CameraManager.stopBlinkDetection
import com.ppam.eyeblinkactions.worker.BlinkWorker

class BlinkDetectionService : LifecycleService() {

    private lateinit var wakeLock: PowerManager.WakeLock

    override fun onCreate() {
        super.onCreate()

        // Acquire WakeLock to keep CPU active
        acquireWakeLock()

        // Create notification for foreground service
        createNotificationChannel()
        startForeground(1, createNotification())

        // ✅ Ask to ignore battery optimizations (if not already granted)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = getSystemService(POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:$packageName")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK  // Required for starting from service
                }
                startActivity(intent)
            }
        }

        // ✅ Start Blink Detection when the service starts
        startBlinkDetection()

        scheduleBlinkDetectionWork() // Schedule the periodic task

    }


    // Function to schedule periodic work
    private fun scheduleBlinkDetectionWork() {
        val workRequest = PeriodicWorkRequestBuilder<BlinkWorker>(
            15, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "BlinkDetectionWork",
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)  // Call the superclass method
        startForeground(1, createNotification())  // Call this early
        startBlinkDetection()
        return START_STICKY
    }

    private fun startBlinkDetection() {
        Log.d("BlinkDetectionService", "Starting Camera-based Blink Detection")

        // ✅ Pass 'this' as LifecycleOwner because LifecycleService supports it
        CameraManager.startBlinkDetection(this, this) { blinkCount ->
            handleBlinkAction(this, blinkCount)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopBlinkDetection() // Stop camera processing
        releaseWakeLock() // Release CPU WakeLock
        Log.d("BlinkDetectionService", "Blink detection stopped, service destroyed")
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "EyeBlink:WakeLock")
        wakeLock.acquire()
    }

    private fun releaseWakeLock() {
        if (::wakeLock.isInitialized && wakeLock.isHeld) {
            wakeLock.release()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "BlinkServiceChannel",
                "Blink Detection Service",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, "BlinkServiceChannel")
            .setContentTitle("Blink Detection Running")
            .setContentText("Detecting blinks in the background...")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .build()
    }
}