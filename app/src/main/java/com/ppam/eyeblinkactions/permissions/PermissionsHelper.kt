package com.ppam.eyeblinkactions.permissions

import android.Manifest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

@Composable
fun RequestCameraPermission(onPermissionResult: (Boolean) -> Unit) {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        onPermissionResult(it)
    }
    LaunchedEffect(Unit) { launcher.launch(Manifest.permission.CAMERA) }
}

@Composable
fun RequestCallPermission(onPermissionResult: (Boolean) -> Unit) {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        onPermissionResult(it)
    }
    LaunchedEffect(Unit) { launcher.launch(Manifest.permission.CALL_PHONE) }
}