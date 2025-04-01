package com.ppam.eyeblinkactions

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.PowerManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.Modifier
import android.Manifest
import android.content.Context.POWER_SERVICE
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.ppam.eyeblinkactions.service.BlinkDetectionService
import com.ppam.eyeblinkactions.ui.EyeBlinkApp
import com.ppam.eyeblinkactions.ui.theme.EyeBlinkActionsTheme
import com.ppam.eyeblinkactions.worker.BlinkWorker
import com.ppam.eyeblinkactions.actions.stopRingtone
import com.ppam.eyeblinkactions.service.releaseHandler
import com.ppam.eyeblinkactions.service.releaseWakeLock
import android.net.Uri
import android.provider.Settings
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.ContextCompat.startForegroundService


import java.util.concurrent.TimeUnit


class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request necessary permissions (Camera, Foreground Service, etc.)
        requestPermissions()

        // Check & Request Battery Optimization Permission
        if (!isIgnoringBatteryOptimizations()) {
            requestBatteryOptimizationPermission()
        }

        // Acquire Wake Lock to keep CPU running when the screen is off
        acquireWakeLock()

        // Start Foreground Service for eye blink detection
        startBlinkDetectionService()

        // Schedule periodic background work using WorkManager
        scheduleBlinkDetectionWork()

        // Launch UI
        setContent {
            EyeBlinkApp()
        }
    }

    // Function to check if the app is ignoring battery optimizations
    @RequiresApi(Build.VERSION_CODES.M)
    private fun isIgnoringBatteryOptimizations(): Boolean {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(packageName)
    }

    // Function to request battery optimization exemption
    private fun requestBatteryOptimizationPermission() {
        try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:$packageName")
            }
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }



    override fun onDestroy() {
        super.onDestroy()
        stopRingtone()
        releaseWakeLock()
        releaseHandler()
        WorkManager.getInstance(this).cancelAllWorkByTag("blink_detection")
    }

    /**
     * Requests all necessary permissions from the user.
     */
    private fun requestPermissions() {
        val requiredPermissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.POST_NOTIFICATIONS
        )

        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missingPermissions.toTypedArray(), 100)
        }
    }

    /**
     * Starts the Foreground Service for continuous blink detection.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun startBlinkDetectionService() {
        val serviceIntent = Intent(this, BlinkDetectionService::class.java)
        startForegroundService(serviceIntent)  // Start as foreground service

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            startForegroundService(serviceIntent)  // Start as foreground service
        } else {
            startService(serviceIntent)
        }
    }

    /**
     * Acquires a Wake Lock to keep the CPU active in sleep mode.
     */
    private fun acquireWakeLock() {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "EyeBlinkApp::WakeLockTag"
        )
        wakeLock.acquire(10 * 60 * 1000L) // Hold wake lock for 10 minutes
    }

    /**
     * Schedules periodic blink detection using WorkManager.
     */
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
}