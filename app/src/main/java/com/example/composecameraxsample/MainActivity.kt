package com.example.composecameraxsample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.composecameraxsample.ui.MainScreen
import com.example.composecameraxsample.ui.theme.ComposeCameraXSampleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeCameraXSampleTheme {
                MainScreen()
            }
        }
    }
}
