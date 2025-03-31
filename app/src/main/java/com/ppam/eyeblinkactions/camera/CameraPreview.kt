package com.ppam.eyeblinkactions.camera

import android.content.Context
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import androidx.core.content.ContextCompat
import com.ppam.eyeblinkactions.face.processImage

@Composable
fun CameraPreview(
    lifecycleOwner: LifecycleOwner,
    onBlinkDetected: (Int) -> Unit
) {
    val context = LocalContext.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val previewView = remember { androidx.camera.view.PreviewView(context) }

    AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize()) { view ->
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(view.surfaceProvider)
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                processImage(context, imageProxy, onBlinkDetected)
            }

            try {
                cameraProvider.unbindAll() // Unbind previous instances before rebinding
                cameraProvider.bindToLifecycle(
                    lifecycleOwner, cameraSelector, preview, imageAnalysis
                )
            } catch (exc: Exception) {
                Log.e("CameraPreview", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(context))
    }
}

object CameraManager {

    private var cameraProvider: ProcessCameraProvider? = null

    fun startBlinkDetection(context: Context, lifecycleOwner: LifecycleOwner?, onBlinkDetected: (Int) -> Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build()

            val preview = Preview.Builder().build()
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                processImage(context, imageProxy, onBlinkDetected)
            }

            cameraProvider?.unbindAll()

            // ✅ Pass lifecycleOwner if available
            if (lifecycleOwner != null) {
                cameraProvider?.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalysis)
            } else {
                Log.e("CameraManager", "LifecycleOwner is null. Cannot start camera.")
            }

        }, ContextCompat.getMainExecutor(context))
    }

    fun stopBlinkDetection() {
        cameraProvider?.unbindAll()  // Stops the camera when service stops
    }
}