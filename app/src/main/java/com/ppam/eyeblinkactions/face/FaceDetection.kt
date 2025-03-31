package com.ppam.eyeblinkactions.face

import android.content.Context
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.*
import com.ppam.eyeblinkactions.actions.handleBlinkAction

private var lastBlinkTime = 0L
private var blinkCounter = 0

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@OptIn(ExperimentalGetImage::class)
fun processImage(context: Context, imageProxy: ImageProxy, onBlinkDetected: (Int) -> Unit) {
    val mediaImage = imageProxy.image ?: return
    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

    val detectorOptions = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .enableTracking()
        .build()

    val detector = FaceDetection.getClient(detectorOptions)

    detector.process(image)
        .addOnSuccessListener { faces ->
            for (face in faces) {
                detectBlink(context, face, onBlinkDetected)
            }
        }
        .addOnCompleteListener { imageProxy.close() }
}

private fun detectBlink(context: Context, face: Face, onBlinkDetected: (Int) -> Unit) {
    val leftEyeOpenProb = face.leftEyeOpenProbability ?: 1.0f
    val rightEyeOpenProb = face.rightEyeOpenProbability ?: 1.0f
    val currentTime = System.currentTimeMillis()

    if (leftEyeOpenProb < 0.2f && rightEyeOpenProb < 0.2f) {
        if (currentTime - lastBlinkTime > 300) {
            blinkCounter++
            lastBlinkTime = currentTime

            handleBlinkAction(context, blinkCounter)
            onBlinkDetected(blinkCounter)
        }
    } else {
        if (currentTime - lastBlinkTime > 2000) {
            blinkCounter = 0
            onBlinkDetected(blinkCounter)
        }
    }
}

