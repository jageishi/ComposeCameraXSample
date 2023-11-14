package com.example.composecameraxsample.ui

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import java.io.ByteArrayOutputStream


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraPreviewScreen() {
    val permissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleObserver = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_START) {
            permissionState.launchPermissionRequest()
        }
    }

    DisposableEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
        }
    }

    when (permissionState.status) {
        is PermissionStatus.Granted -> {
            val display = LocalConfiguration.current
            display.orientation
            val imageCapture = remember { ImageCapture.Builder().build() }
            val context = LocalContext.current
            var imageBitmap by remember {
                mutableStateOf<ImageBitmap?>(null)
            }
            Box {
                CaptureScreen(imageCapture = imageCapture)
                Button(
                    onClick = {
                        takePicture(
                            context = context,
                            imageCapture = imageCapture,
                            onImageCaptured = { bitmap ->
                                imageBitmap = bitmap.asImageBitmap()
                            },
                            onError = { exception ->
                                Log.d("CameraX", "exception: $exception")
                            }
                        )
                    }
                ) {
                    Text(text = "Capture")
                }
            }
            imageBitmap?.let {
                Image(
                    bitmap = it,
                    contentDescription = null
                )
            }
        }

        is PermissionStatus.Denied -> {
            Text(text = "Denied")
        }
    }
}

private fun takePicture(
    context: Context,
    imageCapture: ImageCapture,
    onImageCaptured: (Bitmap) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {
    ByteArrayOutputStream().use { stream ->
        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(stream).build()
        imageCapture.takePicture(
            outputFileOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exception: ImageCaptureException) {
                    onError(exception)
                }

                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    Log.d("CameraX", "onImageSaved, outputFileResults: $outputFileResults")
                    val byteArray = stream.toByteArray()
                    BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)?.let {
                        onImageCaptured(it)
                    }
                }
            }
        )
    }
}