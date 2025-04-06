package com.ppam.eyeblinkactions.face

import android.content.Context
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy

class BlinkAnalyzer(
    private val context: Context,
    private val onBlinkDetected: (Int) -> Unit
) : ImageAnalysis.Analyzer {

    override fun analyze(image: ImageProxy) {
        processImage(context, image, onBlinkDetected)
    }
}