package com.ppam.eyeblinkactions.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ppam.eyeblinkactions.camera.CameraPreview
import com.ppam.eyeblinkactions.permissions.RequestCameraPermission
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.runtime.*
import com.ppam.eyeblinkactions.permissions.RequestCallPermission
import androidx.compose.material3.*

@Composable
fun EyeBlinkApp() {
    val lifecycleOwner = LocalLifecycleOwner.current
    LocalContext.current

    var blinkCount by remember { mutableStateOf(0) }
    var isCameraPermissionGranted by remember { mutableStateOf(false) }
    var isCallPermissionGranted by remember { mutableStateOf(false) }

    RequestCameraPermission { granted -> isCameraPermissionGranted = granted }
    RequestCallPermission { granted -> isCallPermissionGranted = granted }

    Column(modifier = Modifier.Companion.fillMaxSize()) {
        if (isCameraPermissionGranted) {
            CameraPreview(lifecycleOwner) { detectedBlinks ->
                blinkCount = detectedBlinks
            }
            Text(
                text = "Blinks Detected: $blinkCount",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.Companion.padding(16.dp)
            )
        } else {
            Text(
                text = "Camera permission required!",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.Companion.padding(16.dp)
            )
        }
    }
}