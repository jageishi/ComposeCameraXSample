package com.example.composecameraxsample.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.widget.Toast
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.example.composecameraxsample.ui.theme.ComposeCameraXSampleTheme

@Composable
fun CaptureScreen() {
    val imageCapture = remember { ImageCapture.Builder().build() }
    val context = LocalContext.current
    var imageBitmap by remember {
        mutableStateOf<ImageBitmap?>(null)
    }
    var isTorchEnabled by remember { mutableStateOf(false) }
    Column {
        CameraCaptureScreen(
            isTorchEnabled = isTorchEnabled,
            imageCapture = imageCapture,
            modifier = Modifier.weight(4f),
            onBindToLifecycleFailed = { exception ->
                Toast.makeText(
                    context,
                    "Error : ${exception.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        )
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                modifier = Modifier.weight(1f),
                checked = isTorchEnabled,
                onCheckedChange = { isTorchEnabled = !isTorchEnabled }
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
                            Toast.makeText(
                                context,
                                "Error : ${exception.message}",
                                Toast.LENGTH_LONG
                            ).show()
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

@Preview(showBackground = true)
@Composable
fun CaptureScreenPreview() {
    ComposeCameraXSampleTheme {
        CaptureScreen()
    }
}
