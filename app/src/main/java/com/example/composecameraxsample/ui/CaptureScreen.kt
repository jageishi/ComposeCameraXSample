package com.example.composecameraxsample.ui

import android.util.Log
import android.view.Surface
import android.widget.FrameLayout
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.composecameraxsample.ui.theme.ComposeCameraXSampleTheme

@Composable
fun CaptureScreen(
    imageCapture: ImageCapture
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val isPreview = LocalInspectionMode.current


    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            if (isPreview) {
                FrameLayout(ctx)
            } else {
                PreviewView(ctx).also {
                    cameraProviderFuture.addListener(
                        {
                            val cameraProvider = cameraProviderFuture.get()

                            val preview = androidx.camera.core.Preview.Builder().build()
                            preview.setSurfaceProvider(it.surfaceProvider)

                            val cameraSelector = CameraSelector.Builder()
                                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                                .build()

                            try {
                                cameraProvider.unbind()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview,
                                    imageCapture
                                )
                            } catch (e: Exception) {
                                Log.d("CameraX", "bindToLifecycle failed: $e¬")
                            }
                        },
                        ContextCompat.getMainExecutor(it.context)
                    )
                }
            }
        }
    )
}
