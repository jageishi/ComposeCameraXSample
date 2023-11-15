package com.example.composecameraxsample.ui

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState


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
            val imageCapture = remember { ImageCapture.Builder().build() }
            val context = LocalContext.current
            var imageBitmap by remember {
                mutableStateOf<ImageBitmap?>(null)
            }
            var enableTorch by remember { mutableStateOf(false) }
            Column {
                CameraCaptureScreen(
                    enableTorch = enableTorch,
                    imageCapture = imageCapture,
                    modifier = Modifier.weight(4f)
                )
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        modifier = Modifier.weight(1f),
                        checked = enableTorch,
                        onCheckedChange = { enableTorch = !enableTorch }
                    )
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            takePicture(
                                context = context,
                                imageCapture = imageCapture,
                                onCaptureSuccess = { bitmap ->
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
                    Image(
                        modifier = Modifier.weight(1f),
                        bitmap = imageBitmap ?: ImageBitmap(1, 1),
                        contentDescription = null
                    )
                }
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
    onCaptureSuccess: (Bitmap) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {

    imageCapture.takePicture(
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                val buffer = image.planes[0].buffer
                val bytes = ByteArray(buffer.capacity()).also { array -> buffer.get(array) }
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                Log.d(
                    "CameraX",
                    "bitmap: $bitmap, image.imageInfo.rotationDegrees: ${image.imageInfo.rotationDegrees}"
                )
                val rotatedBitmap = rotateBitmap(bitmap, image.imageInfo.rotationDegrees.toFloat())
                onCaptureSuccess(rotatedBitmap)
                image.close()
            }

            override fun onError(exception: ImageCaptureException) {
                onError(exception)
            }
        }
    )
}

private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(degrees)
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}