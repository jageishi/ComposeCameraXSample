package com.example.composecameraxsample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.composecameraxsample.ui.CameraPreviewScreen
import com.example.composecameraxsample.ui.theme.ComposeCameraXSampleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeCameraXSampleTheme {
                CameraPreviewScreen()
            }
        }
    }
}
