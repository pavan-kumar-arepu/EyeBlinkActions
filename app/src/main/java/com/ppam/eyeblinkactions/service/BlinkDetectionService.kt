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
import android.content.Context.POWER_SERVICE
//import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.PowerManager
import android.util.Log
//import com.ppam.eyeblinkactions.actions.handleBlinkAction
import android.provider.Settings
import android.net.Uri
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ServiceCompat.startForeground
import androidx.core.content.ContextCompat
import androidx.camera.core.Preview
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.ContextCompat.startActivity
import com.ppam.eyeblinkactions.camera.CameraManager
import java.util.concurrent.TimeUnit
import androidx.lifecycle.LifecycleService
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.ppam.eyeblinkactions.actions.BlinkActionHandler
import com.ppam.eyeblinkactions.actions.BlinkActionHandler.handleBlinkAction
import com.ppam.eyeblinkactions.camera.CameraManager.stopBlinkDetection
import com.ppam.eyeblinkactions.worker.BlinkWorker
import com.ppam.eyeblinkactions.face.BlinkAnalyzer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


/*
class BlinkDetectionService : LifecycleService() {

    private lateinit var wakeLock: PowerManager.WakeLock


    override fun onCreate() {
        super.onCreate()

        // Acquire wake lock
        acquireWakeLock(applicationContext)

        // Notification Channel + Foreground Notification
        createNotificationChannel()
        startForeground(1, createNotification())

        // Start camera for blink detection
        startCameraForBlinkDetection()

        // Ask to ignore battery optimization
        askBatteryOptimizationExemption()

        // Schedule periodic work
        scheduleBlinkDetectionWork()
    }

    private fun askBatteryOptimizationExemption() {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:$packageName")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
        }
    }
    private fun startCameraForBlinkDetection() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(applicationContext)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build()
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(
                        Executors.newSingleThreadExecutor(),
                        BlinkAnalyzer(applicationContext) { blinkCount ->
                            Log.d("BlinkService", "Blink Count: $blinkCount")
                            // Handle actions here
                        }
                    )
                }

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer
                )
            } catch (exc: Exception) {
                Log.e("BlinkService", "Camera binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(applicationContext))
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

    private fun releaseWakeLock() {
        if (::wakeLock.isInitialized && wakeLock.isHeld) {
            wakeLock.release()
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, "blink_channel")
            .setContentTitle("Eye Blink Detection Active")
            .setContentText("Running in background")
            .setSmallIcon(R.drawable.ic_notification) // 👈 This must be a valid drawable
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "blink_channel",
                "Blink Detection",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Foreground service for detecting eye blinks"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}

 */

class BlinkDetectionService : LifecycleService() {

    private lateinit var wakeLock: PowerManager.WakeLock
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    override fun onCreate() {
        super.onCreate()

        acquireWakeLock(applicationContext)
        createNotificationChannel()
        startForeground(1, createNotification())

        askBatteryOptimizationExemption()
        startCameraInBackground()
        scheduleBlinkDetectionWork()
    }

    private fun askBatteryOptimizationExemption() {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:$packageName")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
        }
    }

    private fun startCameraInBackground() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(applicationContext)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build()
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, BlinkAnalyzer(applicationContext) { blinkCount ->
                        Log.d("BlinkService", "Blink Count: $blinkCount")
//                        handleBlinkAction(applicationContext, blinkCount)
                        BlinkActionHandler.handleBlinkAction(applicationContext, blinkCount)
                    })
                }

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer
                )
            } catch (exc: Exception) {
                Log.e("BlinkService", "Camera binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(applicationContext))
    }

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
        super.onStartCommand(intent, flags, startId)
        startForeground(1, createNotification())
        startBlinkDetection()
        return START_STICKY
    }

    private fun startBlinkDetection() {
        Log.d("BlinkDetectionService", "Starting Camera-based Blink Detection")
        CameraManager.startBlinkDetection(this, this) { blinkCount ->
//            handleBlinkAction(this, blinkCount)
            BlinkActionHandler.handleBlinkAction(applicationContext, blinkCount)

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopBlinkDetection()
        releaseWakeLock()
        cameraExecutor.shutdown()
        Log.d("BlinkDetectionService", "Blink detection stopped, service destroyed")
    }

    private fun releaseWakeLock() {
        if (::wakeLock.isInitialized && wakeLock.isHeld) {
            wakeLock.release()
        }
    }

    private fun acquireWakeLock(context: Context) {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "BlinkDetection::WakeLock"
        ).apply { acquire() }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, "blink_channel")
            .setContentTitle("Eye Blink Detection Active")
            .setContentText("Running in background")
            .setSmallIcon(R.drawable.ic_notification) // 👈 Ensure this icon exists
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "blink_channel",
                "Blink Detection",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Foreground service for detecting eye blinks"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}
