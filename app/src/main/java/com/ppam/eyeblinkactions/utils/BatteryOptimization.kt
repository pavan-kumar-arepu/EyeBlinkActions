package com.ppam.eyeblinkactions.utils

import android.content.Context
import android.content.Intent
import android.provider.Settings

fun requestBatteryOptimization(context: Context) {
    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
    context.startActivity(intent)
}