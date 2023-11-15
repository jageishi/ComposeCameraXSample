package com.example.composecameraxsample.ui

import android.view.View
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.composecameraxsample.ui.theme.ComposeCameraXSampleTheme

@Composable
fun CameraCaptureScreen(
    isTorchEnabled: Boolean,
    imageCapture: ImageCapture,
    onBindToLifecycleFailed: (Exception) -> Unit,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val isPreview = LocalInspectionMode.current
    var camera by remember { mutableStateOf<Camera?>(null) }

    LaunchedEffect(camera, isTorchEnabled) {
        camera?.cameraControl?.enableTorch(isTorchEnabled)
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            if (isPreview) {
                View(context).also {
                    it.setBackgroundColor(
                        ContextCompat.getColor(context, android.R.color.darker_gray)
                    )
                }
            } else {
                PreviewView(context).also {
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                    cameraProviderFuture.addListener(
                        {
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = androidx.camera.core.Preview.Builder().build()
                            preview.setSurfaceProvider(it.surfaceProvider)

                            try {
                                cameraProvider.unbind()
                                camera = cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector.DEFAULT_BACK_CAMERA,
                                    preview,
                                    imageCapture
                                )
                            } catch (e: Exception) {
                                onBindToLifecycleFailed(e)
                            }
                        },
                        ContextCompat.getMainExecutor(it.context)
                    )
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun CameraCaptureScreenPreview() {
    ComposeCameraXSampleTheme {
        CameraCaptureScreen(
            isTorchEnabled = false,
            imageCapture = ImageCapture.Builder().build(),
            onBindToLifecycleFailed = {}
        )
    }
}
