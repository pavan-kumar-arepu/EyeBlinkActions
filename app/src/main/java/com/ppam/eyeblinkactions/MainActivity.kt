package com.ppam.eyeblinkactions

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.Modifier
import com.ppam.eyeblinkactions.ui.EyeBlinkApp
import com.ppam.eyeblinkactions.ui.theme.EyeBlinkActionsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EyeBlinkApp()
        }
    }
}